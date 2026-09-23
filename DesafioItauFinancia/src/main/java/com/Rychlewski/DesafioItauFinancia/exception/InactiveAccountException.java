package com.Rychlewski.DesafioItauFinancia.exception;

public class InactiveAccountException extends RuntimeException{

    public InactiveAccountException(String message) {
        super(message);
    }

}
