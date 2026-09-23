package com.Rychlewski.DesafioItauFinancia.exception;

public class SameAccountTransferException extends RuntimeException{

    public SameAccountTransferException(String message) {
        super(message);
    }

}
