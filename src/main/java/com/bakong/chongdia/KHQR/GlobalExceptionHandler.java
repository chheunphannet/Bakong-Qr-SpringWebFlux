package com.bakong.chongdia.KHQR;

import java.io.IOException;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

 @ExceptionHandler(MethodArgumentNotValidException.class)
 public ResponseEntity<CheckTransactionResponse> handleValidationExceptions(
         MethodArgumentNotValidException ex) {
     
     String errors = ex.getBindingResult().getAllErrors().stream()
             .map(error -> {
                 String fieldName = ((FieldError) error).getField();
                 String errorMessage = error.getDefaultMessage();
                 return fieldName + ": " + errorMessage;
             })
             .collect(Collectors.joining(", "));
     
     log.error("Validation error: {}", errors);
     
     CheckTransactionResponse response = CheckTransactionResponse.builder()
             .responseCode(ResponseCode.FAIL.getCode())
             .responseMessage(errors)
             .errorCode(5) // Missing required fields
             .data(null)
             .build();
     
     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
 }

 @ExceptionHandler(MissingRequestHeaderException.class)
 public ResponseEntity<CheckTransactionResponse> handleMissingHeader(
         MissingRequestHeaderException ex) {
     
     log.error("Missing header: {}", ex.getHeaderName());
     
     CheckTransactionResponse response = CheckTransactionResponse.builder()
             .responseCode(ResponseCode.FAIL.getCode())
             .responseMessage("Missing required header: " + ex.getHeaderName())
             .errorCode(5) // Missing required fields
             .data(null)
             .build();
     
     return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
 }

 @ExceptionHandler(BakongApiException.class)
 public ResponseEntity<CheckTransactionResponse> handleBakongApiException(
         BakongApiException ex) {
     
     log.error("Bakong API error - HTTP: {}, ErrorCode: {}, Message: {}", 
             ex.getHttpStatusCode(), ex.getErrorCode(), ex.getErrorMessage());
     
     CheckTransactionResponse response = CheckTransactionResponse.builder()
             .responseCode(ResponseCode.FAIL.getCode())
             .responseMessage(ex.getErrorMessage())
             .errorCode(ex.getErrorCode())
             .data(null)
             .build();
     
     HttpStatus httpStatus = HttpStatus.resolve(ex.getHttpStatusCode());
     if (httpStatus == null) {
         httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
     }
     
     return ResponseEntity.status(httpStatus).body(response);
 }

 @ExceptionHandler(Exception.class)
 public ResponseEntity<CheckTransactionResponse> handleGenericException(Exception ex) {
	 
	 if (ex instanceof IOException) {
         return null; // Let the other handler deal with it
     }
     log.error("Unexpected error: ", ex);
     
     CheckTransactionResponse response = CheckTransactionResponse.builder()
             .responseCode(ResponseCode.FAIL.getCode())
             .responseMessage("Internal server error")
             .errorCode(9) // Cannot connect to server
             .data(null)
             .build();
     
     return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
 }
 
	/**
	 * Handle client disconnection gracefully - don't log as error
	 */
	@ExceptionHandler(IOException.class)
	public void handleIOException(IOException ex) {
		// Client disconnected - this is normal for SSE/long-polling
		// Just log at debug level, don't return response (connection is already closed)
		log.debug("Client connection closed: {}", ex.getMessage());
	}

	/**
	 * Handle SSE/async timeout
	 */
	@ExceptionHandler(AsyncRequestTimeoutException.class)
	public void handleAsyncTimeout(AsyncRequestTimeoutException ex) {
		log.debug("Async request timeout: {}", ex.getMessage());
	}
 


}
