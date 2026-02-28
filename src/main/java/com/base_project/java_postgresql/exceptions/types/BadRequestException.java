package com.base_project.java_postgresql.exceptions.types;

public class BadRequestException extends RuntimeException {

    public BadRequestException() {
        super("Bad request");
    }

    public BadRequestException(String message){
        super(message);
    }
}