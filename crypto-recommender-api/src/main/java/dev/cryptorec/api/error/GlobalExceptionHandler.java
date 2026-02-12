package dev.cryptorec.api.error;

import dev.cryptorec.model.exception.CryptoNotFoundException;
import dev.cryptorec.model.exception.InvalidTimeframeException;
import dev.cryptorec.model.exception.ValidationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.cryptorec.api.generated.model.ErrorResponse;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MissingRequestValueException;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CryptoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCryptoNotFound(final CryptoNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse().code(404).message("Cryptocurrency not found").details(List.of(ex.getMessage())));
    }

    @ExceptionHandler(InvalidTimeframeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeframe(final InvalidTimeframeException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse().code(400).message("Invalid timeframe parameters").details(List.of(ex.getMessage())));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(final ValidationException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse().code(400).message("Validation failed").details(List.of(ex.getMessage())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(final Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse().code(500).message("Unexpected error occurred").details(List.of(ex.getMessage())));
    }

    @ExceptionHandler(MissingRequestValueException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(final MissingRequestValueException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse().code(400).message("Validation failed").details(List.of(ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(final MethodArgumentNotValidException ex) {
        List<String> messages = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage()).toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse().code(400).message("Validation failed").details(messages));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolations(final ConstraintViolationException ex) {
        List<String> messages = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage()).toList();

        return ResponseEntity.badRequest()
                .body(new ErrorResponse().code(400).message("Validation failed").details(messages));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJson(final HttpMessageNotReadableException ex) {
        return ResponseEntity.badRequest()
                .body(new ErrorResponse().code(400).message("Malformed request body")
                        .details(List.of(ex.getMostSpecificCause().getMessage())));
    }

}
