package com.unihub.app.controller;

import com.unihub.app.dto.ProfessorDetailDto;
import com.unihub.app.dto.ProfessorDto;
import com.unihub.app.dto.ProfessorPublicDto;
import com.unihub.app.dto.ProfessorRequest;
import com.unihub.app.service.ProfessorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
@RequestMapping("/api/professores")
@Tag(name = "Professores", description = "Endpoints for managing professors")
public class ProfessorController {

    @Autowired
    private ProfessorService professorService;

    @GetMapping
    @Operation(summary = "List all professors with pagination and filtering",
               description = "Publicly accessible. Allows filtering by 'top' (best rated), 'worst' (worst rated), 'recent' (recently added), or by 'periodo' (academic period).")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of professors")
    public ResponseEntity<Page<ProfessorPublicDto>> getAllProfessores(
            @PageableDefault(size = 10) Pageable pageable,
            @Parameter(description = "Filter type: 'top', 'worst', 'recent', or 'periodo'") @RequestParam(required = false) String filter,
            @Parameter(description = "Academic period, e.g., '2023.1', required if filter is 'periodo'") @RequestParam(required = false) String periodo) {
        Page<ProfessorPublicDto> professores = professorService.getAllProfessores(pageable, filter, periodo);
        return ResponseEntity.ok(professores);
    }

    @GetMapping("/top")
    @Operation(summary = "Get top X professors", description = "Publicly accessible. Returns a list of top-rated professors.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved top professors")
    public ResponseEntity<List<ProfessorPublicDto>> getTopProfessores(
            @Parameter(description = "Number of top professors to return, default 5") @RequestParam(defaultValue = "5") int limit) {
        List<ProfessorPublicDto> topProfessores = professorService.getTopProfessores(limit);
        return ResponseEntity.ok(topProfessores);
    }

    @GetMapping("/search")
    @Operation(summary = "Search professors by name", description = "Publicly accessible.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved professors matching the search term")
    public ResponseEntity<Page<ProfessorPublicDto>> searchProfessores(
            @Parameter(description = "Search term for professor name") @RequestParam String nome,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<ProfessorPublicDto> professores = professorService.searchProfessoresByNome(nome, pageable);
        return ResponseEntity.ok(professores);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get professor details by ID", description = "Publicly accessible. Includes detailed information, average grade per criterion, and top comments.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved professor details")
    @ApiResponse(responseCode = "404", description = "Professor not found")
    public ResponseEntity<ProfessorDetailDto> getProfessorById(@PathVariable Long id) {
        ProfessorDetailDto professorDetailDto = professorService.getProfessorDetails(id);
        return ResponseEntity.ok(professorDetailDto);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new professor", description = "Admin only. Allows associating cadeiras (courses) with the professor.")
    @ApiResponse(responseCode = "201", description = "Professor created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<ProfessorDto> createProfessor(@Valid @RequestBody ProfessorRequest professorRequest) {
        ProfessorDto createdProfessor = professorService.createProfessor(professorRequest);
        return new ResponseEntity<>(createdProfessor, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing professor", description = "Admin only.")
    @ApiResponse(responseCode = "200", description = "Professor updated successfully")
    @ApiResponse(responseCode = "404", description = "Professor not found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<ProfessorDto> updateProfessor(@PathVariable Long id, @Valid @RequestBody ProfessorRequest professorRequest) {
        ProfessorDto updatedProfessor = professorService.updateProfessor(id, professorRequest);
        return ResponseEntity.ok(updatedProfessor);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a professor", description = "Admin only.")
    @ApiResponse(responseCode = "204", description = "Professor deleted successfully")
    @ApiResponse(responseCode = "404", description = "Professor not found")
    public ResponseEntity<Void> deleteProfessor(@PathVariable Long id) {
        professorService.deleteProfessor(id);
        return ResponseEntity.noContent().build();
    }
} 