package com.jello.jello_app.user.service;

import com.jello.jello_app.auth.dto.RegisterRequest;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import com.jello.jello_app.domain.RequestContext;
import com.jello.jello_app.enumeration.EventType;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.event.UserEvent;
import com.jello.jello_app.follow.dto.FollowStats;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.service.UserAvatarService;
import com.jello.jello_app.image.service.UserCoverService;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.post.service.PostService;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.role.repository.RoleRepository;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UpdateUserRequest;
import com.jello.jello_app.user.exception.UserAlreadyExistsException;
import com.jello.jello_app.user.exception.UserNotFoundException;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;
    private final ApplicationEventPublisher publisher;
    private final ConfirmationRepository confirmationRepository;
    private final UserAvatarService userAvatarService;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final UserCoverService userCoverService;

    public User register(RegisterRequest request) {
        Role roleUser = userRoleService.getRoleByName(RoleType.ROLE_USER.getName());
        User user = createUser(roleUser, request);
        try {
            user.setProfileCover(userCoverService.createUserCover());
            user.setProfilePicture(userAvatarService.createUserAvatar());
            User savedUser = userRepository.save(user);

            if (RequestContext.getUserId() == null) {
                user.setCreatedBy(savedUser.getId());
                user.setUpdatedBy(savedUser.getId());
            }

            Confirmation confirmation = new Confirmation((savedUser));
            confirmationRepository.save(confirmation);

            publisher.publishEvent(new UserEvent(savedUser, EventType.REGISTRATION, Map.of("key", confirmation.getConfirmationKey())));

            return savedUser;
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(user.getUsername(), user.getEmail());
        }
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    public ProfileDTO getUserProfile(Long userId) {
        User user = getUserById(userId);
        long postsCount = postRepository.countByUserId(userId);
        FollowStats followStats = FollowStats.builder()
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .build();

        return UserMapper.toProfileDto(user, postsCount, followStats);
    }

    public ProfileDTO updateUser(UpdateUserRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setFirstName(request.getFirstName());
                    existingUser.setLastName(request.getLastName());
                    existingUser.setEmail(request.getEmail());
                    existingUser.setBio(request.getBio());
                    existingUser.setUsername(request.getUsername());
                    if (request.getAvatar() != null) {
                        existingUser.setProfilePicture(userAvatarService.updateUserAvatar(existingUser.getProfilePicture(), request.getAvatar()));
                    }
                    if (request.getCover() != null) {
                        existingUser.setProfileCover(userCoverService.updateUserCover(existingUser.getProfileCover(), request.getCover()));
                    }
                    return existingUser;
                })
                .orElseThrow(() -> new UserNotFoundException(userId));

        long postsCount = postRepository.countByUserId(userId);
        FollowStats followStats = FollowStats.builder()
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .build();

        User savedUser = userRepository.save(user);

        return UserMapper.toProfileDto(savedUser, postsCount, followStats);
    }

    public void deleteUser(Long userId) {
        userRepository.findById(userId)
                .ifPresentOrElse(userRepository::delete, () -> {
                    throw new UserNotFoundException(userId);
                });
    }

    private User createUser(Role roleUser, RegisterRequest request) {
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRoles(new HashSet<>(Set.of(roleUser)));
        user.setBio(null);
        user.setEnabled(true);
        user.setBanned(false);

        return user;
    }
}
