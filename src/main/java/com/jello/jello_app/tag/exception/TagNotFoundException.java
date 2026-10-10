package com.jello.jello_app.tag.exception;

import com.jello.jello_app.common.exception.ResourceNotFoundException;

import java.util.Set;

public class TagNotFoundException extends ResourceNotFoundException {
    public TagNotFoundException(Set<Long> ids) {
        super("Tags não encontradas: " + ids);
    }
}
