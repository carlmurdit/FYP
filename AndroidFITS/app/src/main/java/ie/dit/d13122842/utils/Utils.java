package ie.dit.d13122842.utils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class Utils {

    public static void tellUI(Handler handler, String msg) {
        // send a message to another thread via a Handler
        Message message = handler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putString("msg", msg);
        message.setData(bundle);
        handler.sendMessage(message);
    }

}
