package com.jello.jello_app.post.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CreatePostRequestDTO {
    private String title;
    private String content;
    private Set<Long> tagIds;
}
