package servlets;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import constant.Constant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.mongodb.client.MongoCollection;
import com.mongodb.*;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

@WebServlet(name = "MatchesServlet", value = "/matches")
public class MatchesServlet extends HttpServlet {

    private MongoDatabase database;

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
        this.database = mongoClient.getDatabase(Constant.DB_NAME);
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
        MongoCollection<Document> collection = database.getCollection(Constant.COLLECTION_MATCHES);
        Bson filter = Filters.eq("swiper", userId);
        Bson projectionFields = Projections.fields(Projections.include("matchList"),Projections.excludeId());
        Document result = collection.find(filter).projection(projectionFields).first();
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
            //List<String> matchList = result.getList("candidate", String.class);
            //Matches matches = new Matches(matchList);
            response.getWriter().write(result.toJson());
            //Gson gson = new Gson();
            //response.getWriter().write(gson.toJson(matches));
            response.getWriter().flush();
        }
    }

}
