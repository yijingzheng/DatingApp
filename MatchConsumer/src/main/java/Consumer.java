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

    public static void main(String[] args) throws IOException, TimeoutException {
        //String host = args[0];
        String host = "localhost";

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

        Map<Integer, Set<Integer>> matchAllTime = new ConcurrentHashMap<>();
        Map<Integer, Set<Integer>> matchRecord = new ConcurrentHashMap<>();
        Map<Integer, AtomicInteger[]> statusRecord = new ConcurrentHashMap<>();
        for (int i = 0; i < THREAD_NUM; i++) {
            Thread thread = new Thread(new ConsumerThread(EXCHANGE_NAME, QUEUE_NAME, connection, matchAllTime, matchRecord, statusRecord, QUEUE_SIZE));
            thread.start();
        }

        updateDB(database, matchRecord, statusRecord);
    }

    private static void updateDB(MongoDatabase database, Map<Integer, Set<Integer>> matchRecord, Map<Integer, AtomicInteger[]> statusRecord) {
        TimerTask repeatedTask = new repeatedTask(database, matchRecord, statusRecord);
        Timer timer = new Timer();
        timer.schedule(repeatedTask, 0, 2000);
    }

    static class repeatedTask extends TimerTask {
        private MongoDatabase database;
        private Map<Integer, Set<Integer>> matchRecord;
        private Map<Integer, AtomicInteger[]> statusRecord;

        public repeatedTask(MongoDatabase database, Map<Integer, Set<Integer>> matchRecord, Map<Integer, AtomicInteger[]> statusRecord) {
            this.database = database;
            this.matchRecord = matchRecord;
            this.statusRecord = statusRecord;
        }

        @Override
        public void run() {
            if (statusRecord.isEmpty()) return;
            Map<Integer, AtomicInteger[]> tempStatus = new ConcurrentHashMap<>(statusRecord);
            Map<Integer, Set<Integer>> tempMatch = new ConcurrentHashMap<>(matchRecord);
            statusRecord.clear();
            matchRecord.clear();
            MongoCollection<Document> collection = database.getCollection("status");
            for (int swiper : tempStatus.keySet()) {
                //System.out.println(swiper);
                Bson filter = Filters.eq("swiper", swiper);
                Bson update = Updates.combine(Updates.inc("likes", tempStatus.get(swiper)[0]),
                        Updates.inc("dislikes", tempStatus.get(swiper)[1]));
                UpdateOptions options = new UpdateOptions().upsert(true);
                collection.updateOne(filter, update, options);
            }
            collection = database.getCollection("matches");
            for (int swiper : tempMatch.keySet()) {
                Bson filter = Filters.eq("swiper", swiper);
                Bson update = Updates.pushEach("candidate", new ArrayList<>(tempMatch.get(swiper)));
                UpdateOptions options = new UpdateOptions().upsert(true);
                collection.updateOne(filter, update, options);
            }
        }
    }

}
