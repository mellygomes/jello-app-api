package com.jello.jello_app.role.repository;

import com.jello.jello_app.role.model.Moderator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ModeratorRepository extends JpaRepository<Moderator, Long> {

    Optional<Moderator> findByUserId(Long userId);

    // So moderadores que ainda tem o cargo
    @Query("SELECT m FROM Moderator m JOIN m.user u JOIN u.roles r WHERE r.name = :roleName ORDER BY m.id")
    List<Moderator> findActiveModerators(@Param("roleName") String roleName);
}
