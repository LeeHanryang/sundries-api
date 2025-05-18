package io.github.leehanryang.sundriesapi.domain.controller;

import io.github.leehanryang.sundriesapi.common.security.principal.CustomUserPrincipal;
import io.github.leehanryang.sundriesapi.domain.dto.TodoDTO;
import io.github.leehanryang.sundriesapi.domain.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Tag(name = "Todo", description = "Todo 관리 API")
@RestController
@RequestMapping("/todos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class TodoController {

    private final TodoService todoService;

    @Operation(summary = "Todo 생성", description = "Todo를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없습니다.", content = @Content),
            @ApiResponse(responseCode = "409", description = "이미 존재하는 항목입니다.", content = @Content)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(content = @Content(mediaType = "application/json",
            schemaProperties = {
                    @SchemaProperty(name = "title", schema = @Schema(implementation = String.class)),
                    @SchemaProperty(name = "description", schema = @Schema(implementation = String.class)),
            })
    )
    @PostMapping
    public ResponseEntity<Void> create(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Valid @RequestBody TodoDTO dto
    ) {
        TodoDTO saved = todoService.create(principal.id(), dto);
        return ResponseEntity.created(URI.create("/todos/" + saved.getId())).build();
    }

    @Operation(summary = "Todo 목록 조회", description = "사용자의 Todo 목록을 최신순으로 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoDTO.class)))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.", content = @Content),
    })

    @GetMapping
    public ResponseEntity<List<TodoDTO>> findAll(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal) {
        return ResponseEntity.ok(todoService.findAll(principal.id()));
    }

    @Operation(summary = "Todo 상세 조회", description = "Todo ID로 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = TodoDTO.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.", content = @Content)
    })

    @GetMapping("/{id}")
    public ResponseEntity<TodoDTO> findById(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Todo UUID") @PathVariable UUID id
    ) {
        return ResponseEntity.ok(todoService.findById(principal.id(), id));
    }

    @Operation(summary = "Todo 수정", description = "Todo ID에 해당하는 항목을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = TodoDTO.class))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.", content = @Content)
    })

    @PutMapping("/{id}")
    public ResponseEntity<TodoDTO> update(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Todo UUID") @PathVariable UUID id,
            @Valid @RequestBody TodoDTO dto
    ) {
        return ResponseEntity.ok(todoService.update(principal.id(), id, dto));
    }

    @Operation(summary = "Todo 삭제", description = "Todo ID에 해당하는 항목을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공", content = @Content),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.", content = @Content),
            @ApiResponse(responseCode = "404", description = "데이터를 찾을 수 없습니다.", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "Todo UUID") @PathVariable UUID id

    ) {
        todoService.delete(principal.id(), id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Todo 검색", description = "제목으로 Todo 를 검색합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "검색 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoDTO.class)))),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 토큰입니다.", content = @Content),
    })
    @GetMapping("/search")
    public ResponseEntity<List<TodoDTO>> search(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserPrincipal principal,
            @Parameter(description = "검색 키워드") @RequestParam String keyword) {
        return ResponseEntity.ok(todoService.search(principal.id(), keyword));
    }
}