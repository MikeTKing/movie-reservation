package com.moviereservation.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    // ── Validation errors ─────────────────────────────────────────────────────

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = error instanceof FieldError fe ? fe.getField() : error.getObjectName();
            errors.put(field, error.getDefaultMessage());
        });

        return ResponseEntity.badRequest().body(
                errorBody(HttpStatus.BAD_REQUEST, "Validation failed", errors, request));
    }

    // ── Domain exceptions ─────────────────────────────────────────────────────

    @ExceptionHandler(Exceptions.ResourceNotFoundException.class)
    public ResponseEntity<Object> handleNotFound(Exceptions.ResourceNotFoundException ex,
                                                  WebRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(errorBody(HttpStatus.NOT_FOUND, ex.getMessage(), null, req));
    }

    @ExceptionHandler(Exceptions.ConflictException.class)
    public ResponseEntity<Object> handleConflict(Exceptions.ConflictException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(errorBody(HttpStatus.CONFLICT, ex.getMessage(), null, req));
    }

    @ExceptionHandler(Exceptions.BadRequestException.class)
    public ResponseEntity<Object> handleBadRequest(Exceptions.BadRequestException ex,
                                                    WebRequest req) {
        return ResponseEntity.badRequest()
                .body(errorBody(HttpStatus.BAD_REQUEST, ex.getMessage(), null, req));
    }

    @ExceptionHandler(Exceptions.ForbiddenException.class)
    public ResponseEntity<Object> handleForbidden(Exceptions.ForbiddenException ex,
                                                   WebRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(HttpStatus.FORBIDDEN, ex.getMessage(), null, req));
    }

    // ── Spring Security ───────────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException ex, WebRequest req) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(errorBody(HttpStatus.FORBIDDEN, "Access denied", null, req));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Object> handleBadCredentials(BadCredentialsException ex,
                                                        WebRequest req) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(errorBody(HttpStatus.UNAUTHORIZED, "Invalid credentials", null, req));
    }

    // ── Catch-all ─────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAll(Exception ex, WebRequest req) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.internalServerError()
                .body(errorBody(HttpStatus.INTERNAL_SERVER_ERROR,
                        "An unexpected error occurred", null, req));
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private Map<String, Object> errorBody(HttpStatus status, String message,
                                           Object details, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        if (details != null) body.put("details", details);
        return body;
    }
}
