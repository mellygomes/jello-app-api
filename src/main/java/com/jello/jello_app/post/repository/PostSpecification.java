package com.jello.jello_app.post.repository;

import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.model.Post;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PostSpecification {
    public static Specification<Post> withFilter(FilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) return criteriaBuilder.conjunction();

            if (filter.isFilterAi()) {
                predicates.add(criteriaBuilder.equal(root.get("aiClassified"), false));
            }

            if (filter.getTags() != null) {
                filter.getTags().forEach(id ->
                        predicates.add(
                                criteriaBuilder.equal(root.get("tags").get("id"), id)
                        )
                );
            }

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String searchTerm = "%" + filter.getSearch().toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm);
                Predicate contentLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), searchTerm);
                predicates.add(criteriaBuilder.or(titleLike, contentLike));
            }

            if (filter.getInitialDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getInitialDate()));
            }

            if (filter.getFinalDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), filter.getFinalDate()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Post> orderByFollowingUser(List<Long> followingIds) {
        return (root, query, criteriaBuilder) -> {
            if (!followingIds.isEmpty()) {
                Expression<Integer> matchOrder = criteriaBuilder.selectCase()
                        .when(root.get("user").get("id").in(followingIds), 0)
                        .otherwise(1)
                        .as(Integer.class);

                query.orderBy(criteriaBuilder.asc(matchOrder));
            }
            return criteriaBuilder.conjunction();
        };
    }
}
