package ie.dit.d13122842.exception;

public class WorkException extends Exception {
    public WorkException(String message, Exception e) {
        super(message, e);
    }
    public WorkException(String message) {
        super(message);
    }
    public WorkException(Exception e) {
        super(e);
    }
    public WorkException() {
        super();
    }
}
