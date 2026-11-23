package com.jello.jello_app.post.dto;

import com.jello.jello_app.image.dto.ImageResponseDTO;
import com.jello.jello_app.tag.dto.TagResponseDTO;
import com.jello.jello_app.user.dto.UserDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public class PostResponseDTO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private UserDTO user;
    private int likes;
    private int comments;
    private List<ImageResponseDTO> images;
    private List<TagResponseDTO> tags;
    private OffsetDateTime createdAt;
    private boolean likedByMe;
    // NONE, AI ou HUMAN
    private String classification;
    private boolean aiClassified;
    private boolean banned;
    private AiVoteResponseDTO aiVotes;
    private PostAuthorDTO author;
}
