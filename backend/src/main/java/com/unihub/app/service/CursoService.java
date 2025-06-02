package com.unihub.app.service;

import com.unihub.app.dto.CadeiraSimplificadaDto;
import com.unihub.app.dto.CursoDto;
import com.unihub.app.entity.Curso;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.CadeiraRepository;
import com.unihub.app.repository.CursoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final CadeiraRepository cadeiraRepository; // Added for checking existing cadeiras

    @Autowired
    public CursoService(CursoRepository cursoRepository, CadeiraRepository cadeiraRepository) {
        this.cursoRepository = cursoRepository;
        this.cadeiraRepository = cadeiraRepository;
    }

    @Transactional(readOnly = true)
    public List<CursoDto> getAllCursos() {
        return cursoRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CursoDto getCursoById(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso", "id", id));
        return convertToDtoWithCadeiras(curso); // Optionally include cadeiras
    }

    @Transactional(readOnly = true)
    public Curso getCursoEntityById(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso", "id", id));
    }

    @Transactional
    public CursoDto createCurso(CursoDto cursoDto) {
        if (cursoRepository.findByNome(cursoDto.getNome()).isPresent()) {
            throw new BadRequestException("Curso com nome '" + cursoDto.getNome() + "' já existe.");
        }
        Curso curso = new Curso(cursoDto.getNome());
        curso = cursoRepository.save(curso);
        return convertToDto(curso);
    }

    @Transactional
    public CursoDto updateCurso(Long id, CursoDto cursoDto) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso", "id", id));

        if (!curso.getNome().equals(cursoDto.getNome()) && cursoRepository.findByNome(cursoDto.getNome()).isPresent()) {
            throw new BadRequestException("Outro curso com nome '" + cursoDto.getNome() + "' já existe.");
        }

        curso.setNome(cursoDto.getNome());
        curso = cursoRepository.save(curso);
        return convertToDto(curso);
    }

    @Transactional
    public void deleteCurso(Long id) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Curso", "id", id));
        
        // Check if there are any Cadeiras associated with this Curso
        if (cadeiraRepository.existsByCursoId(id)) {
            throw new BadRequestException("Não é possível excluir o curso pois existem cadeiras associadas a ele.");
        }
        
        cursoRepository.delete(curso);
    }

    private CursoDto convertToDto(Curso curso) {
        return new CursoDto(curso.getId(), curso.getNome());
    }

    private CursoDto convertToDtoWithCadeiras(Curso curso) {
        List<CadeiraSimplificadaDto> cadeiraDtos = curso.getCadeiras() != null ?
                curso.getCadeiras().stream()
                        .map(c -> new CadeiraSimplificadaDto(c.getId(), c.getNome()))
                        .collect(Collectors.toList()) :
                List.of();
        return new CursoDto(curso.getId(), curso.getNome(), cadeiraDtos);
    }
} 