package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

import com.google.gson.Gson;
import model.*;

@WebServlet(name = "SwipeServlet", value = "/swipe")
public class SwipeServlet extends HttpServlet {

    private final String RES_TYPE = "application/json";
    private final String LEFT = "/left/";
    private final String RIGHT = "/right/";
    private final String WRONG_URL = "wrong url";
    private final String INVALID_INPUT = "invalid input";
    private Gson gson = new Gson();
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(RES_TYPE);
        String msg;

        String url = request.getPathInfo();

        if (!url.equals(LEFT) && !url.equals(RIGHT)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            msg = this.gson.toJson(new Message(WRONG_URL));
            response.getWriter().write(msg);
            response.getWriter().flush();
            return;
        }

        try {
            StringBuilder sb = new StringBuilder();
            String s;
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
            SwipeDetails detail = this.gson.fromJson(sb.toString(), SwipeDetails.class);

            if (detail.isValid()) {
                response.setStatus(HttpServletResponse.SC_OK);
                msg = this.gson.toJson(detail);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                msg = this.gson.toJson(INVALID_INPUT);
            }
            response.getWriter().write(msg);
            response.getWriter().flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
            response.getWriter().flush();
        }
    }
}
