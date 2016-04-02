package ie.dit.d13122842.exception;

public class ResultPublicationException extends Exception {
    public ResultPublicationException(String message, Exception e) {
        super(message, e);
    }
    public ResultPublicationException(String message) {
        super(message);
    }
    public ResultPublicationException(Exception e) {
        super(e);
    }
    public ResultPublicationException() {
        super();
    }
}