package kuit.modi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//잘못된 날짜 파라미터 -> 400
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidDateException extends RuntimeException {
  public InvalidDateException() {
    super("Invalid date parameter");
  }
}
