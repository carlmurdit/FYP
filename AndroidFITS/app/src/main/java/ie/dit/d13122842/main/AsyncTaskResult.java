package ie.dit.d13122842.main;

public class AsyncTaskResult<T> {
    private T result;
    private Exception error;
    private String message;

    //constructor for success
    public AsyncTaskResult(T result, String successMessage) {
        super();
        this.result = result;
        this.error = null;
        this.message = successMessage;
    }

    //constructor for failure
    public AsyncTaskResult(Exception error, String failMessage) {
        super();
        this.result = null;
        this.error = error;
        this.message = failMessage;
    }

    public boolean isError() {
        return (error != null);
    }

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

    public String toString() {
        if (error == null) {
            return message;
        } else if (message == null) {
            return error.getMessage();
        } else {
            return this.message + ": " + this.error.getMessage();
        }
    }

}
