package com.techfreak.cartify.exception;

public class ProductAlreadyExistsException extends RuntimeException {
    public ProductAlreadyExistsException(String message){
        super(message);
    }
}
