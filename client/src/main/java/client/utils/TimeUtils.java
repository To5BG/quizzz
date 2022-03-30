package client.utils;

import javafx.concurrent.Task;
import org.glassfish.jersey.internal.util.Producer;

public class TimeUtils extends Task<Void> {

    private final long maxTime;
    private final long updateInterval;
    private Producer<Double> getTimeBoost;

    public TimeUtils(long duration, long updateInterval) {
        super();
        this.maxTime = duration;
        this.updateInterval = updateInterval;
        this.getTimeBoost = () -> 0.0;
    }

    @Override
    protected Void call() throws Exception {
        double refreshCounter = 0;
        long gameRoundMs = maxTime * 1000;
        double timeElapsed = 0;
        while (timeElapsed < gameRoundMs) {
            //the speed on which the timer updates, with default speed 1
            double booster = getTimeBoost.call() + 1;
            updateProgress(gameRoundMs - timeElapsed, gameRoundMs);
            refreshCounter += booster;
            try {
                Thread.sleep(updateInterval);
                timeElapsed = refreshCounter * updateInterval;
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
}
