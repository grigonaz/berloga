package cz.cvut.fel.berloga.service.exceptions;

public class AuthorizationException extends RuntimeException {

    public AuthorizationException() {
        super("User must be logged");
    }

}
