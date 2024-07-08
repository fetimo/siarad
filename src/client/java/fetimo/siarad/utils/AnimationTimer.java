package fetimo.siarad.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class AnimationTimer {
    public static void start(Runnable task, Runnable callback) {
        try {
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            int delay = 5000;
            int animationDuration = 300;

            scheduler.schedule(task, delay, TimeUnit.MILLISECONDS);
            scheduler.schedule(callback, delay + animationDuration, TimeUnit.MILLISECONDS);

            // Shutdown the scheduler after the task execution to free resources
            scheduler.schedule(scheduler::shutdown, delay + 1000, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
