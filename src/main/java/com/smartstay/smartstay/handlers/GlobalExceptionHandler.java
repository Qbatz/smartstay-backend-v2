package com.smartstay.smartstay.handlers;

import com.smartstay.smartstay.Exceptions.SmartStayException;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleMethodValidationException(MethodArgumentNotValidException manv, WebRequest request) {
        String errorMessage = manv.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation failed");
        return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<?> handleIllegalException(IllegalArgumentException ex) {
//        return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
//    }
//
//
//    @ExceptionHandler(RuntimeException.class)
//    public ResponseEntity<?> handleRuntimeException(RuntimeException ex) {
//
//        return new ResponseEntity<>("Something went wrong", HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(SignatureException.class)
    public ResponseEntity<?> handleSignatureMismatchException(SignatureException se) {
        return new ResponseEntity<>("Something went wrong. Please login again", HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(SmartStayException.class)
    public ResponseEntity<?> handleLogoutException(SmartStayException sse) {
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

}
