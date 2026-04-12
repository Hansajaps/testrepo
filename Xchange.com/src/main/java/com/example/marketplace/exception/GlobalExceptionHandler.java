package com.example.marketplace.exception;

import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── MongoDB DNS / Network Failures ──────────────────────────────────────

    /**
     * Fires when the MongoDB Atlas hostname cannot be resolved.
     * Root cause: ISP DNS failure or no network access.
     * Fix: Switch to Google DNS (8.8.8.8) and run `ipconfig /flushdns`.
     */
    @ExceptionHandler(UnknownHostException.class)
    public ResponseEntity<Map<String, String>> handleDnsFailure(UnknownHostException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Database unavailable: DNS resolution failed for MongoDB Atlas host.");
        body.put("hint", "Check your internet connection or switch DNS to 8.8.8.8 (Google) and flush DNS: ipconfig /flushdns");
        body.put("host", ex.getMessage());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    /**
     * Fires for any Spring Data connection failure: timeout, socket reset, etc.
     * Wraps MongoSocketReadTimeoutException, MongoSocketWriteException, etc.
     */
    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<Map<String, String>> handleDbConnectionFailure(DataAccessResourceFailureException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Database temporarily unavailable. Please try again in a moment.");
        Throwable cause = ex.getCause();
        if (cause instanceof UnknownHostException) {
            body.put("hint", "DNS resolution failed. Switch your DNS to 8.8.8.8 and run: ipconfig /flushdns");
        } else {
            body.put("hint", "This is often a transient network issue with MongoDB Atlas. Retrying usually resolves it.");
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    // ── Application-Level Errors ─────────────────────────────────────────────

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(BadRequestException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Map<String, String>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint.");
        body.put("supportedMethods", String.join(", ", ex.getSupportedMethods()));
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> {
            String field = ((FieldError) err).getField();
            String msg = err.getDefaultMessage();
            errors.put(field, msg);
        });
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthException(
            org.springframework.security.core.AuthenticationException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", "Invalid email or password");
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    @ExceptionHandler(TrackingNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(TrackingNotFoundException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(InvalidStageTransitionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidStage(InvalidStageTransitionException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflict(ConflictException ex) {
        Map<String, String> body = new HashMap<>();
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleOther(Exception ex) {
        ex.printStackTrace();
        Map<String, String> body = new HashMap<>();
        body.put("error", "Internal server error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}
