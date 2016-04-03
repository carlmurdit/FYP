package ie.dit.d13122842.exception;

public class ActivationException extends Exception {
    public ActivationException(String message, Exception e) {
        super(message, e);
    }
    public ActivationException(String message) {
        super(message);
    }
    public ActivationException(Exception e) {
        super(e);
    }
    public ActivationException() {
        super();
    }
}
