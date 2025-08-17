package kr.or.connect.reservation.config.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ExceptionAdvice {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<?> handleBusiness(CustomException ex, HttpServletRequest req) {
        CustomExceptionStatus s = ex.getCustomExceptionStatus();
        Map<String, Object> body = Map.of(
                "code", s.getCode(),
                "message", s.getMessage(),
                "status", s.getHttpStatus().value(),
                "path", req.getRequestURI()
        );
        return ResponseEntity.status(s.getHttpStatus()).body(body);
    }

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
