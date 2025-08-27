package cc.meteormc.sbpractice.feature.task;

import cc.meteormc.sbpractice.config.MainConfig;
import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Timer {
    private final AtomicBoolean canStart = new AtomicBoolean(true);
    private final AtomicLong startTime = new AtomicLong(0L);
    private final AtomicDouble time = new AtomicDouble(0D);
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public boolean isCanStart() {
        return this.canStart.get();
    }

    public void setCanStart(boolean canStart) {
        this.canStart.set(canStart);
    }

    public double getTime() {
        if (this.isStarted()) {
            this.time.set((System.currentTimeMillis() - startTime.get()) / 1000D);
        }

        double t = this.time.get();
        return t >= Double.MIN_VALUE ? Math.max(1 / 20.0D, t) : 0.0D;
    }

    public String getFormattedTime() {
        double t = this.getTime();
        return String.format("%.3f", MainConfig.NORMALIZE_TIME.resolve() ? Math.round(t * 20) / 20.0 : t);
    }

    public boolean isStarted() {
        return this.isStarted.get();
    }

    public void startTimer() {
        if (!this.isCanStart()) return;
        this.startTime.set(System.currentTimeMillis());
        this.time.set(0D);
        this.isStarted.set(true);
    }

    public void stopTimer() {
        this.getTime(); // Refresh time
        this.isStarted.set(false);
    }
}
