package task2;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class client1 {

    public static void main(String[] args) throws InterruptedException {
        String url = args[0];
        int THREAD_NUM = Integer.parseInt(args[1]);
        int TOTAL_REQUESTS = Integer.parseInt(args[2]);
        //String url = "http://CS6650-alb-1967874639.us-west-2.elb.amazonaws.com/server_war";
        //int THREAD_NUM = 10;
        //int TOTAL_REQUESTS = 100;
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);
        Counter counter = new Counter();
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_NUM);
        long start = System.currentTimeMillis();
        for (int i = 0; i < THREAD_NUM; i++) {
            ClientThread clientThread = new ClientThread(url, (int)(TOTAL_REQUESTS / THREAD_NUM * 1.1), latch, counter);
            pool.execute(clientThread);
        }
        latch.await();
        counter.setStop(true);
        pool.shutdown();
        long end = System.currentTimeMillis();
        System.out.println("Thread Number: " + THREAD_NUM);
        System.out.println("Run Time: " + (end - start) * 1.0 / 1000 + "s");
        System.out.println("Successful Requests: " + counter.getSuccessCount());
        System.out.println("Failed Requests: " + counter.getFailCount());
        System.out.println("Throughput: " + (counter.getSuccessCount()+counter.getFailCount()) / ((end - start) * 1.0 /1000));
//        Thread.sleep(2000);
//        System.out.println(pool.isShutdown());
//        System.out.println(counter.getSuccessCount()+counter.getFailCount());
    }
}
