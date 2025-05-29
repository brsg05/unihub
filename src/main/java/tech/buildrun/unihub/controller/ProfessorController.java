package tech.buildrun.unihub.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tech.buildrun.unihub.dto.ProfessorRequest;
import tech.buildrun.unihub.dto.ProfessorResponse;
import tech.buildrun.unihub.service.ProfessorService;

import java.util.List;
import java.util.UUID;

/**
 * Controller para gerenciamento de professores.
 * Inclui busca, detalhes e CRUD (Admin-only).
 */
@RestController
@RequestMapping("/api/v1/professors")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;

    /**
     * Obtém todos os professores, com opções de busca por nome ou pelos top N professores.
     * Não requer autenticação.
     * @param name Nome para busca (opcional).
     * @param topN Número de professores com maior média para retornar (opcional).
     * @return Lista de ProfessorResponse.
     */
    @GetMapping
    public ResponseEntity<List<ProfessorResponse>> getAllProfessors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer topN) {
        List<ProfessorResponse> professors = professorService.getAllProfessors(name, topN);
        return ResponseEntity.ok(professors);
    }

    /**
     * Obtém os detalhes de um professor específico, incluindo médias e comentários "top".
     * Não requer autenticação.
     * @param id ID do professor.
     * @return ProfessorResponse completo.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProfessorResponse> getProfessorById(@PathVariable UUID id) {
        ProfessorResponse professor = professorService.getProfessorById(id);
        return ResponseEntity.ok(professor);
    }

    /**
     * Cria um novo professor. Requer role ADMIN.
     * @param request DTO com dados do professor.
     * @return ProfessorResponse do professor criado.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorResponse> createProfessor(@Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse newProfessor = professorService.createProfessor(request);
        return new ResponseEntity<>(newProfessor, HttpStatus.CREATED);
    }

    /**
     * Atualiza um professor existente. Requer role ADMIN.
     * @param id ID do professor a ser atualizado.
     * @param request DTO com dados atualizados.
     * @return ProfessorResponse do professor atualizado.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProfessorResponse> updateProfessor(@PathVariable UUID id, @Valid @RequestBody ProfessorRequest request) {
        ProfessorResponse updatedProfessor = professorService.updateProfessor(id, request);
        return ResponseEntity.ok(updatedProfessor);
    }

    /**
     * Deleta um professor. Requer role ADMIN.
     * @param id ID do professor a ser deletado.
     * @return ResponseEntity com status HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProfessor(@PathVariable UUID id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
}