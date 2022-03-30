package client.utils;

import javafx.concurrent.Task;
import org.glassfish.jersey.internal.util.Producer;

public class TimeUtils extends Task<Void> {

    private final long maxTime;
    private final long updateInterval;
    private final long initialTime;
    private Producer<Double> getTimeBoost;
    private double timeElapsed;

    public TimeUtils(long duration, long updateInterval) {
        super();
        this.maxTime = duration;
        this.updateInterval = updateInterval;
        this.getTimeBoost = () -> 0.0;
        this.timeElapsed = 0;
        this.initialTime = duration;
    }

    public TimeUtils(long duration, long updateInterval, long initialTime) {
        super();
        this.maxTime = duration;
        this.updateInterval = updateInterval;
        this.getTimeBoost = () -> 0.0;
        this.timeElapsed = 0;
        this.initialTime = initialTime;
    }

    @Override
    protected Void call() throws Exception {
        double refreshCounter = 0;
        long gameRoundMs = maxTime * 1000;
        while (this.timeElapsed < gameRoundMs) {
            //the speed on which the timer updates, with default speed 1
            double booster = getTimeBoost.call() + 1;
            updateProgress(gameRoundMs - this.timeElapsed, initialTime * 1000);
            refreshCounter += booster;
            try {
                Thread.sleep(updateInterval);
                this.timeElapsed = refreshCounter * updateInterval;
            } catch (InterruptedException e) {
                updateProgress(0, 1);
                throw e;
            }
        }
        updateProgress(0, 1);
        return null;
    }

    public void setTimeBooster(Producer<Double> booster) {
        this.getTimeBoost = booster;
    }

    public long getElapsedTime() {
        return (long) this.timeElapsed/1000;
    }
}
