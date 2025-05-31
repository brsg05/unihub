package com.unihub.app.controller;

import com.unihub.app.dto.CriterioDto;
import com.unihub.app.dto.CriterioRequest;
import com.unihub.app.service.CriterioService;
import io.swagger.v3.oas.annotations.Operation;
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

import java.util.List;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/criterios")
@Tag(name = "Critérios", description = "Endpoints para gerenciamento de Critérios de Avaliação")
public class CriterioController {

    @Autowired
    private CriterioService criterioService;

    @GetMapping
    @Operation(summary = "Lista todos os critérios com paginação", description = "Retorna uma lista paginada de todos os critérios de avaliação.")
    public ResponseEntity<Page<CriterioDto>> getAllCriterios(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(criterioService.getAllCriterios(pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lista todos os critérios (sem paginação)", description = "Retorna uma lista completa de todos os critérios de avaliação, útil para dropdowns.")
    public ResponseEntity<List<CriterioDto>> getAllCriteriosList() {
        return ResponseEntity.ok(criterioService.getAllCriteriosList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca critério por ID", description = "Retorna os detalhes de um critério específico.")
    public ResponseEntity<CriterioDto> getCriterioById(@PathVariable Long id) {
        return ResponseEntity.ok(criterioService.getCriterioById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria um novo critério", description = "Adiciona um novo critério de avaliação. Requer papel ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CriterioDto> createCriterio(@Valid @RequestBody CriterioRequest criterioRequest) {
        CriterioDto novoCriterio = criterioService.createCriterio(criterioRequest);
        return new ResponseEntity<>(novoCriterio, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza um critério existente", description = "Modifica os dados de um critério. Requer papel ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CriterioDto> updateCriterio(@PathVariable Long id, @Valid @RequestBody CriterioRequest criterioRequest) {
        return ResponseEntity.ok(criterioService.updateCriterio(id, criterioRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deleta um critério", description = "Remove um critério de avaliação. Requer papel ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteCriterio(@PathVariable Long id) {
        criterioService.deleteCriterio(id);
        return ResponseEntity.noContent().build();
    }
} 