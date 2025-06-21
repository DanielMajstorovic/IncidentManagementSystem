package eu.reportincident.moderation_service.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.HandlerMethod;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public final ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e, HandlerMethod handlerMethod) {
        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpException.class)
    public final ResponseEntity<Object> handleHttpException(HttpException e, HandlerMethod handlerMethod) {
        getLogger(handlerMethod).error(getMessage(e));
        if (e.getStatus() == null)
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(e.getData(), e.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleException(Exception e, HandlerMethod handlerMethod) {
        getLogger(handlerMethod).error(getMessage(e));
        return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private Logger getLogger(HandlerMethod handlerMethod) {
        return LogManager.getLogger(handlerMethod.getMethod().getDeclaringClass());
    }

    private String getMessage(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append(throwable);
        builder.append(System.lineSeparator());
        Arrays.stream(throwable.getStackTrace()).limit(1).forEach(l -> {
            builder.append(l);
            builder.append(System.lineSeparator());
        });
        return builder.toString();
    }
}