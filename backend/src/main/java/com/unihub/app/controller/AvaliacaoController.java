package com.unihub.app.controller;

import com.unihub.app.dto.AvaliacaoPublicDto;
import com.unihub.app.dto.AvaliacaoRequest;
import com.unihub.app.dto.MessageResponse;
import com.unihub.app.service.AvaliacaoService;
import com.unihub.app.service.PublicQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/avaliacoes")
@Tag(name = "Avaliações", description = "Endpoints for managing evaluations")
public class AvaliacaoController {

    @Autowired
    private AvaliacaoService avaliacaoService;

    @Autowired
    private PublicQueryService publicQueryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Submit a new evaluation", description = "User or Admin role required. Allows submitting grades for all criteria of a course for a specific professor and period. Optional comments can be included.",
               security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "201", description = "Evaluation submitted successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data or business rule violation (e.g., duplicate evaluation)")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User not logged in")
    public ResponseEntity<MessageResponse> submitAvaliacao(@Valid @RequestBody AvaliacaoRequest avaliacaoRequest) {
        avaliacaoService.createAvaliacao(avaliacaoRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Evaluation submitted successfully!"));
    }

    @GetMapping("/professor/{professorId}/cadeira/{cadeiraId}")
    @Operation(summary = "Get evaluations for a professor and cadeira (course)", description = "Publicly accessible. Lists evaluations with pagination.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved evaluations")
    public ResponseEntity<Page<AvaliacaoPublicDto>> getAvaliacoesByProfessorAndCadeira(
            @PathVariable Long professorId,
            @PathVariable Long cadeiraId,
            @Parameter(description = "Academic period (e.g., 2023.1)") @RequestParam(required = false) String periodo,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<AvaliacaoPublicDto> avaliacoes = avaliacaoService.getAvaliacoesPublicasPage(professorId, cadeiraId, periodo, pageable);
        return ResponseEntity.ok(avaliacoes);
    }

    @GetMapping("/criterio/{criterioId}/professor/{professorId}")
    @Operation(summary = "Get criterion evaluation history for a professor",
               description = "Publicly accessible. Shows evaluation history and top comments for a specific criterion and professor.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved criterion evaluation history")
    public ResponseEntity<Page<AvaliacaoPublicDto>> getCriterionHistoryForProfessor(
            @PathVariable Long criterioId,
            @PathVariable Long professorId,
            @Parameter(description = "Academic period, e.g., '2023.1'") @RequestParam(required = false) String periodo,
            @PageableDefault(size = 10) Pageable pageable) {
        // This might be better placed in PublicQueryService if it involves more complex aggregation
        // For now, assuming AvaliacaoService can handle it or delegate appropriately.
        Page<AvaliacaoPublicDto> history = publicQueryService.getCriterionEvaluationHistory(professorId, criterioId, periodo, pageable);
        return ResponseEntity.ok(history);
    }
} 