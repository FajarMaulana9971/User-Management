package com.base_project.java_postgresql.exceptions.types;

public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("Data not found");
    }

    public NotFoundException(String message){
        super(message);
    }
}