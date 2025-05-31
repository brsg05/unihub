package com.unihub.app.service;

import com.unihub.app.dto.CadeiraDto;
import com.unihub.app.dto.CadeiraRequest;
import com.unihub.app.entity.Cadeira;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.CadeiraRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CadeiraService {

    @Autowired
    private CadeiraRepository cadeiraRepository;

    @Transactional(readOnly = true)
    public Page<CadeiraDto> getAllCadeiras(Pageable pageable) {
        return cadeiraRepository.findAll(pageable).map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<CadeiraDto> getAllCadeirasList() { // For internal use or non-paginated lists
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
        if (cadeiraRepository.existsByNome(cadeiraRequest.getNome())) {
            throw new BadRequestException("Cadeira com nome '" + cadeiraRequest.getNome() + "' já existe.");
        }
        Cadeira cadeira = new Cadeira();
        cadeira.setNome(cadeiraRequest.getNome());
        cadeira.setCargaHoraria(cadeiraRequest.getCargaHoraria());
        cadeira.setIsEletiva(cadeiraRequest.getIsEletiva());
        return convertToDto(cadeiraRepository.save(cadeira));
    }

    @Transactional
    public CadeiraDto updateCadeira(Long id, CadeiraRequest cadeiraRequest) {
        Cadeira cadeira = cadeiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));

        // Check if name is being changed and if the new name already exists
        if (!cadeira.getNome().equals(cadeiraRequest.getNome()) && cadeiraRepository.existsByNome(cadeiraRequest.getNome())) {
            throw new BadRequestException("Outra cadeira com nome '" + cadeiraRequest.getNome() + "' já existe.");
        }

        cadeira.setNome(cadeiraRequest.getNome());
        cadeira.setCargaHoraria(cadeiraRequest.getCargaHoraria());
        cadeira.setIsEletiva(cadeiraRequest.getIsEletiva());
        return convertToDto(cadeiraRepository.save(cadeira));
    }

    @Transactional
    public void deleteCadeira(Long id) {
        Cadeira cadeira = cadeiraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));
        // Considerar o que acontece com Professores associados (cascade? ou impedir exclusão?)
        // Por enquanto, a FK em professor_cadeiras tem ON DELETE CASCADE
        cadeiraRepository.delete(cadeira);
    }

    protected CadeiraDto convertToDto(Cadeira cadeira) {
        return new CadeiraDto(cadeira.getId(), cadeira.getNome(), cadeira.getCargaHoraria(), cadeira.getIsEletiva());
    }

    protected Cadeira findCadeiraEntityById(Long id) {
        return cadeiraRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", id));
    }
} 