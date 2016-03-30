package ie.dit.d13122842.main;

import android.os.Handler;
import android.util.Log;

import ie.dit.d13122842.utils.Utils;

public class Magnitude {
    private final Handler handler;

    public Magnitude(Handler handler) {
        this.handler = handler;
    }

    public void doWork() {
        // todo
        Log.d("fyp", "In Magnitude");
        Utils.tellUI(handler, Enums.UITarget.ACT_STATUS, "Working on magnitude...");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // todo: finish implementation
        }
    }
}