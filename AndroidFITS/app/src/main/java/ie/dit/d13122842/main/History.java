package ie.dit.d13122842.main;

public class History {

    private static int unitCount = 0;
    private static int averageTime = 0;

    public static int getUnitCount() {
        return unitCount;
    }

    public static int getAverageTime() throws Exception {
        if (unitCount == 0)
            throw new Exception("Attempt to get average time when no units have been timed.");
        return averageTime;
    }

    public static void insert(int value) {
        averageTime = (unitCount * averageTime + value) / (unitCount + 1);
        unitCount++;
    }

    public synchronized static void reset() {
        unitCount = 0;
        averageTime = 0;
    }

}
