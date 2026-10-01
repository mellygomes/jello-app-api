package com.jello.jello_app.image.repository;

import com.jello.jello_app.image.model.UserCover;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserCoverRepository extends JpaRepository<UserCover, Long> {
    Optional<UserCover> findByUpdatedBy(Long userId);
}
