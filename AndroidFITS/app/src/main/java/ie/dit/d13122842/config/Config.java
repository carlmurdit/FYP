package ie.dit.d13122842.config;

public class Config {

    public static class MQ {
        // Management UI: http://192.168.3.21:15672
        public static final String QUEUE_URI = "amqp://test:test@192.168.3.21:5672";
        public static final String QUEUE_NAME_CTL = "control_queue";
    }


}
