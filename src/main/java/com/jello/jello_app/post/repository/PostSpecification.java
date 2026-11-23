package com.jello.jello_app.post.repository;

import com.jello.jello_app.enumeration.PostSort;
import com.jello.jello_app.post.dto.FilterRequestDTO;
import com.jello.jello_app.post.model.Post;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import com.jello.jello_app.tag.model.Tag;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class PostSpecification {
    public static Specification<Post> withFilter(FilterRequestDTO filter) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter == null) return criteriaBuilder.conjunction();

            if (filter.isFilterAi()) {
                predicates.add(criteriaBuilder.equal(root.get("aiClassified"), false));
            }

            // tag via subquery: nao duplica linha e deixa a ordenacao livre (sem DISTINCT)
            if (filter.getTags() != null && !filter.getTags().isEmpty()) {
                Subquery<Long> sub = query.subquery(Long.class);
                Root<Post> tagged = sub.from(Post.class);
                Join<Post, Tag> tags = tagged.join("tags");
                sub.select(tagged.get("id"))
                        .where(criteriaBuilder.equal(tagged.get("id"), root.get("id")), tags.get("id").in(filter.getTags()));
                predicates.add(criteriaBuilder.exists(sub));
            }

            if (filter.getSearch() != null && !filter.getSearch().isBlank()) {
                String search = filter.getSearch().trim();
                if (search.length() > 100) search = search.substring(0, 100);
                String searchTerm = "%" + search.toLowerCase() + "%";
                Predicate titleLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), searchTerm);
                Predicate contentLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("content")), searchTerm);
                predicates.add(criteriaBuilder.or(titleLike, contentLike));
            }

            // createdAt e LocalDateTime, entao a data vira inicio do dia (e o fim vai ate o comeco do dia seguinte)
            if (filter.getInitialDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), filter.getInitialDate().atStartOfDay()));
            }

            if (filter.getFinalDate() != null) {
                predicates.add(criteriaBuilder.lessThan(root.get("createdAt"), filter.getFinalDate().plusDays(1).atStartOfDay()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Ordenacao escolhida pelo usuario. Sempre desempata por data e id pra paginacao nao repetir post.
    // (o Pageable vai sem Sort, senao o Spring Data sobrescreve esse orderBy)
    public static Specification<Post> sortedBy(PostSort sort) {
        return (root, query, criteriaBuilder) -> {
            if (query.getResultType() == Long.class || query.getResultType() == long.class) {
                return criteriaBuilder.conjunction();
            }

            List<Order> orders = new ArrayList<>();
            PostSort safeSort = sort == null ? PostSort.RECENT : sort;

            if (safeSort == PostSort.POPULAR) {
                orders.add(criteriaBuilder.desc(criteriaBuilder.size(root.<Collection<?>>get("likes"))));
            } else if (safeSort == PostSort.COMMENTED) {
                orders.add(criteriaBuilder.desc(criteriaBuilder.size(root.<Collection<?>>get("comments"))));
            }

            if (safeSort == PostSort.OLDEST) {
                orders.add(criteriaBuilder.asc(root.get("createdAt")));
                orders.add(criteriaBuilder.asc(root.get("id")));
            } else {
                orders.add(criteriaBuilder.desc(root.get("createdAt")));
                orders.add(criteriaBuilder.desc(root.get("id")));
            }

            query.orderBy(orders);
            return criteriaBuilder.conjunction();
        };
    }

    public static Specification<Post> byUserId(Long userId) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    // Esconde posts banidos e posts de usuarios banidos (pra quem nao e moderador)
    public static Specification<Post> onlyVisible() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get("banned"), false),
                criteriaBuilder.equal(root.get("user").get("banned"), false)
        );
    }
}
