package cc.meteormc.sbpractice.arena.task;

import com.google.common.util.concurrent.AtomicDouble;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

@Getter
public class Timer {
    @Setter
    private boolean canStart = true;
    private final AtomicLong startTime = new AtomicLong(0L);
    private final AtomicDouble time = new AtomicDouble(0D);
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public boolean isStarted() {
        return this.isStarted.get();
    }

    public double getTime() {
        if (this.isStarted()) {
            time.set((System.currentTimeMillis() - startTime.get()) / 1000D);
        }
        return time.get();
    }

    public String getFormattedTime() {
        return String.format("%.3f", this.getTime());
    }

    public void startTimer() {
        if (!this.canStart) return;
        this.startTime.set(System.currentTimeMillis());
        this.time.set(0D);
        this.isStarted.set(true);
    }

    public void stopTimer() {
        this.getTime();
        this.isStarted.set(false);
    }
}
