package task2;

import io.swagger.client.ApiClient;
import io.swagger.client.ApiException;
import io.swagger.client.api.SwipeApi;
import io.swagger.client.model.SwipeDetails;

import java.util.Random;
import java.util.concurrent.CountDownLatch;

public class ClientThread implements Runnable {
    ApiClient apiClient;
    SwipeApi apiInstance;
    Random rand = new Random();
    private CountDownLatch latch;
    private Counter counter;
    private int requestNumber;
    private final static int RETRY_MAX = 5;
    private final static int SWIPER_MAX = 5000;
    private final static int SWIPEE_MAX = 1000000;
    private final static int COMMENT_MAX = 256;
    private final static String[] leftOrRight = new String[]{"left", "right"};

    public ClientThread(String url, int requestNumber, CountDownLatch latch, Counter counter) {
        this.requestNumber = requestNumber;
        this.latch = latch;
        this.counter = counter;
        apiClient = new ApiClient();
        apiClient.setBasePath(url);
        this.apiInstance = new SwipeApi(apiClient);
    }

    private SwipeDetails generateBody() {
        String swiper = String.valueOf(rand.nextInt(SWIPER_MAX) + 1);
        String swipee = String.valueOf(rand.nextInt(SWIPEE_MAX) + 1);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < COMMENT_MAX; i++) {
            sb.append((char)rand.nextInt(128));
        }
        String comment = sb.toString();
        return new SwipeDetails().swiper(swiper).swipee(swipee).comment(comment);
    }

    @Override
    public void run() {
        for (int i = 0; i < requestNumber; i++) {
            if (counter.isStop()) {
                return;
            }
            int retry = 0;
            while(retry < RETRY_MAX) {
                try {
                    apiInstance.swipe(generateBody(), leftOrRight[rand.nextInt(leftOrRight.length)]);
                    break;
                } catch (ApiException e) {
                    retry += 1;
                }
            }
            if (retry < RETRY_MAX) {
                counter.successInc();
            } else {
                counter.failInc();
            }
            latch.countDown();
        }
    }
}
