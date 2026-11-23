package com.jello.jello_app.post.service;

import com.jello.jello_app.comment.repository.CommentRepository;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.dto.ImageResponseDTO;
import com.jello.jello_app.image.mapper.ImageMapper;
import com.jello.jello_app.image.repository.PostImageRepository;
import com.jello.jello_app.post.dto.AiVoteResponseDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.mapper.PostMapper;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostAiVoteRepository;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Component;

import java.util.*;

// Monta o PostResponseDTO com os dados de quem ta vendo (curtida, voto, seguindo...) buscando tudo em lote
@Component
@RequiredArgsConstructor
public class PostResponseAssembler {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PostAiVoteRepository aiVoteRepository;
    private final PostImageRepository imageRepository;
    private final FollowRepository followRepository;

    public PostResponseDTO toResponse(Post post, User viewer) {
        return toResponses(List.of(post), viewer).getFirst();
    }

    public Page<PostResponseDTO> toPage(Page<Post> posts, User viewer) {
        return new PageImpl<>(toResponses(posts.getContent(), viewer), posts.getPageable(), posts.getTotalElements());
    }

    public List<PostResponseDTO> toResponses(List<Post> posts, User viewer) {
        if (posts.isEmpty()) return List.of();

        List<Long> ids = posts.stream().map(Post::getId).toList();

        Map<Long, Integer> likes = toCountMap(postRepository.countLikesByPostIds(ids));
        Map<Long, Integer> comments = toCountMap(commentRepository.countByPostIds(ids));
        Set<Long> liked = new HashSet<>(postRepository.findLikedPostIds(ids, viewer.getId()));
        Set<Long> following = new HashSet<>(followRepository.findFollowingIds(viewer.getId()));

        Map<Long, int[]> voteCounts = new HashMap<>();
        for (Object[] row : aiVoteRepository.countByPostIds(ids)) {
            int[] counts = voteCounts.computeIfAbsent((Long) row[0], key -> new int[2]);
            counts[Boolean.TRUE.equals(row[1]) ? 0 : 1] = ((Number) row[2]).intValue();
        }

        Map<Long, Boolean> myVotes = new HashMap<>();
        for (Object[] row : aiVoteRepository.findUserVotes(ids, viewer.getId())) {
            myVotes.put((Long) row[0], (Boolean) row[1]);
        }

        Map<Long, List<ImageResponseDTO>> images = new HashMap<>();
        for (Object[] row : imageRepository.findIdsByPostIds(ids)) {
            images.computeIfAbsent((Long) row[0], key -> new ArrayList<>()).add(ImageMapper.toImageResponse((Long) row[1]));
        }

        return posts.stream().map(post -> {
            int[] counts = voteCounts.getOrDefault(post.getId(), new int[2]);
            AiVoteResponseDTO votes = new AiVoteResponseDTO(counts[0], counts[1], myVotes.get(post.getId()));

            return PostMapper.toDto(
                    post,
                    likes.getOrDefault(post.getId(), 0),
                    comments.getOrDefault(post.getId(), 0),
                    liked.contains(post.getId()),
                    votes,
                    images.getOrDefault(post.getId(), List.of()),
                    following.contains(post.getUser().getId())
            );
        }).toList();
    }

    private Map<Long, Integer> toCountMap(List<Object[]> rows) {
        Map<Long, Integer> result = new HashMap<>();
        for (Object[] row : rows) {
            result.put((Long) row[0], ((Number) row[1]).intValue());
        }
        return result;
    }
}
