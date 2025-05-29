package tech.buildrun.unihub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.buildrun.unihub.dto.CommentResponse;
import tech.buildrun.unihub.dto.CommentVoteRequest;
import tech.buildrun.unihub.service.CommentService;

import java.util.UUID;

/**
 * Controller para listagem de comentários e votação.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * Obtém uma página de comentários para um professor em um critério específico.
     * Não requer autenticação.
     * @param professorId ID do professor.
     * @param criterionId ID do critério.
     * @param page Número da página (default 0).
     * @param size Tamanho da página (default 10).
     * @param sort Campo e direção de ordenação (ex: "score,desc", "createdAt,asc").
     * @return Página de CommentResponse.
     */
    @GetMapping("/professors/{professorId}/criteria/{criterionId}/comments")
    public ResponseEntity<Page<CommentResponse>> getCommentsForProfessorAndCriterion(
            @PathVariable UUID professorId,
            @PathVariable UUID criterionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "score,desc") String[] sort) {

        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        String property = sort[0];
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, property));

        Page<CommentResponse> comments = commentService.getCommentsForProfessorAndCriterion(professorId, criterionId, pageable);
        return ResponseEntity.ok(comments);
    }

    /**
     * Registra um voto em um comentário.
     * Requer autenticação (ROLE_USER ou ROLE_ADMIN).
     * @param commentId ID do comentário a ser votado.
     * @param request DTO indicando se o voto é positivo (true) ou negativo (false).
     * @return ResponseEntity com status HTTP 200 OK.
     */
    @PostMapping("/comments/{commentId}/vote")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Apenas usuários autenticados podem votar
    public ResponseEntity<Void> voteComment(
            @PathVariable UUID commentId,
            @Valid @RequestBody CommentVoteRequest request) {
        commentService.voteComment(commentId, request);
        return ResponseEntity.ok().build();
    }
}