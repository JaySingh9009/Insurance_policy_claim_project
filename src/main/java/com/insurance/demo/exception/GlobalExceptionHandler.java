package com.insurance.demo.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse>
    handleNotFound(
            ResourceNotFoundException ex){

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                .timestamp(
                        LocalDateTime.now())
                .status(404)
                .error("NOT_FOUND")
                .message(
                        ex.getMessage())
                .build();

        return ResponseEntity
                .status(
                        HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(
            BadRequestException.class)
    public ResponseEntity<ApiErrorResponse>
    handleBadRequest(
            BadRequestException ex){

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                .timestamp(
                        LocalDateTime.now())
                .status(400)
                .error("BAD_REQUEST")
                .message(
                        ex.getMessage())
                .build();

        return ResponseEntity
                .badRequest()
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse>
    handleException(
            Exception ex){

        ApiErrorResponse response =
                ApiErrorResponse.builder()
                .timestamp(
                        LocalDateTime.now())
                .status(500)
                .error(
                        "INTERNAL_SERVER_ERROR")
                .message(
                        ex.getMessage())
                .build();

        return ResponseEntity
                .status(500)
                .body(response);
    }
}