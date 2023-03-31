package task2;

import java.util.ArrayList;
import java.util.concurrent.*;

public class client1 {

    private static final int GET_PER_SECOND = 5;

    public static void main(String[] args) throws InterruptedException {
//        String url = args[0];
//        int THREAD_NUM = Integer.parseInt(args[1]);
//        int TOTAL_REQUESTS = Integer.parseInt(args[2]);
        String url = "http://54.185.12.133:8080/server_war/";
        int THREAD_NUM = 100;
        int TOTAL_REQUESTS = 500000;
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);
        Counter counter = new Counter();
        ArrayList<ArrayList<Long>> getResponseTime = new ArrayList<>();

        ExecutorService pool = Executors.newFixedThreadPool(THREAD_NUM);
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_NUM; i++) {
            ClientThread clientThread = new ClientThread(url, (int)(TOTAL_REQUESTS / THREAD_NUM * 1.1), latch, counter);
            pool.execute(clientThread);
        }

        ScheduledExecutorService service = Executors.newScheduledThreadPool(GET_PER_SECOND);
        for (int i = 0; i < GET_PER_SECOND; i++) {
            getResponseTime.add(new ArrayList<>());
            service.scheduleAtFixedRate(new repeatedTask(url, i, getResponseTime.get(i), 5000), 0, 1, TimeUnit.SECONDS);
        }

        latch.await();

        service.shutdown();
        counter.setStop(true);
        pool.shutdown();
        long end = System.currentTimeMillis();

        printResult(THREAD_NUM, start, end, counter, getResponseTime);
    }

    private static void printResult(int THREAD_NUM, long start, long end, Counter counter, ArrayList<ArrayList<Long>> getResponseTime) {
        double throughput = (counter.getSuccessCount() + counter.getFailCount()) / ((end - start) * 1.0 /1000);
        long sum = 0;
        int count = 0;
        long maxLatency = 0;
        long minLatency = end - start;
        for (int i = 0; i < GET_PER_SECOND; i++) {
            for (long latency : getResponseTime.get(i)) {
                sum += latency;
                count += 1;
                maxLatency = Math.max(maxLatency, latency);
                minLatency = Math.min(minLatency, latency);
            }
        }

        System.out.println("Thread Number: " + THREAD_NUM);
        System.out.println("Run Time: " + (end - start) * 1.0 / 1000 + "s");
        System.out.println("Successful Requests: " + counter.getSuccessCount());
        System.out.println("Failed Requests: " + counter.getFailCount());
        System.out.println("Throughput: " + throughput);
        System.out.println("POST mean response time: " + 1000 * THREAD_NUM / throughput);
        System.out.println("GET mean response time: " + sum * 1.0 / count);
        System.out.println("GET min response time: " + minLatency);
        System.out.println("GET max response time: " + maxLatency);
    }
}
