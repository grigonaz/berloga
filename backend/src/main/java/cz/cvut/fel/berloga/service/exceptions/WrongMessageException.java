package cz.cvut.fel.berloga.service.exceptions;

public class WrongMessageException extends RuntimeException {

    public WrongMessageException(String text) {
        super(text);
    }

}
