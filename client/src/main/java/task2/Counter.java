package task2;

public class Counter {

    private int successCount = 0;
    private int failCount = 0;

    synchronized public void successInc() {
        successCount += 1;
    }

    synchronized public void failInc() {
        failCount += 1;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }
}
