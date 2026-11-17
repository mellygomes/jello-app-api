package com.jello.jello_app.role.repository;

import com.jello.jello_app.role.model.Moderator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ModeratorRepository extends JpaRepository<Moderator, Long> {
}
