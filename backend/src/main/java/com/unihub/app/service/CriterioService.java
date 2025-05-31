package com.unihub.app.service;

import com.unihub.app.dto.CriterioDto;
import com.unihub.app.dto.CriterioRequest;
import com.unihub.app.entity.Criterio;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.CriterioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CriterioService {

    @Autowired
    private CriterioRepository criterioRepository;

    @Transactional(readOnly = true)
    public Page<CriterioDto> getAllCriterios(Pageable pageable) {
        return criterioRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public List<CriterioDto> getAllCriteriosList() {
        return criterioRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CriterioDto getCriterioById(Long id) {
        Criterio criterio = criterioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", id));
        return convertToDto(criterio);
    }

    @Transactional
    public CriterioDto createCriterio(CriterioRequest criterioRequest) {
        if (criterioRepository.existsByNome(criterioRequest.getNome())) {
            throw new BadRequestException("Critério com nome '" + criterioRequest.getNome() + "' já existe.");
        }
        Criterio criterio = new Criterio();
        criterio.setNome(criterioRequest.getNome());
        return convertToDto(criterioRepository.save(criterio));
    }

    @Transactional
    public CriterioDto updateCriterio(Long id, CriterioRequest criterioRequest) {
        Criterio criterio = criterioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", id));

        if (!criterio.getNome().equals(criterioRequest.getNome()) && criterioRepository.existsByNome(criterioRequest.getNome())) {
            throw new BadRequestException("Outro critério com nome '" + criterioRequest.getNome() + "' já existe.");
        }
        criterio.setNome(criterioRequest.getNome());
        return convertToDto(criterioRepository.save(criterio));
    }

    @Transactional
    public void deleteCriterio(Long id) {
        Criterio criterio = criterioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", id));
        // Adicionar lógica para verificar se o critério está em uso antes de deletar, se necessário
        criterioRepository.delete(criterio);
    }

    protected CriterioDto convertToDto(Criterio criterio) {
        return new CriterioDto(criterio.getId(), criterio.getNome());
    }

    protected Criterio findCriterioEntityById(Long id) {
         return criterioRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", id));
    }
}
