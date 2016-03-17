package ie.dit.d13122842.main;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

public class History {

    private static Map<String, Long> units = new HashMap<String, Long>();;

    public static Map<String, Long> getUnits() {
        return units;
    }

    public static int getUnitCount() {
        return units.size();
    }

    public static Long getAvgTimePerUnit() throws Exception {
        if (units.size()==0) {
            throw new Exception("Attempt to get average time when no units have been timed.");
        }
        Long timeSum = new Long(0);
        for (Long time : units.values()) {
            timeSum += time;
        }
        return timeSum / units.size();
    }

    public static void reset() {
        units.clear();
    }

    public static void record(String key, Long time) {
        units.put(key, time);
        for (Map.Entry<String, Long> unit : units.entrySet()) {
            Log.d("fyp", "Time for "+unit.getKey()+": "+unit.getValue());
        }
    }

}
