package com.Rychlewski.DesafioItauFinancia.exception;

public class DuplicateCpfException extends RuntimeException{
    public DuplicateCpfException(String message) {
        super(message);
    }
}
