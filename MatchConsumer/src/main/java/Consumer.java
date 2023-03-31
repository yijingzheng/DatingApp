import com.mongodb.client.MongoCollection;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.conversions.Bson;
import org.bson.Document;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;

public class Consumer {

    private static final String EXCHANGE_NAME = "swipeExchange";
    private static final String QUEUE_NAME = "matchQueue";
    private static final int QUEUE_SIZE = 10;
    public static final String USER = "user";
    public static final String PASSWORD = "password";
    public static final int PORT = 5672;
    private static final int THREAD_NUM = 50;
    private static final long PERIOD = 3000;

    public static void main(String[] args) throws IOException, TimeoutException {
        String host = args[0];
        //String host = "localhost";

        // connect to RMQ
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setPort(PORT);
        factory.setUsername(USER);
        factory.setPassword(PASSWORD);
        Connection connection = factory.newConnection();

        // connect to mongo
        ConnectionString connectionString = new ConnectionString("mongodb+srv://user:myPassword01@swipedata.jj8l3l3.mongodb.net/?retryWrites=true&w=majority");
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase("swipedata");

        // all potential matches for one user
        Map<Integer, Set<Integer>> matchAllTime = new ConcurrentHashMap<>();
        // potential matches during one updating period
        Map<Integer, Set<Integer>> matchRecord = new ConcurrentHashMap<>();
        // like and dislike numbers during one updating period
        Map<Integer, AtomicInteger[]> statRecord = new ConcurrentHashMap<>();
        for (int i = 0; i < THREAD_NUM; i++) {
            Thread thread = new Thread(new ConsumerThread(EXCHANGE_NAME, QUEUE_NAME, connection, matchAllTime, matchRecord, statRecord, QUEUE_SIZE));
            thread.start();
        }

        updateDB(database, matchRecord, statRecord);
    }

    /** connect to the db and update documents every PERIOD millisecond */
    private static void updateDB(MongoDatabase database, Map<Integer, Set<Integer>> matchRecord, Map<Integer, AtomicInteger[]> statRecord) {
        TimerTask repeatedTask = new repeatedTask(database, matchRecord, statRecord);
        Timer timer = new Timer();
        timer.schedule(repeatedTask, 0, PERIOD);
    }

    static class repeatedTask extends TimerTask {
        private MongoDatabase database;
        private Map<Integer, Set<Integer>> matchRecord;
        private Map<Integer, AtomicInteger[]> statRecord;

        public repeatedTask(MongoDatabase database, Map<Integer, Set<Integer>> matchRecord, Map<Integer, AtomicInteger[]> statRecord) {
            this.database = database;
            this.matchRecord = matchRecord;
            this.statRecord = statRecord;
        }

        @Override
        public void run() {
            if (statRecord.isEmpty()) return;
            Map<Integer, AtomicInteger[]> tempStat = new ConcurrentHashMap<>(statRecord);
            Map<Integer, Set<Integer>> tempMatch = new ConcurrentHashMap<>(matchRecord);
            statRecord.clear();
            matchRecord.clear();
            MongoCollection<Document> collection = database.getCollection("stats");
            for (int swiper : tempStat.keySet()) {
                Bson filter = Filters.eq("swiper", swiper);
                Bson update = Updates.combine(Updates.inc("numLlikes", tempStat.get(swiper)[0]),
                        Updates.inc("numDislikes", tempStat.get(swiper)[1]));
                UpdateOptions options = new UpdateOptions().upsert(true);
                collection.updateOne(filter, update, options);
            }
            collection = database.getCollection("matches");
            for (int swiper : tempMatch.keySet()) {
                Bson filter = Filters.eq("swiper", swiper);
                Bson update = Updates.pushEach("matchList", new ArrayList<>(tempMatch.get(swiper)));
                UpdateOptions options = new UpdateOptions().upsert(true);
                collection.updateOne(filter, update, options);
            }
        }
    }

}
