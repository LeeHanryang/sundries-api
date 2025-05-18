package io.github.leehanryang.sundriesapi.domain.repository;

import io.github.leehanryang.sundriesapi.domain.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<Todo, UUID> {
    List<Todo> findAllByUserIdOrderByCreatedAtDesc(UUID userid);

    List<Todo> findByUserIdAndTitleContaining(UUID userid, String keyword);

}
