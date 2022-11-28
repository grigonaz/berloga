package cz.cvut.fel.berloga.service.exceptions;

public class DoesNotExistException extends RuntimeException {
    public DoesNotExistException(String text) {
        super(text);
    }
}
