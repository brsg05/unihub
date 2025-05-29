package tech.buildrun.unihub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.buildrun.unihub.dto.CriterionRequest;
import tech.buildrun.unihub.dto.CriterionResponse;
import tech.buildrun.unihub.service.CriterionService;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciamento de critérios de avaliação (apenas para ADMIN).
 */
@RestController
@RequestMapping("/api/v1/criteria")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Todas as operações neste controller requerem ROLE_ADMIN
public class CriterionController {

    private final CriterionService criterionService;

    /**
     * Obtém todos os critérios.
     * @return Lista de CriterionResponse.
     */
    @GetMapping
    public ResponseEntity<List<CriterionResponse>> getAllCriteria() {
        List<CriterionResponse> criteria = criterionService.getAllCriteria();
        return ResponseEntity.ok(criteria);
    }

    /**
     * Obtém um critério por ID.
     * @param id ID do critério.
     * @return CriterionResponse.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CriterionResponse> getCriterionById(@PathVariable UUID id) {
        CriterionResponse criterion = criterionService.getCriterionById(id);
        return ResponseEntity.ok(criterion);
    }

    /**
     * Cria um novo critério.
     * @param request DTO com dados do critério.
     * @return CriterionResponse do critério criado.
     */
    @PostMapping
    public ResponseEntity<CriterionResponse> createCriterion(@Valid @RequestBody CriterionRequest request) {
        CriterionResponse newCriterion = criterionService.createCriterion(request);
        return new ResponseEntity<>(newCriterion, HttpStatus.CREATED);
    }

    /**
     * Atualiza um critério existente.
     * @param id ID do critério a ser atualizado.
     * @param request DTO com dados atualizados.
     * @return CriterionResponse do critério atualizado.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CriterionResponse> updateCriterion(@PathVariable UUID id, @Valid @RequestBody CriterionRequest request) {
        CriterionResponse updatedCriterion = criterionService.updateCriterion(id, request);
        return ResponseEntity.ok(updatedCriterion);
    }

    /**
     * Deleta um critério.
     * @param id ID do critério a ser deletado.
     * @return ResponseEntity com status HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCriterion(@PathVariable UUID id) {
        criterionService.deleteCriterion(id);
        return ResponseEntity.noContent().build();
    }
}
