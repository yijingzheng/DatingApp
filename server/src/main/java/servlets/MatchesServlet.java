package servlets;

import com.google.gson.Gson;
import com.mongodb.client.*;
import constant.Constant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.mongodb.*;
import model.Matches;
import org.bson.Document;

@WebServlet(name = "MatchesServlet", value = "/matches")
public class MatchesServlet extends HttpServlet {

    private HashMap<String, Matches> matchesCollection = new HashMap<>();
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {
        ConnectionString connectionString = new ConnectionString(Constant.MONGO_URL);
        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(connectionString)
                .serverApi(ServerApi.builder()
                        .version(ServerApiVersion.V1)
                        .build())
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        MongoDatabase database = mongoClient.getDatabase(Constant.DB_NAME);

        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                HashMap<String, Matches> temp = new HashMap<>();
                MongoCollection<Document> collection = database.getCollection(Constant.COLLECTION_MATCHES);
                MongoCursor<Document> cursor = collection.find().iterator();
                try {
                    while(cursor.hasNext()) {
                        Document doc = cursor.next();
                        temp.put(doc.getString("swiper"), new Matches(doc.getList("matchList", String.class)));
                    }
                } finally {
                    matchesCollection = temp;
                    cursor.close();
                }
            }
        }, 0, Constant.PERIOD, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType(Constant.RES_TYPE);

        String url = request.getPathInfo();

        // validate url
        if (url == null || url.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Constant.INVALID_INPUT);
            return;
        }
        String[] urlParts = url.split("/");
        if (urlParts.length < 2) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Constant.INVALID_INPUT);
            return;
        }

        String userId = urlParts[1];
        if (!matchesCollection.containsKey(userId)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write(gson.toJson(matchesCollection.get(userId)));
            response.getWriter().flush();
        }
    }

}
