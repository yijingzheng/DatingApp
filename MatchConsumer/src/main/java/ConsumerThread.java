import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ConsumerThread implements Runnable {

    private String exchangeName;
    private String queueName;
    private Connection connection;
    private Map<Integer, Set<Integer>> record;
    private int queueSize;
    private Gson gson = new Gson();
    private static final int MATCHES_NUM = 100;

    public ConsumerThread(String exchangeName, String queueName, Connection connection, Map<Integer, Set<Integer>> record, int queueSize) {
        this.exchangeName = exchangeName;
        this.queueName = queueName;
        this.connection = connection;
        this.record = record;
        this.queueSize = queueSize;
    }

    @Override
    public void run() {
        try {
            Channel channel = connection.createChannel();
            // Name, Durable (survive a broker restart), Exclusive (used by only one connection), Auto-delete, Arguments
            channel.queueDeclare(queueName, false, false, false, null);
            channel.exchangeDeclare(exchangeName, "fanout");
            channel.queueBind(queueName, exchangeName, "");
            channel.basicQos(queueSize);

            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");
                JsonObject info = this.gson.fromJson(message, JsonObject.class);
                int swiper = info.get("swiper").getAsInt();
                boolean userLiked = info.get("userLiked").getAsBoolean();
                if (!record.containsKey(swiper)) {
                    Set<Integer> mySet = ConcurrentHashMap.newKeySet();
                    record.put(swiper, mySet);
                }
                if (userLiked && record.get(swiper).size() < MATCHES_NUM) {
                    int swipee = info.get("swipee").getAsInt();
                    record.get(swiper).add(swipee);
                }
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
