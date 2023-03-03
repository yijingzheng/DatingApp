package task3;

import task2.Counter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.*;

public class client2 {
    private final static int THREAD_NUM = 100;
    private final static int TOTAL_REQUESTS = 500000;
    private final static String url = "http://localhost:8080/server_war";
    private final static String path = THREAD_NUM + "-" + TOTAL_REQUESTS + ".csv";

    public static void main(String[] args) throws InterruptedException, IOException {
        CountDownLatch latch = new CountDownLatch(TOTAL_REQUESTS);
        Counter counter = new Counter();
        ExecutorService pool = Executors.newFixedThreadPool(THREAD_NUM);
        long start = System.currentTimeMillis();
        ConcurrentHashMap<Integer, ArrayList<Record>> records = new ConcurrentHashMap<>();
        for (int i = 0; i < THREAD_NUM; i++) {
            records.put(i, new ArrayList<>());
            ClientThread clientThread = new ClientThread(url, (int)(TOTAL_REQUESTS / THREAD_NUM * 1.1), latch, counter, records, i);
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
        processRecords(records);
    }

    private static void processRecords(ConcurrentHashMap<Integer, ArrayList<Record>> records) throws IOException {
        ArrayList<Record> arr = new ArrayList<>();
        FileWriter fileWriter = new FileWriter(path);
        long sum = 0;
        long min = Integer.MAX_VALUE;
        long max = 0;
        for (ArrayList<Record> curr : records.values()) {
            for (int i = 0; i < curr.size(); i++) {
                Record record = curr.get(i);
                fileWriter.write(record.toString());
                arr.add(record);
                sum += record.getLatency();
                min = Math.min(min, record.getLatency());
                max = Math.max(max, record.getLatency());
            }
        }
        fileWriter.close();
        Collections.sort(arr, (a, b) -> Long.compare(a.getLatency(), b.getLatency()));
        System.out.println("mean response time (millisecs): " + sum * 1.0 / arr.size());
        System.out.println("median response time (millisecs): " + arr.get(arr.size() / 2).getLatency());
        System.out.println("p99 response time: " + arr.get((int)(arr.size() * 0.99)).getLatency());
        System.out.println("min response time (millisecs): " + min);
        System.out.println("max response time (millisecs): " + max);
    }
}
