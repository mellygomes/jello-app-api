package com.jello.jello_app.auth.exception;

public class ExpiredTokenException extends InvalidTokenException {
    public ExpiredTokenException(Throwable cause) {
        super("Token expirado", cause);
    }
}
