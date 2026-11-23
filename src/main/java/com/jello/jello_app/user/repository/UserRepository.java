package com.jello.jello_app.user.repository;

import com.jello.jello_app.user.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    Boolean existsByEmail(String email);
    User findByEmail(String email);
    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNot(String username, Long id);

    boolean existsByEmailAndIdNot(String email, Long id);

    List<User> findByBannedTrueOrderByUsernameAsc();

    // Sugestoes: quem eu nao sigo, nao sou eu e nao esta banido
    @Query("""
            SELECT u
            FROM User u
            WHERE u.banned = false
              AND u.id <> :me
              AND NOT EXISTS (SELECT 1 FROM Follow f WHERE f.follower.id = :me AND f.following.id = u.id)
            ORDER BY u.createdAt DESC
            """)
    List<User> findSuggestions(@Param("me") Long me, Pageable pageable);

    @Query("""
            SELECT u
            FROM User u
            WHERE (:includeBanned = true OR u.banned = false)
              AND (LOWER(u.username) LIKE :term
                   OR LOWER(u.firstName) LIKE :term
                   OR LOWER(COALESCE(u.lastName, '')) LIKE :term)
            ORDER BY u.username
            """)
    List<User> searchUsers(@Param("term") String term, @Param("includeBanned") boolean includeBanned, Pageable pageable);
}
