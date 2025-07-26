package kuit.modi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//잘못된 연도/월 파라미터 ->400
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidYearMonthException extends RuntimeException {
  public InvalidYearMonthException() {
    super("Invalid year or month parameter");
  }
}
