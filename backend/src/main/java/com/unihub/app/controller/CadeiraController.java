package com.unihub.app.controller;

import com.unihub.app.dto.CadeiraDto;
import com.unihub.app.dto.CadeiraRequest;
import com.unihub.app.service.CadeiraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
@RequestMapping("/api/cadeiras")
@Tag(name = "Cadeiras", description = "Endpoints para gerenciamento de Cadeiras (Disciplinas)")
public class CadeiraController {

    @Autowired
    private CadeiraService cadeiraService;

    @GetMapping
    @Operation(summary = "Lista todas as cadeiras (paginado)", description = "Retorna uma lista paginada de todas as cadeiras.")
    public ResponseEntity<Page<CadeiraDto>> getAllCadeiras(
            @PageableDefault(size = 10, sort = "nome") Pageable pageable) {
        return ResponseEntity.ok(cadeiraService.getAllCadeiras(pageable));
    }
    
    @GetMapping("/all") // Endpoint para obter todas as cadeiras sem paginação
    @Operation(summary = "Lista todas as cadeiras (sem paginação)", description = "Retorna uma lista completa de todas as cadeiras, útil para dropdowns etc.")
    public ResponseEntity<List<CadeiraDto>> getAllCadeirasList() {
        return ResponseEntity.ok(cadeiraService.getAllCadeirasList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca cadeira por ID", description = "Retorna os detalhes de uma cadeira específica.")
    public ResponseEntity<CadeiraDto> getCadeiraById(@PathVariable Long id) {
        return ResponseEntity.ok(cadeiraService.getCadeiraById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cria uma nova cadeira", description = "Adiciona uma nova cadeira ao sistema. Requer papel ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CadeiraDto> createCadeira(@Valid @RequestBody CadeiraRequest cadeiraRequest) {
        CadeiraDto novaCadeira = cadeiraService.createCadeira(cadeiraRequest);
        return new ResponseEntity<>(novaCadeira, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualiza uma cadeira existente", description = "Modifica os dados de uma cadeira. Requer papel ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<CadeiraDto> updateCadeira(@PathVariable Long id, @Valid @RequestBody CadeiraRequest cadeiraRequest) {
        return ResponseEntity.ok(cadeiraService.updateCadeira(id, cadeiraRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deleta uma cadeira", description = "Remove uma cadeira do sistema. Requer papel ADMIN.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> deleteCadeira(@PathVariable Long id) {
        cadeiraService.deleteCadeira(id);
        return ResponseEntity.noContent().build();
    }
} 