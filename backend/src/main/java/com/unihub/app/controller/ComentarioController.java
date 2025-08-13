package com.unihub.app.controller;

import com.unihub.app.dto.ComentarioDto;
import com.unihub.app.dto.ComentarioVoteRequest;
import com.unihub.app.dto.MessageResponse;
import com.unihub.app.service.ComentarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/comentarios")
@Tag(name = "Comentários", description = "Endpoints for managing comments")
public class ComentarioController {

    @Autowired
    private ComentarioService comentarioService;

    @GetMapping("/professor/{professorId}/cadeira/{cadeiraId}")
    @Operation(summary = "Get comments for a professor in a specific discipline", description = "Returns a paginated list of comments for evaluations of a professor in a specific discipline")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved comments")
    @ApiResponse(responseCode = "404", description = "Professor or discipline not found")
    public ResponseEntity<Page<ComentarioDto>> getComentariosPorProfessorECadeira(
            @PathVariable Long professorId,
            @PathVariable Long cadeiraId,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ComentarioDto> comentarios = comentarioService.getComentariosPorProfessorECadeira(professorId, cadeiraId, pageable);
        return ResponseEntity.ok(comentarios);
    }

    @GetMapping("/professor/{professorId}/criterio/{criterioId}")
    @Operation(summary = "Get comments for a professor by specific criterion", description = "Returns a paginated list of comments for evaluations of a professor filtered by criterion")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved comments")
    @ApiResponse(responseCode = "404", description = "Professor or criterion not found")
    public ResponseEntity<Page<ComentarioDto>> getComentariosPorCriterioEProfessor(
            @PathVariable Long professorId,
            @PathVariable Long criterioId,
            @RequestParam(required = false) Long cadeiraId,
            @RequestParam(required = false) String periodo,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ComentarioDto> comentarios = comentarioService.getComentariosPorCriterioEProfessor(professorId, criterioId, cadeiraId, periodo, pageable);
        return ResponseEntity.ok(comentarios);
    }

    @PostMapping("/{comentarioId}/vote/up")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Upvote a comment", description = "User role required.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successfully upvoted comment")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<ComentarioDto> upvoteComentario(@PathVariable Long comentarioId) {
        ComentarioVoteRequest voteRequest = new ComentarioVoteRequest();
        voteRequest.setType("UPVOTE");
        ComentarioDto comentario = comentarioService.voteOnComentario(comentarioId, voteRequest);
        return ResponseEntity.ok(comentario);
    }

    @PostMapping("/{comentarioId}/vote/down")
    @PreAuthorize("hasRole('USER')")
    @Operation(summary = "Downvote a comment", description = "User role required.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successfully downvoted comment")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Comment not found")
    public ResponseEntity<ComentarioDto> downvoteComentario(@PathVariable Long comentarioId) {
        ComentarioVoteRequest voteRequest = new ComentarioVoteRequest();
        voteRequest.setType("DOWNVOTE");
        ComentarioDto comentario = comentarioService.voteOnComentario(comentarioId, voteRequest);
        return ResponseEntity.ok(comentario);
    }

    // GET endpoints for comments are typically part of Avaliacao or Professor responses
    // to provide context. If standalone comment fetching is needed, it can be added here.
}