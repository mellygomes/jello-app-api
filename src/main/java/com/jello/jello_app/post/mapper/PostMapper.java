package com.jello.jello_app.post.mapper;

import com.jello.jello_app.enumeration.PostClassification;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.image.dto.ImageResponseDTO;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.post.dto.AiVoteResponseDTO;
import com.jello.jello_app.post.dto.PostAuthorDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.tag.mapper.TagMapper;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

public class PostMapper {

    // Mapeamento simples, pra post recem criado ou editado (sem dados de quem ta vendo)
    public static PostResponseDTO toDto(Post post) {
        List<ImageResponseDTO> images = post.getImages().stream().map(ImageMapper::toTeste).toList();
        int comments = post.getComments() != null ? post.getComments().size() : 0;

        return toDto(post, post.getLikes().size(), comments, false, new AiVoteResponseDTO(), images, false);
    }

    public static PostResponseDTO toDto(Post post, int likes, int comments, boolean likedByMe,
                                        AiVoteResponseDTO aiVotes, List<ImageResponseDTO> images, boolean following) {
        User author = post.getUser();

        return PostResponseDTO.builder()
                .id(post.getId())
                .userId(author.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .user(UserMapper.toPublicDto(author, following))
                .likes(likes)
                .comments(comments)
                .images(images)
                .tags(post.getTags().stream().map(TagMapper::toDto).toList())
                .createdAt(toOffset(post))
                .likedByMe(likedByMe)
                .classification(PostClassification.of(post).name())
                .aiClassified(post.isAiClassified())
                .banned(post.isBanned())
                .aiVotes(aiVotes)
                .author(PostAuthorDTO.builder()
                        .id(author.getId())
                        .username(author.getUsername())
                        .firstName(author.getFirstName())
                        .lastName(author.getLastName())
                        .avatarId(UserMapper.avatarId(author))
                        .following(following)
                        .moderator(author.hasRole(RoleType.ROLE_MODERATOR.getName()))
                        .build())
                .build();
    }

    private static OffsetDateTime toOffset(Post post) {
        if (post.getCreatedAt() == null) return null;
        // sempre em UTC ("...Z"), o navegador converte pro horario local
        return post.getCreatedAt().atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneOffset.UTC).toOffsetDateTime();
    }
}
