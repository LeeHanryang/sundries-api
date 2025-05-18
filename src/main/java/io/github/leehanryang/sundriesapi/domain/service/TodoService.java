package io.github.leehanryang.sundriesapi.domain.service;

import io.github.leehanryang.sundriesapi.common.enums.ErrorCodeEnum;
import io.github.leehanryang.sundriesapi.common.exception.ApiException;
import io.github.leehanryang.sundriesapi.domain.dto.TodoDTO;
import io.github.leehanryang.sundriesapi.domain.entity.Todo;
import io.github.leehanryang.sundriesapi.domain.entity.User;
import io.github.leehanryang.sundriesapi.domain.repository.TodoRepository;
import io.github.leehanryang.sundriesapi.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;

    @Transactional
    public TodoDTO create(UUID userId, TodoDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.USER_NOT_FOUND));

        Todo todo = dto.toEntity(user);
        return todoRepository.save(todo).toDto();
    }

    public List<TodoDTO> findAll(UUID userId) {
        return todoRepository.findAllByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(Todo::toDto)
                .toList();
    }

    public TodoDTO findById(UUID userId, UUID todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.TODO_DETAIL_NOT_FOUND));
        if (!todo.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCodeEnum.ACCESS_DENIED);
        }
        return todo.toDto();
    }

    @Transactional
    public TodoDTO update(UUID userId, UUID todoId, TodoDTO dto) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.TODO_DETAIL_NOT_FOUND));
        if (!todo.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCodeEnum.ACCESS_DENIED);
        }
        todo.update(dto.getTitle(), dto.getDescription(), dto.isCompleted());
        return todoRepository.save(todo).toDto();
    }

    @Transactional
    public void delete(UUID userId, UUID todoId) {
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new ApiException(ErrorCodeEnum.TODO_DETAIL_NOT_FOUND));
        if (!todo.getUser().getId().equals(userId)) {
            throw new ApiException(ErrorCodeEnum.ACCESS_DENIED);
        }
        todoRepository.delete(todo);
    }

    public List<TodoDTO> search(UUID userId, String keyword) {
        return todoRepository.findByUserIdAndTitleContaining(userId, keyword)
                .stream()
                .map(Todo::toDto)
                .toList();
    }
}
