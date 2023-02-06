package task2;

public class Counter {

    private int successCount = 0;
    private int failCount = 0;
    private boolean stop = false;

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

    public boolean isStop() {
        return stop;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
