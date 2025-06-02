package com.unihub.app.controller;

import com.unihub.app.dto.CursoDto;
import com.unihub.app.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    @Autowired
    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    // Get all Cursos (Admin only for full list, public might get a simplified list elsewhere)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')") // Allow USER to list Cursos for Cadeira creation form
    public ResponseEntity<List<CursoDto>> getAllCursos() {
        List<CursoDto> cursos = cursoService.getAllCursos();
        return ResponseEntity.ok(cursos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoDto> getCursoById(@PathVariable Long id) {
        CursoDto curso = cursoService.getCursoById(id); // This one might include cadeiras
        return ResponseEntity.ok(curso);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoDto> createCurso(@Valid @RequestBody CursoDto cursoDto) {
        // The DTO for creation should ideally not contain a list of cadeiras
        CursoDto createdCurso = cursoService.createCurso(new CursoDto(null, cursoDto.getNome()));
        return new ResponseEntity<>(createdCurso, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CursoDto> updateCurso(@PathVariable Long id, @Valid @RequestBody CursoDto cursoDto) {
        CursoDto updatedCurso = cursoService.updateCurso(id, new CursoDto(null, cursoDto.getNome()));
        return ResponseEntity.ok(updatedCurso);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteCurso(@PathVariable Long id) {
        cursoService.deleteCurso(id);
        return ResponseEntity.noContent().build();
    }
} 