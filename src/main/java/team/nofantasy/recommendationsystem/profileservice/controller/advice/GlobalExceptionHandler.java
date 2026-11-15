package team.nofantasy.recommendationsystem.profileservice.controller.advice;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * {@code GlobalExceptionHandler} is dedicated to centrally handle all uncaught runtime exceptions in the application.
 */
@RestControllerAdvice
@Slf4j
@SuppressWarnings(value = {"unused"})
public class GlobalExceptionHandler {
    private void makeDefaultErrorLog(Exception e) {
        log.atError()
                .setMessage(e.getMessage())
                .setCause(e.getCause())
                .log();
    }

    @ExceptionHandler(exception = IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        makeDefaultErrorLog(e);
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(exception = EntityNotFoundException.class)
    public ResponseEntity<Void> handleEntityNotFoundException(EntityNotFoundException e) {
        makeDefaultErrorLog(e);
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(exception = MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = error instanceof FieldError fieldError ? fieldError.getField() : error.getObjectName();
            errors.put(fieldName, error.getDefaultMessage());
        });

        log.error("Validation errors: {}", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}
