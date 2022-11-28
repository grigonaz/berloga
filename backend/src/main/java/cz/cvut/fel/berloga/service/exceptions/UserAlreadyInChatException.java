package cz.cvut.fel.berloga.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class UserAlreadyInChatException extends RuntimeException{
    public UserAlreadyInChatException(String message) {
        super(message);
    }
}
