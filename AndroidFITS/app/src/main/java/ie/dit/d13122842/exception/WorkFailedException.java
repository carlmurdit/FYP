package ie.dit.d13122842.exception;

public class WorkFailedException extends Exception {
    public WorkFailedException(String message, Exception e) {
        super(message, e);
    }
    public WorkFailedException(String message) {
        super(message);
    }
    public WorkFailedException(Exception e) {
        super(e);
    }
    public WorkFailedException() {
        super();
    }
}
