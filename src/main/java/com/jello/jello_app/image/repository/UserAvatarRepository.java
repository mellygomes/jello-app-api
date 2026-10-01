package com.jello.jello_app.image.repository;

import com.jello.jello_app.image.model.UserAvatar;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAvatarRepository extends JpaRepository<UserAvatar, Long> {
    Optional<UserAvatar> findByUpdatedBy(Long userId);
}
