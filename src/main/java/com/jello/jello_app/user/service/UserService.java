package com.jello.jello_app.user.service;

import com.jello.jello_app.auth.dto.RegisterRequestDTO;
import com.jello.jello_app.common.exception.BusinessException;
import com.jello.jello_app.confirmation.model.Confirmation;
import com.jello.jello_app.confirmation.repository.ConfirmationRepository;
import com.jello.jello_app.domain.RequestContext;
import com.jello.jello_app.enumeration.EventType;
import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.event.UserEvent;
import com.jello.jello_app.follow.dto.FollowStatsDTO;
import com.jello.jello_app.follow.repository.FollowRepository;
import com.jello.jello_app.image.model.UserAvatar;
import com.jello.jello_app.image.model.UserCover;
import com.jello.jello_app.image.service.UserAvatarService;
import com.jello.jello_app.image.service.UserCoverService;
import com.jello.jello_app.post.repository.PostRepository;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.role.service.UserRoleService;
import com.jello.jello_app.user.dto.ChangePasswordRequestDTO;
import com.jello.jello_app.user.dto.ProfileDTO;
import com.jello.jello_app.user.dto.UpdateUserRequestDTO;
import com.jello.jello_app.user.dto.UserSummaryDTO;
import com.jello.jello_app.user.exception.UserAlreadyExistsException;
import com.jello.jello_app.user.exception.UserNotFoundException;
import com.jello.jello_app.user.mapper.UserMapper;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final int MAX_LIST_SIZE = 20;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleService userRoleService;
    private final ApplicationEventPublisher publisher;
    private final ConfirmationRepository confirmationRepository;
    private final UserAvatarService userAvatarService;
    private final PostRepository postRepository;
    private final FollowRepository followRepository;
    private final UserCoverService userCoverService;

    public User register(RegisterRequestDTO request) {
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
        FollowStatsDTO followStats = FollowStatsDTO.builder()
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .build();

        return UserMapper.toProfileDto(user, postsCount, followStats);
    }

    // Perfil visto por outro usuario: e-mail so pro dono e moderador, banido so moderador enxerga
    public ProfileDTO getUserProfile(Long userId, Long viewerId) {
        User viewer = getUserById(viewerId);
        User user = getVisibleUser(userId, viewer);

        boolean self = viewer.getId().equals(user.getId());
        boolean viewerIsModerator = isModerator(viewer);

        long postsCount = viewerIsModerator
                ? postRepository.countByUserId(userId)
                : postRepository.countByUserIdAndBannedFalse(userId);

        FollowStatsDTO followStats = FollowStatsDTO.builder()
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .build();

        ProfileDTO profile = UserMapper.toProfileDto(user, postsCount, followStats);
        profile.setFollowedByMe(!self && followRepository.existsByFollowerAndFollowing(viewer, user));

        if (!self && !viewerIsModerator) {
            profile.setEmail(null);
        }

        return profile;
    }

    public ProfileDTO updateUser(UpdateUserRequestDTO request, Long userId) {
        if (userRepository.existsByUsernameAndIdNot(request.getUsername(), userId)
                || userRepository.existsByEmailAndIdNot(request.getEmail(), userId)) {
            throw new UserAlreadyExistsException(request.getUsername(), request.getEmail());
        }

        User user = userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.setFirstName(request.getFirstName());
                    existingUser.setLastName(request.getLastName());
                    existingUser.setEmail(request.getEmail());
                    existingUser.setBio(request.getBio());
                    existingUser.setUsername(request.getUsername());
                    if (request.getAvatar() != null) {
                        // usuarios antigos podem nao ter avatar ainda, entao cria um padrao antes de trocar
                        UserAvatar currentAvatar = existingUser.getProfilePicture() != null
                                ? existingUser.getProfilePicture()
                                : userAvatarService.createUserAvatar();
                        existingUser.setProfilePicture(userAvatarService.updateUserAvatar(currentAvatar, request.getAvatar()));
                    }
                    if (request.getCover() != null) {
                        UserCover currentCover = existingUser.getProfileCover() != null
                                ? existingUser.getProfileCover()
                                : userCoverService.createUserCover();
                        existingUser.setProfileCover(userCoverService.updateUserCover(currentCover, request.getCover()));
                    }
                    return existingUser;
                })
                .orElseThrow(() -> new UserNotFoundException(userId));

        long postsCount = postRepository.countByUserId(userId);
        FollowStatsDTO followStats = FollowStatsDTO.builder()
                .followersCount(followRepository.countByFollowingId(userId))
                .followingCount(followRepository.countByFollowerId(userId))
                .build();

        try {
            User savedUser = userRepository.save(user);
            return UserMapper.toProfileDto(savedUser, postsCount, followStats);
        } catch (DataIntegrityViolationException ex) {
            throw new UserAlreadyExistsException(request.getUsername(), request.getEmail());
        }
    }

    // Apaga a conta (posts, comentarios, likes, follows, votos e denuncias saem por cascade no banco)
    @Transactional
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        UserAvatar avatar = user.getProfilePicture();
        UserCover cover = user.getProfileCover();

        userRepository.delete(user);
        userRepository.flush();

        // foto e capa ficam soltas depois que o usuario some, entao apaga junto
        userAvatarService.deleteAvatar(avatar);
        userCoverService.deleteCover(cover);
    }

    public void changePassword(Long userId, ChangePasswordRequestDTO request) {
        User user = getUserById(userId);

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("A senha atual está incorreta.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public List<UserSummaryDTO> getSuggestions(Long viewerId, int limit) {
        int size = Math.min(Math.max(limit, 1), MAX_LIST_SIZE);
        return toSummaries(userRepository.findSuggestions(viewerId, PageRequest.of(0, size)), viewerId);
    }

    public List<UserSummaryDTO> searchUsers(String term, Long viewerId) {
        // tira os curingas do LIKE pra busca ser sempre por texto simples
        String clean = term == null ? "" : term.replaceAll("[%_\\\\]", "").trim().toLowerCase();
        if (clean.isEmpty()) return List.of();
        if (clean.length() > 50) clean = clean.substring(0, 50);

        boolean viewerIsModerator = isModerator(getUserById(viewerId));
        List<User> found = userRepository.searchUsers("%" + clean + "%", viewerIsModerator, PageRequest.of(0, MAX_LIST_SIZE));

        return toSummaries(found, viewerId);
    }

    public List<UserSummaryDTO> getFollowers(Long userId, Long viewerId) {
        getVisibleUser(userId, getUserById(viewerId));
        return toSummaries(followRepository.findFollowersOf(userId), viewerId);
    }

    public List<UserSummaryDTO> getFollowing(Long userId, Long viewerId) {
        getVisibleUser(userId, getUserById(viewerId));
        return toSummaries(followRepository.findUsersFollowedBy(userId), viewerId);
    }

    public void banUser(Long targetId, Long actorId) {
        User target = getUserById(targetId);

        if (target.getId().equals(actorId)) {
            throw new BusinessException("Você não pode banir a si mesmo.");
        }
        if (isModerator(target)) {
            throw new BusinessException("Moderadores não podem ser banidos.");
        }

        target.setBanned(true);
        userRepository.save(target);
    }

    public void unbanUser(Long targetId) {
        User target = getUserById(targetId);
        target.setBanned(false);
        userRepository.save(target);
    }

    public List<UserSummaryDTO> getBannedUsers(Long viewerId) {
        return toSummaries(userRepository.findByBannedTrueOrderByUsernameAsc(), viewerId);
    }

    // Banido so aparece pra moderador (ou pra ele mesmo)
    private User getVisibleUser(Long userId, User viewer) {
        User user = getUserById(userId);

        if (user.isBanned() && !isModerator(viewer) && !user.getId().equals(viewer.getId())) {
            throw new UserNotFoundException(userId);
        }

        return user;
    }

    private List<UserSummaryDTO> toSummaries(List<User> users, Long viewerId) {
        Set<Long> followingIds = new HashSet<>(followRepository.findFollowingIds(viewerId));
        boolean viewerIsModerator = isModerator(getUserById(viewerId));

        return users.stream()
                .filter(user -> viewerIsModerator || !user.isBanned())
                .map(user -> UserMapper.toSummaryDto(user, followingIds.contains(user.getId())))
                .toList();
    }

    private boolean isModerator(User user) {
        return user.hasRole(RoleType.ROLE_MODERATOR.getName());
    }

    private User createUser(Role roleUser, RegisterRequestDTO request) {
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
