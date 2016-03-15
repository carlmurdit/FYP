package ie.dit.d13122842.config;

public class Config {

    public static class MQ {
        // public static final String QUEUE_URI = "amqp://test:test@192.168.3.21:5672";
        public static final String QUEUE_URI = "amqp://test:test@192.168.43.82:5672";
        public static final String MANAGEMENT_UI = "http://192.168.3.21:15672"; // FYI
        public static final String QUEUE_NAME_CTL = "control_queue";
        public static final String QUEUE_NAME_WRK = "work_queue";
        public static final String QUEUE_NAME_RLT = "result_queue";
    }


}
