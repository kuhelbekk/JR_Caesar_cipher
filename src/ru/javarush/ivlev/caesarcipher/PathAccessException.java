package ru.javarush.ivlev.caesarcipher;

public class PathAccessException extends RuntimeException{

    public PathAccessException(String message) {
        super(message);
    }

    public PathAccessException(Throwable cause) {
        super(cause);
    }

    public PathAccessException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
