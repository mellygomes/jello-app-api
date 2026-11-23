package com.jello.jello_app.auth.exception;

public class BannedUserException extends RuntimeException {
    public BannedUserException() {
        super("Sua conta foi banida.");
    }
}
