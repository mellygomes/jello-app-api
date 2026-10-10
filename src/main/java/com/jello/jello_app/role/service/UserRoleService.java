package com.jello.jello_app.role.service;

import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.role.exception.RoleNotFoundException;
import com.jello.jello_app.role.exception.UserAlreadyHasModeratorException;
import com.jello.jello_app.role.exception.UserDoesNotHaveRoleException;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.role.repository.RoleRepository;
import com.jello.jello_app.user.exception.UserNotFoundException;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

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
        return userRepository.save(user);
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
}
