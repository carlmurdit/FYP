package ie.dit.d13122842.utils;


import android.os.SystemClock;

public class Timer {
    long tStart;

    public void start() {
        tStart = SystemClock.elapsedRealtime();
    }

    public Long stop() {
        return SystemClock.elapsedRealtime() - tStart;
    }

}
