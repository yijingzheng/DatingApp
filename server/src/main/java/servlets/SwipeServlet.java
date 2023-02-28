package servlets;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.*;
import java.io.IOException;

import com.google.gson.Gson;
import model.*;
import rmqpool.*;
import constant.*;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.util.concurrent.TimeoutException;

@WebServlet(name = "SwipeServlet", value = "/swipe")
public class SwipeServlet extends HttpServlet {

    private Gson gson = new Gson();
    private RMQChannelPool pool;

    @Override
    public void init() throws ServletException {
        super.init();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(Constant.HOST);
        factory.setPort(Constant.PORT);
        factory.setUsername(Constant.USER);
        factory.setPassword(Constant.PASSWORD);
        try {
            Connection connection = factory.newConnection();
            this.pool = new RMQChannelPool(Constant.POOL_SIZE, new RMQChannelFactory(connection));
        } catch (IOException | TimeoutException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType(Constant.RES_TYPE);

        String url = request.getPathInfo();

        if (!url.equals(Constant.LEFT) && !url.equals(Constant.RIGHT)) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write(Constant.WRONG_URL);
            response.getWriter().flush();
            return;
        }

        String status = url.equals(Constant.LEFT) ? Constant.DISLIKE : Constant.LIKE;
        StringBuilder sb = new StringBuilder();
        String s;

        try {
            while ((s = request.getReader().readLine()) != null) {
                sb.append(s);
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(e.getMessage());
            response.getWriter().flush();
        }

        SwipeDetails detail = this.gson.fromJson(sb.toString(), SwipeDetails.class);

        if (!detail.isValid()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(Constant.INVALID_INPUT);
            response.getWriter().flush();
        }

        if (pool == null) {
            System.out.println("why is pool null");
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("why is pool null");
            return;
        }
        Channel channel = pool.borrowObject();
        channel.queueDeclare(Constant.STATUS_QUEUE, false, false, false, null);
        channel.queueDeclare(Constant.MATCHES_QUEUE, false, false, false, null);
        channel.basicPublish("", Constant.STATUS_QUEUE, null, status.getBytes());
        if (status.equals(Constant.LIKE)) {
            channel.basicPublish("", Constant.MATCHES_QUEUE, null, this.gson.toJson(detail).toString().getBytes());
        }
        pool.returnObject(channel);

        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().write(Constant.SUCCEED);
        response.getWriter().flush();
    }
}
