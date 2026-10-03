package com.jello.jello_app.tag.repository;

import com.jello.jello_app.tag.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
}
