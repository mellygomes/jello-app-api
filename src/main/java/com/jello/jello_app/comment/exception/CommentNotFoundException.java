package com.jello.jello_app.comment.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class CommentNotFoundException extends ResourceNotFoundException {
    public CommentNotFoundException(Long id) {
        super("Comentário com ID " + id + " não encontrado.");
    }
}
