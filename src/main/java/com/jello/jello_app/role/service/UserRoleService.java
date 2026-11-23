package com.jello.jello_app.role.service;

import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.role.dto.ModeratorResponseDTO;
import com.jello.jello_app.role.exception.ModeratorNotFoundException;
import com.jello.jello_app.role.exception.RoleNotFoundException;
import com.jello.jello_app.role.exception.UserAlreadyHasModeratorException;
import com.jello.jello_app.role.exception.UserDoesNotHaveRoleException;
import com.jello.jello_app.role.mapper.ModeratorMapper;
import com.jello.jello_app.role.model.Moderator;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.role.repository.ModeratorRepository;
import com.jello.jello_app.role.repository.RoleRepository;
import com.jello.jello_app.user.exception.UserNotFoundException;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ModeratorRepository moderatorRepository;

    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new RoleNotFoundException(name));
    }

    public User grantModerator(Long userId) {
        Role moderatorRole = getRoleByName(RoleType.ROLE_MODERATOR.getName());

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        boolean hasModerator = user.getRoles().stream().anyMatch(role -> role.getName().equals(moderatorRole.getName()));

        if (hasModerator) {
            throw new UserAlreadyHasModeratorException(user.getUsername());
        }

        user.getRoles().add(moderatorRole);
        User savedUser = userRepository.save(user);
        ensureModeratorRow(user);
        return savedUser;
    }

    public User revokeModerator(Long userId) {
        Role moderatorRole = getRoleByName(RoleType.ROLE_MODERATOR.getName());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        boolean removedModerator = user
                .getRoles()
                .removeIf(role -> role.getName().equals(moderatorRole.getName()));

        if (!removedModerator) {
            throw new UserDoesNotHaveRoleException(userId, moderatorRole.getName());
        }

        return userRepository.save(user);
    }

    // Primeiro moderador que ainda tem o cargo (os reports caem pra ele)
    public Moderator getModerator() {
        return moderatorRepository.findActiveModerators(RoleType.ROLE_MODERATOR.getName())
                .stream()
                .findFirst()
                .orElseThrow(ModeratorNotFoundException::new);
    }

    // Igual ao getModerator, mas sem explodir: denuncia pode ficar sem analista ate alguem assumir
    public Optional<Moderator> findModerator() {
        return moderatorRepository.findActiveModerators(RoleType.ROLE_MODERATOR.getName())
                .stream()
                .findFirst();
    }

    // Linha de tb_moderator do usuario logado, cria se ainda nao existir
    public Moderator getModeratorByUser(User user) {
        return moderatorRepository.findByUserId(user.getId())
                .orElseGet(() -> createModeratorRow(user));
    }

    private void ensureModeratorRow(User user) {
        if (moderatorRepository.findByUserId(user.getId()).isEmpty()) {
            createModeratorRow(user);
        }
    }

    private Moderator createModeratorRow(User user) {
        Moderator moderator = new Moderator();
        moderator.setUser(user);
        return moderatorRepository.save(moderator);
    }
}
