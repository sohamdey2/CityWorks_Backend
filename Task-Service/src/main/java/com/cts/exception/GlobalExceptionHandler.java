package com.cts.exception;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.cts.api.ApiResponse;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(
            ResourceNotFoundException ex) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", java.time.LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "NOT_FOUND");
        response.put("message", ex.getMessage());

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValidException(
			MethodArgumentNotValidException ex) {

		Map<String, String> fieldErrors = new HashMap<>();

		ex.getBindingResult().getFieldErrors().forEach(error -> {
			fieldErrors.put(error.getField(), error.getDefaultMessage());
		});

		Map<String, Object> response = new HashMap<>();
		response.put("timestamp", java.time.LocalDateTime.now());
		response.put("status", HttpStatus.BAD_REQUEST.value());
		response.put("error", "INVALID_OPERATION");
		response.put("message", fieldErrors);

		return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
	}    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Map<String,String>>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.<Map<String,String>>builder()
                		.status("Access Denied")
                		.message("Access denied: you do not have permission to perform this action")
                		.build());
    }
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<ApiResponse<Object>> handleInvalidFormat(InvalidFormatException ex) {
        String field = ex.getPath().isEmpty()
                ? "field"
                : ex.getPath().get(ex.getPath().size() - 1).getFieldName();
        return ResponseEntity.badRequest().body(
            ApiResponse.builder()
                .status("ERROR")
                .message(field + " has an invalid value: '" + ex.getValue() + "'")
                .data(null)
                .build()
        );
    }
}
