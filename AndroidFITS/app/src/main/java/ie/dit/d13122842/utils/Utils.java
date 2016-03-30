package ie.dit.d13122842.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import ie.dit.d13122842.main.Enums;

public class Utils {

    public static void tellUI(Handler handler, Enums.UITarget target, String str) {
        // send a message to another thread via a Handler
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("tgt", target.name());
        bundle.putString("str", str);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public static void tellUI(Handler handler, Enums.UITarget target, int num) {
        // send a message to another thread via a Handler
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("tgt", target.name());
        bundle.putInt("num", num);
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public static void tellUI(Handler handler, Enums.UITarget target) {
        // send a message to another thread via a Handler
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("tgt", target.name());
        message.setData(bundle);
        handler.sendMessage(message);
    }

    public static void longLogV(String tag, String str, boolean enabled) {
        // Split debug messages into multiple smaller ones
        if(!enabled) return; // disable
        // avoid max length of logcat messages
        if(str.length() > 4000) {
            Log.v(tag, str.substring(0, 4000));
            longLogV(tag, str.substring(4000), enabled);
        } else
            Log.v(tag, str);
    }

}
