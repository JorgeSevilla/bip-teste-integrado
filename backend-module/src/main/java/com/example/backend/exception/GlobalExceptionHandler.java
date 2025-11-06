package com.example.backend.exception;

import jakarta.ejb.EJBException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EJBException.class)
    public ResponseEntity<Map<String, String>> handleEJBException(EJBException ex, WebRequest request) {

        String errorMessage = "Erro de negócio: ";

        if (ex.getCause() != null) {
            errorMessage = ex.getCause().getMessage();
        } else {
            errorMessage = ex.getMessage();
        }

        if (errorMessage.contains("não encontrado")) {
            return new ResponseEntity<>(Map.of("erro", errorMessage), HttpStatus.NOT_FOUND);
        }

        return new ResponseEntity<>(Map.of("erro", errorMessage), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return new ResponseEntity<>(Map.of("erro", errors), HttpStatus.BAD_REQUEST);
    }

}
