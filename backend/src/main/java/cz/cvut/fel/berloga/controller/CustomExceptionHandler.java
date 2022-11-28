package cz.cvut.fel.berloga.controller;

import cz.cvut.fel.berloga.service.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CustomExceptionHandler {

    @ExceptionHandler(PermissionException.class)
    public ResponseEntity<?> permissionException(PermissionException permissionException) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(permissionException.getMessage());
    }

    @ExceptionHandler(AuthorizationException.class)
    public ResponseEntity<?> authorizationException(AuthorizationException authorizationException) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(authorizationException.getMessage());
    }

    @ExceptionHandler(DoesNotExistException.class)
    public ResponseEntity<?> notExistException(DoesNotExistException doesNotExistException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(doesNotExistException.getMessage());
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<?> nullPointerException(NullPointerException nullPointerException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(nullPointerException.getMessage() == null ?
                "Null pointer Exception" : nullPointerException.getMessage());
    }

    @ExceptionHandler(UserAlreadyInChatException.class)
    public ResponseEntity<?> userAlreadyExistException(UserAlreadyInChatException userAlreadyInChatException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userAlreadyInChatException.getMessage());
    }

    @ExceptionHandler(WrongInputException.class)
    public ResponseEntity<?> wrongInputException(WrongInputException wrongInputException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(wrongInputException.getMessage());
    }

    @ExceptionHandler(UserException.class)
    public ResponseEntity<?> userException(UserException userException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(userException.getMessage());
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<?> loginException(LoginException loginException) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(loginException.getMessage());
    }

    @ExceptionHandler(AlreadyExistException.class)
    public ResponseEntity<?> alreadyExistException(AlreadyExistException alreadyExistException) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(alreadyExistException.getMessage());
    }

    @ExceptionHandler(BerlogaException.class)
    public ResponseEntity<?> berlogaException(BerlogaException berlogaException) {
        return ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(berlogaException.getMessage());
    }

    @ExceptionHandler(WrongMessageException.class)
    public ResponseEntity<?> wrongMessageException(WrongMessageException wrongMessageException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(wrongMessageException.getMessage());
    }
}
