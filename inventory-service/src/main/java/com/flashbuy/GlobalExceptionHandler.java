package com.flashbuy.inventory_service;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "BAD_REQUEST", "message", ex.getMessage()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "BAD_REQUEST", "message", "Body inválido o mal formado"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException ex) {
        String message = ex.getMessage();

        if (message != null && message.startsWith("Producto no encontrado")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "message", message));
        }
        if (message != null && message.startsWith("INSUFFICIENT_STOCK")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "INSUFFICIENT_STOCK", "message", "Stock insuficiente"));
        }
        if (message != null && message.startsWith("LOCK_BUSY")) {
            return ResponseEntity.status(HttpStatus.LOCKED)
                    .body(Map.of("error", "LOCK_BUSY", "message", "Producto siendo modificado"));
        }

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "INTERNAL_ERROR", "message", message));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleTransaction(TransactionSystemException ex) {
        Throwable cause = ex.getRootCause();
        if (cause != null && cause.getMessage() != null) {
            String message = cause.getMessage();
            if (message.startsWith("Producto no encontrado")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "NOT_FOUND", "message", message));
            }
            if (message.startsWith("INSUFFICIENT_STOCK")) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "INSUFFICIENT_STOCK", "message", "Stock insuficiente"));
            }
            if (message.startsWith("LOCK_BUSY")) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(Map.of("error", "LOCK_BUSY", "message", "Producto siendo modificado"));
            }
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "INTERNAL_ERROR", "message", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneral(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "INTERNAL_ERROR", "message", ex.getMessage()));
    }
}