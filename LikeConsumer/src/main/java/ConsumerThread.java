import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.DeliverCallback;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class ConsumerThread implements Runnable {

    private String exchangeName;
    private String queueName;
    private Connection connection;
    private Map<Integer, AtomicInteger[]> record;
    private int queueSize;
    private Gson gson = new Gson();

    public ConsumerThread(String exchangeName, String queueName, Connection connection, Map<Integer, AtomicInteger[]> record, int queueSize) {
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
                int idx = userLiked ? 0 : 1;
                if (!record.containsKey(swiper)) {
                    record.put(swiper, new AtomicInteger[]{new AtomicInteger(0), new AtomicInteger(0)});
                }
                record.get(swiper)[idx].incrementAndGet();
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            };
            channel.basicConsume(queueName, false, deliverCallback, consumerTag -> { });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
