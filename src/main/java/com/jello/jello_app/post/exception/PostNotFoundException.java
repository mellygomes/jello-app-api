package com.jello.jello_app.post.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

public class PostNotFoundException extends ResourceNotFoundException {
    public PostNotFoundException(Long id) {
        super("Post com ID " + id + " não encontrado.");
    }
}
