package nl.qunit.crudmeister.model;

import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Order(50)
public class DefaultExceptionHandler {
//    @ExceptionHandler(Throwable.class)
//    public ProblemDetail handle(Throwable e) {
//        ProblemDetail problemDetail =
//                ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
//        problemDetail.setDetail(e.getMessage());
//        return problemDetail;
//    }
}
