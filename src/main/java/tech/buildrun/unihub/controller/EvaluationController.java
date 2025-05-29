package tech.buildrun.unihub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.buildrun.unihub.dto.EvaluationRequest;
import tech.buildrun.unihub.dto.EvaluationResponse;
import tech.buildrun.unihub.service.EvaluationService;

import java.util.UUID;

/**
 * Controller para registro de avaliações.
 */
@RestController
@RequestMapping("/api/v1/professors/{professorId}/criteria/{criterionId}/evaluations")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    /**
     * Registra uma nova avaliação para um professor em um critério específico.
     * Requer autenticação (ROLE_USER ou ROLE_ADMIN).
     * @param professorId ID do professor.
     * @param criterionId ID do critério.
     * @param request DTO com a pontuação e texto do comentário (opcional).
     * @return EvaluationResponse da avaliação criada.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Apenas usuários autenticados podem avaliar
    public ResponseEntity<EvaluationResponse> createEvaluation(
            @PathVariable UUID professorId,
            @PathVariable UUID criterionId,
            @Valid @RequestBody EvaluationRequest request) {
        EvaluationResponse newEvaluation = evaluationService.createEvaluation(professorId, criterionId, request);
        return new ResponseEntity<>(newEvaluation, HttpStatus.CREATED);
    }
}