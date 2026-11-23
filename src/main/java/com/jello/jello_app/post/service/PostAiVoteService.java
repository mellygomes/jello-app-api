package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.common.exception.BusinessException;
import com.jello.jello_app.post.dto.AiVoteResponseDTO;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.model.PostAiVote;
import com.jello.jello_app.post.repository.PostAiVoteRepository;
import com.jello.jello_app.user.model.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostAiVoteService {

    private final PostAiVoteRepository aiVoteRepository;
    private final PostService postService;
    private final AuthService authService;

    // Registra (ou troca) o voto do usuario: true = acha que e IA, false = acha que e arte humana
    @Transactional
    public AiVoteResponseDTO vote(Long postId, boolean ai) {
        User user = authService.getAuthenticatedUser();
        Post post = postService.getVisiblePost(postId, user);

        if (post.getUser().getId().equals(user.getId())) {
            throw new BusinessException("Você não pode votar na sua própria postagem.");
        }

        PostAiVote vote = aiVoteRepository.findByPostIdAndUserId(postId, user.getId())
                .orElseGet(() -> {
                    PostAiVote newVote = new PostAiVote();
                    newVote.setPost(post);
                    newVote.setUser(user);
                    return newVote;
                });

        vote.setVoteAi(ai);
        aiVoteRepository.save(vote);

        return summarize(postId, ai);
    }

    @Transactional
    public AiVoteResponseDTO removeVote(Long postId) {
        User user = authService.getAuthenticatedUser();
        postService.getVisiblePost(postId, user);

        aiVoteRepository.deleteByUserIdAndPostId(user.getId(), postId);

        return summarize(postId, null);
    }

    public AiVoteResponseDTO getVotes(Long postId) {
        User user = authService.getAuthenticatedUser();
        postService.getVisiblePost(postId, user);

        Boolean myVote = aiVoteRepository.findByPostIdAndUserId(postId, user.getId())
                .map(PostAiVote::isVoteAi)
                .orElse(null);

        return summarize(postId, myVote);
    }

    private AiVoteResponseDTO summarize(Long postId, Boolean myVote) {
        return AiVoteResponseDTO.builder()
                .ai((int) aiVoteRepository.countByPostIdAndVoteAi(postId, true))
                .human((int) aiVoteRepository.countByPostIdAndVoteAi(postId, false))
                .myVote(myVote)
                .build();
    }
}
