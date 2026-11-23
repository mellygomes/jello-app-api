package com.jello.jello_app.auth.exception;

public class TooManyAttemptsException extends RuntimeException {
    public TooManyAttemptsException() {
        super("Muitas tentativas de login. Aguarde alguns minutos e tente de novo.");
    }
}
