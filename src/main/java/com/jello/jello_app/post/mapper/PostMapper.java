package com.jello.jello_app.post.mapper;

import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.tag.mapper.TagMapper;
import com.jello.jello_app.user.mapper.UserMapper;

public class PostMapper {
    public static PostResponseDTO toDto(Post post) {
        return PostResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .user(UserMapper.toDto(post.getUser()))
                .tags(post.getTags().stream().map(TagMapper::toDto).toList())
                .build();
    }
}
