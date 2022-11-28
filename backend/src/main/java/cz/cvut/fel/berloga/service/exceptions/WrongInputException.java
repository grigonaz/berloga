package cz.cvut.fel.berloga.service.exceptions;

public class WrongInputException extends RuntimeException {
    public WrongInputException(String message) {
        super(message);
    }
}
