package ie.dit.d13122842.main;

public class AsyncTaskResult<T> {
    private T result;
    private Exception error;

    //constructor for success
    public AsyncTaskResult(T result) {
        super();
        this.result = result;
    }

    //constructor for failure
    public AsyncTaskResult(Exception error, String failMessage) {
        super();
        if (error == null) error = new Exception(failMessage);
        Exception e = new Exception(failMessage+". "+ error.getMessage(), error);
        this.error = e;
    }

    public T getResult() {
        return result;
    }

    public Exception getError() {
        return error;
    }

}
