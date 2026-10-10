package com.jello.jello_app.post.service;

import com.jello.jello_app.auth.service.AuthService;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.service.PostImageService;
import com.jello.jello_app.post.dto.CreatePostRequestDTO;
import com.jello.jello_app.post.dto.PostResponseDTO;
import com.jello.jello_app.post.dto.UpdatePostRequestDTO;
import com.jello.jello_app.post.exception.PostNotFoundException;
import com.jello.jello_app.post.model.Post;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.tag.model.Tag;
import com.jello.jello_app.tag.service.TagService;
import com.jello.jello_app.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostImageService postImageService;

    @Mock
    private AuthService authService;

    @Mock
    private PostRepository postRepository;

    @Mock
    private FollowRepository followRepository;

    @Mock
    private TagService tagService;

    @InjectMocks
    private PostService postService;

    private CreatePostRequestDTO request;
    private UpdatePostRequestDTO updateRequest;
    private User user;
    private Post post;
    private List<MultipartFile> files;

    @BeforeEach
    void setUp() {
        request = createRequest();
        updateRequest = createUpdateRequest();
        user = createUser();
        files = createFiles();
        post = createPost();
    }

    // Teste para simular criação de post com fluxo normal
    @Test
    void shouldCreatePost() {
        List<Tag> tags = List.of(
                new Tag(1L, "Tag 1", "#123123", Collections.emptySet()),
                new Tag(2L, "Tag 2", "#123123", Collections.emptySet())
        );

        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(tagService.getAllTagsById(request.getTagIds())).thenReturn(tags);
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post post = invocation.getArgument(0);
            post.setId(1L);
            return post;
        });

        PostResponseDTO result = postService.createPost(request, files);

        assertEquals(request.getTitle(), result.getTitle());

        verify(authService, times(1)).getAuthenticatedUser();
        verify(postImageService, times(1)).saveImageForPost(any(), any());
        verify(postRepository, times(1)).save(any());
    }

    // Teste para simular criação de post com dados invalidos
    @Test
    void shouldFailWhenCreatePostWithInvalidData() {
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(postRepository.save(any(Post.class))).thenThrow(new RuntimeException("Erro ao criar post!"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> postService.createPost(request, null)
        );

        assertTrue(exception.getMessage().contains("Erro ao criar post!"));

        verify(authService, times(1)).getAuthenticatedUser();
        verify(postRepository, times(1)).save(any());
        verifyNoInteractions(postImageService);
    }

    // Teste para recuperar post pelo ID
    @Test
    void shouldGetPostById() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        Post result = postService.getPostById(1L);

        assertEquals(post.getTitle(), result.getTitle());

        verify(postRepository, times(1)).findById(anyLong());
    }

    // Teste para tentar recuperar post por ID quando ID é invalido
    @Test
    void shouldFailWhenGetPostWithInvalidId() {
        Long postId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.getPostById(postId)
        );

        assertEquals("Post com ID 1 não encontrado.", exception.getMessage());
        verify(postRepository, times(1)).findById(anyLong());
    }

    // Teste para simular deleção de post por ID
    @Test
    void shouldDeletePostById() {
        Long postId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));

        postService.deletePost(postId);

        verify(postRepository, times(1)).findById(anyLong());
        verify(postRepository, times(1)).delete(any(Post.class));
    }

    // Teste para simular deleção falha de post por ID invalido
    @Test
    void shouldFailWhenDeletePostById() {
        Long postId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        PostNotFoundException exception = assertThrows(
                PostNotFoundException.class,
                () -> postService.deletePost(postId)
        );

        assertEquals("Post com ID 1 não encontrado.", exception.getMessage());

        verify(postRepository, times(1)).findById(anyLong());
        verify(postRepository, times(0)).delete(any(Post.class));
    }

    // Teste para simular atualizacao de post por ID
    @Test
    void shouldUpdatePostById() {
        Long postId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            savedPost.setId(2L);
            return savedPost;
        });

        Post result = postService.updatePost(updateRequest, postId);

        assertEquals(post.getTitle(), result.getTitle());
        verify(postRepository, times(1)).findById(anyLong());
        verify(postRepository, times(1)).save(any());
    }

    // Teste para simular atualizacao de post por ID invalido
    @Test
    void shouldFailWhenUpdatePostWithInvalidId() {
        Long postId = 1L;

        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(PostNotFoundException.class, () -> postService.updatePost(updateRequest, postId));

        assertEquals("Post com ID 1 não encontrado.", exception.getMessage());

        verify(postRepository, times(1)).findById(anyLong());
        verify(postRepository, never()).save(any());
    }

    // Teste para recuperar os posts do feed
    @Test
    void shouldGetFeedPosts() {
        Page<Post> posts = new PageImpl<>(List.of(post));

        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(followRepository.findUsersFollowedBy(user.getId())).thenReturn(List.of());
        when(postRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(posts);

        Page<PostResponseDTO> result = postService.findPosts(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());

        verify(authService, times(1)).getAuthenticatedUser();
        verify(followRepository, times(1)).findUsersFollowedBy(user.getId());
        verify(postRepository, times(1)).findAll(any(Specification.class), any(Pageable.class));
    }

    // Teste para simular caminho falho ao tentar recuperar posts do feed
    @Test
    void shouldFailWhenGetFeedPosts() {
        when(authService.getAuthenticatedUser()).thenReturn(user);
        when(followRepository.findUsersFollowedBy(user.getId()))
                .thenThrow(new RuntimeException("Erro ao recuperar usuários seguidos"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> postService.findPosts(null, 0, 10)
        );

        assertEquals(
                "Erro ao recuperar usuários seguidos",
                exception.getMessage()
        );

        verify(authService, times(1)).getAuthenticatedUser();
        verify(followRepository, times(1)).findUsersFollowedBy(user.getId());
        verifyNoInteractions(postRepository);
    }

    private CreatePostRequestDTO createRequest() {
        CreatePostRequestDTO request = new CreatePostRequestDTO();
        request.setTitle("Post Mega Foda");
        request.setContent("Descrição do post insana");
        request.setTagIds(Set.of(1L, 2L));
        return request;
    }

    private User createUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("nick");
        user.setEmail("email@fake.com");
        return user;
    }

    private List<MultipartFile> createFiles() {
        MultipartFile file1 = new MockMultipartFile(
                "file1",
                "foto-um.png",
                "image/png",
                "conteudo mesmo".getBytes()
        );

        MultipartFile file2 = new MockMultipartFile(
                "file2",
                "foto-dois.png",
                "image/png",
                "conteudo 2 mesmo".getBytes()
        );
        return List.of(file1, file2);
    }

    private Post createPost() {
        Post post = new Post();
        post.setId(2L);
        post.setTitle("Titulo post teste");
        post.setContent("Conteudo do post");
        post.setUser(user);
        return post;
    }

    private UpdatePostRequestDTO createUpdateRequest() {
        UpdatePostRequestDTO request = new UpdatePostRequestDTO();
        request.setTitle("Post Mega Foda");
        request.setContent("Descrição do post insana");
        return request;
    }
}