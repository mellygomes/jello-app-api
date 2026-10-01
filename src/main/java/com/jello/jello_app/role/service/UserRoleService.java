package com.jello.jello_app.role.service;

import com.jello.jello_app.enumeration.RoleType;
import com.jello.jello_app.role.model.Role;
import com.jello.jello_app.role.repository.RoleRepository;
import com.jello.jello_app.user.model.User;
import com.jello.jello_app.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserRoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public User grantModerator(Long userId) {
        Role moderatorRole = roleRepository.findByName(RoleType.ROLE_MODERATOR.getName())
                .orElseThrow(() -> new RuntimeException("ROLE_MODERATOR não encontrado!"));
        return userRepository.findById(userId)
                .map(existingUser -> {
                    existingUser.getRoles().add(moderatorRole);
                    return userRepository.save(existingUser);
                })
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado!"));
    }

    public User revokeModerator(Long userId) {
        Role moderatorRole = roleRepository.findByName(RoleType.ROLE_MODERATOR.getName())
                .orElseThrow(() -> new RuntimeException("ROLE_MODERATOR não encontrado!"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        boolean removedModerator = user.getRoles().removeIf(role -> role.getName().equals(moderatorRole.getName()));

        if (!removedModerator) {
            throw new RuntimeException("Usuário nao possui o cargo de MODERADOR!");
        }

        return userRepository.save(user);
    }
}
