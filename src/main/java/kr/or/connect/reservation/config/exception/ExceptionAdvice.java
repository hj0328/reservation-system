package kr.or.connect.reservation.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ErrorResponse> bindingExceptionHandle(BindException e) {
        List<String> reasons = new ArrayList<>();
        e.getBindingResult().getAllErrors().listIterator()
                .forEachRemaining(fe -> reasons.add(fe.getDefaultMessage()));

        ErrorResponse errorResponse = new ErrorResponse(CustomExceptionStatus.INVALID_REQUEST_ERROR.toString()
                , reasons);

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public CustomExceptionStatus exHandle(CustomException e) {
        return e.getCustomExceptionStatus();
    }
}
