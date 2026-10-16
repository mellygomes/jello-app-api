package com.jello.jello_app.post.dto;

import com.jello.jello_app.image.dto.ImageResponseDTO;
import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.user.dto.UserDTO;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PostResponseDTO {
    private Long id;
    private String title;
    private String content;
    private UserDTO user;
    private int likes;
    private int comments;
    private List<ImageResponseDTO> images;
    private List<TagResponseDTO> tags;
}
