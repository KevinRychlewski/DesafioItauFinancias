package com.Rychlewski.DesafioItauFinancia.exception;

public class InsufficientBalanceException extends RuntimeException{

    public InsufficientBalanceException(String message) {
        super(message);
    }

}
