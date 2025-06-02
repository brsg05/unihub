package com.unihub.app.service;

import com.unihub.app.dto.CadeiraDto;
import com.unihub.app.dto.CadeiraRequest;
import com.unihub.app.entity.Cadeira;
import com.unihub.app.entity.Curso;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.CadeiraRepository;
import com.unihub.app.service.CursoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CadeiraService {

    private final CadeiraRepository cadeiraRepository;
    private final CursoService cursoService;

    @Autowired
    public CadeiraService(CadeiraRepository cadeiraRepository, CursoService cursoService) {
        this.cadeiraRepository = cadeiraRepository;
        this.cursoService = cursoService;
    }

    @Transactional(readOnly = true)
    public Page<CadeiraDto> getAllCadeiras(Pageable pageable) {
        return cadeiraRepository.findAll(pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<CadeiraDto> getAllCadeirasList() {
        return cadeiraRepository.findAll().stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CadeiraDto getCadeiraById(Long id) {
        Cadeira cadeira = cadeiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));
        return convertToDto(cadeira);
    }

    @Transactional
    public CadeiraDto createCadeira(CadeiraRequest cadeiraRequest) {
        Curso curso = cursoService.getCursoEntityById(cadeiraRequest.getCursoId());
        if (cadeiraRepository.existsByNomeAndCursoId(cadeiraRequest.getNome(), curso.getId())) {
            throw new BadRequestException("Cadeira com nome '" + cadeiraRequest.getNome() + "' já existe para o curso '" + curso.getNome() + "'.");
        }

        Cadeira cadeira = new Cadeira();
        cadeira.setNome(cadeiraRequest.getNome());
        cadeira.setCargaHoraria(cadeiraRequest.getCargaHoraria());
        cadeira.setIsEletiva(cadeiraRequest.getIsEletiva());
        cadeira.setCurso(curso);
        Cadeira savedCadeira = cadeiraRepository.save(cadeira);
        return convertToDto(savedCadeira);
    }

    @Transactional
    public CadeiraDto updateCadeira(Long id, CadeiraRequest cadeiraRequest) {
        Cadeira cadeira = cadeiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));

        Curso curso = cursoService.getCursoEntityById(cadeiraRequest.getCursoId());

        if (!cadeira.getNome().equals(cadeiraRequest.getNome()) || !cadeira.getCurso().getId().equals(curso.getId())) {
            if (cadeiraRepository.existsByNomeAndCursoId(cadeiraRequest.getNome(), curso.getId())) {
                throw new BadRequestException("Outra cadeira com nome '" + cadeiraRequest.getNome() + "' já existe para o curso '" + curso.getNome() + "'.");
            }
        }

        cadeira.setNome(cadeiraRequest.getNome());
        cadeira.setCargaHoraria(cadeiraRequest.getCargaHoraria());
        cadeira.setIsEletiva(cadeiraRequest.getIsEletiva());
        cadeira.setCurso(curso);
        Cadeira updatedCadeira = cadeiraRepository.save(cadeira);
        return convertToDto(updatedCadeira);
    }

    @Transactional
    public void deleteCadeira(Long id) {
        Cadeira cadeira = cadeiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));
        cadeiraRepository.delete(cadeira);
    }

    protected CadeiraDto convertToDto(Cadeira cadeira) {
        CadeiraDto dto = new CadeiraDto();
        dto.setId(cadeira.getId());
        dto.setNome(cadeira.getNome());
        dto.setCargaHoraria(cadeira.getCargaHoraria());
        dto.setIsEletiva(cadeira.getIsEletiva());
        if (cadeira.getCurso() != null) {
            dto.setCursoId(cadeira.getCurso().getId());
            dto.setCursoNome(cadeira.getCurso().getNome());
        }
        return dto;
    }

    protected Cadeira findCadeiraEntityById(Long id) {
        return cadeiraRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));
    }
} 