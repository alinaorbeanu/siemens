package com.siemens.internship.exception;

import java.time.LocalDateTime;
import org.apache.coyote.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ObjectNotFoundException.class)
    protected ResponseEntity<ApiException> handleNotFoundException(Exception ex) {
        return getResponseEntity(ex, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler({IllegalArgumentException.class, BadRequestException.class})
    protected ResponseEntity<ApiException> handleBadRequestException(RuntimeException exception) {
        return getResponseEntity(exception, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ApiException apiException = new ApiException(HttpStatus.BAD_REQUEST);
        ex.getBindingResult().getAllErrors()
                .stream()
                .filter(FieldError.class::isInstance)
                .map(FieldError.class::cast)
                .forEach((error) -> insertErrorMessages(apiException, error));

        handleLogging(apiException);
        return new ResponseEntity<>(apiException, apiException.getHttpStatus());
    }

    private ResponseEntity<ApiException> getResponseEntity(Exception exception, HttpStatus httpStatus) {
        ApiException apiException = new ApiException(httpStatus);
        apiException.setMessage(exception.getMessage());
        apiException.setTimestamp(LocalDateTime.now());
        handleLogging(apiException);
        return new ResponseEntity<>(apiException, apiException.getHttpStatus());
    }

    private void handleLogging(ApiException apiException) {
        HttpStatus httpStatus = apiException.getHttpStatus();
        String message = apiException.getMessage();
        if (httpStatus.is5xxServerError()) {
            logger.error("An exception occurred with message {} , which will cause a {} response", message, httpStatus);
        } else if (httpStatus.is4xxClientError()) {
            logger.warn("An exception occurred with message  {} , which will cause a {} response", message, httpStatus);
        } else {
            logger.debug("An exception occurred with message {} , which will cause a {} response", message, httpStatus);
        }
    }

    private void insertErrorMessages(ApiException apiException, FieldError errorField) {
        String errorMessage = errorField.getField() + ": " + errorField.getDefaultMessage();
        apiException.setMessage(errorMessage);
        apiException.setTimestamp(LocalDateTime.now());
    }
}
