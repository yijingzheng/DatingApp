package task2;

import io.swagger.client.*;
import io.swagger.client.api.MatchesApi;
import io.swagger.client.api.StatsApi;

import java.util.ArrayList;
import java.util.Random;

public class repeatedTask implements Runnable {

    private MatchesApi matchesApi;
    private StatsApi statsApi;
    private int num;
    private Random rand = new Random();
    private ArrayList<Long> latency;
    private int swiperMax;

    public repeatedTask(String url, int num, ArrayList<Long> latency, int swiperMax) {
        this.num = num;
        this.latency = latency;
        this.matchesApi = new MatchesApi();
        this.statsApi = new StatsApi();
        this.swiperMax = swiperMax;
        matchesApi.getApiClient().setBasePath(url);
        statsApi.getApiClient().setBasePath(url);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {
            if (num % 2 == 0) {
                matchesApi.matches(String.valueOf(rand.nextInt()) + 1);
            } else {
                statsApi.matchStats(String.valueOf(rand.nextInt()) + 1);
            }
        } catch (ApiException e) {
            //System.err.println(e.getCode());
            //e.printStackTrace();
        } finally {
            long end = System.currentTimeMillis();
            latency.add(end - start);
        }
    }
}