package kuit.modi.exception;

import org.springframework.http.HttpStatus;

public interface BaseExceptionResponseStatus {
    int getCode();
    HttpStatus getHttpStatus();
    String getMessage();
}
