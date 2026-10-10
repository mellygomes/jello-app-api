package com.jello.jello_app.image.exception;

import com.jello.jello_app.common.exception.InfrastructureException;

public class FileReadException extends InfrastructureException {
    public FileReadException(String filename, Throwable cause) {
        super("Falha ao ler o arquivo: " + filename, cause);
    }
}
