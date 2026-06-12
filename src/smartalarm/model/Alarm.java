package smartalarm.model;

import java.time.LocalTime;

public class Alarm {

    private final LocalTime time;
    private boolean fired = false;

    public Alarm(LocalTime time) {
        this.time = time;
    }

    public LocalTime getTime() {
        return time;
    }

    public boolean isFired() {
        return fired;
    }

    public void setFired(boolean fired) {
        this.fired = fired;
    }

    public boolean matches(LocalTime now) {
        return time.getHour() == now.getHour()
                && time.getMinute() == now.getMinute()
                && time.getSecond() == now.getSecond();
    }
}
