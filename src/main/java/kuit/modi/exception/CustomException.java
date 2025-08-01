package kuit.modi.exception;

public class CustomException extends RuntimeException {
    private final BaseExceptionResponseStatus status;

    public CustomException(BaseExceptionResponseStatus status) {
        super(status.getMessage());
        this.status = status;
    }

    public BaseExceptionResponseStatus getStatus() {
        return status;
    }
}
