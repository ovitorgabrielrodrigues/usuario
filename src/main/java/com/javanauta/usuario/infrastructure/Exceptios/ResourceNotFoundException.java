package com.javanauta.usuario.infrastructure.Exceptios;

public class ResourceNotFoundException extends RuntimeException{

    public ResourceNotFoundException(String mensage){
        super(mensage);
    }

    public ResourceNotFoundException(String mensage, Throwable throwable){
        super(mensage, throwable);
    }
}
