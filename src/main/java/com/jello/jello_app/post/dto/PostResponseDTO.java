package com.jello.jello_app.post.dto;

import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.tag.model.Tag;
import com.jello.jello_app.user.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private UserDTO user;
    private List<TagResponseDTO> tags;
}
