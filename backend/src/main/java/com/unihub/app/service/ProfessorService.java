package com.unihub.app.service;

import com.unihub.app.dto.*;
import com.unihub.app.entity.*;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProfessorService {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CadeiraRepository cadeiraRepository;

    @Autowired
    private CriterioRepository criterioRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Autowired
    private NotaCriterioRepository notaCriterioRepository;

    @Transactional(readOnly = true)
    public Page<ProfessorPublicDto> getAllProfessores(Pageable pageable, String nome, String periodo) {
        Page<Professor> professoresPage;
        if (nome != null && !nome.isEmpty()) {
            professoresPage = professorRepository.findByNomeCompletoContainingIgnoreCase(nome, pageable);
        } else if (periodo != null && !periodo.isEmpty()) {
            professoresPage = professorRepository.findByPeriodoLecionado(periodo, pageable);
        } else {
            professoresPage = professorRepository.findAll(pageable);
        }
        return professoresPage.map(this::convertToPublicDto);
    }

    @Transactional(readOnly = true)
    public List<ProfessorPublicDto> getTopProfessores(int limit) {
        return professorRepository.findTopXByOrderByNotaGeralDesc(limit).stream()
                .map(this::convertToPublicDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProfessorPublicDto> searchProfessoresByNome(String nome, Pageable pageable) {
        Page<Professor> professoresPage = professorRepository.findByNomeCompletoContainingIgnoreCase(nome, pageable);
        return professoresPage.map(this::convertToPublicDto);
    }

    @Transactional(readOnly = true)
    public ProfessorDetailDto getProfessorDetails(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));
        
        ProfessorDetailDto detailDto = new ProfessorDetailDto();
        detailDto.setId(professor.getId());
        detailDto.setNomeCompleto(professor.getNomeCompleto());
        detailDto.setPhotoUrl(professor.getPhotoUrl());
        detailDto.setNotaGeral(professor.getNotaGeral() != null ? professor.getNotaGeral().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);

        detailDto.setCadeiras(professor.getCadeiras().stream()
            .map(c -> new CadeiraSimplificadaDto(c.getId(), c.getNome()))
            .collect(Collectors.toList()));

        List<Criterio> criterios = criterioRepository.findAll();
        
        List<BackendCriterioComMediaDto> backendCriteriosComMedias = criterios.stream().map(criterio -> {
            BackendCriterioDto backendCriterioDto = new BackendCriterioDto();
            backendCriterioDto.setId(criterio.getId());
            backendCriterioDto.setNome(criterio.getNome());
            
            Double notaMediaDouble = avaliacaoRepository.findAverageNotaForCriterioByProfessor(id, criterio.getId());
            BigDecimal notaMedia = (notaMediaDouble != null) ? BigDecimal.valueOf(notaMediaDouble).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            
            List<Comentario> principaisComentarios = comentarioRepository.findPrincipalComentarioPorCriterioEProfessor(id, criterio.getId());
            ComentarioSimplificadoDto principalComentarioDto = null;
            if (!principaisComentarios.isEmpty()) {
                Comentario principalComentario = principaisComentarios.get(0); // Pega o primeiro (maior score)
                principalComentarioDto = new ComentarioSimplificadoDto(principalComentario.getId(), principalComentario.getTexto(), principalComentario.getScore());
            }
            
            BackendCriterioComMediaDto criterioComMedia = new BackendCriterioComMediaDto();
            criterioComMedia.setCriterio(backendCriterioDto);
            criterioComMedia.setMediaNotas(notaMedia);
            criterioComMedia.setTopComentario(principalComentarioDto);
            
            return criterioComMedia;
        }).collect(Collectors.toList());
        
        detailDto.setCriteriosComMedias(backendCriteriosComMedias);

        return detailDto;
    }

    @Transactional
    public ProfessorDto createProfessor(ProfessorRequest professorRequest) {
        Professor professor = new Professor();
        professor.setNomeCompleto(professorRequest.getNomeCompleto());
        professor.setPhotoUrl(professorRequest.getPhotoUrl());

        if (professorRequest.getCadeiraIds() != null && !professorRequest.getCadeiraIds().isEmpty()) {
            Set<Cadeira> cadeiras = new HashSet<>(cadeiraRepository.findAllById(professorRequest.getCadeiraIds()));
            if(cadeiras.size() != professorRequest.getCadeiraIds().size()){
                throw new BadRequestException("Uma ou mais cadeiras fornecidas não existem.");
            }
            professor.setCadeiras(cadeiras);
        }
        return convertToDto(professorRepository.save(professor));
    }

    @Transactional
    public ProfessorDto updateProfessor(Long id, ProfessorRequest professorRequest) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));

        professor.setNomeCompleto(professorRequest.getNomeCompleto());
        professor.setPhotoUrl(professorRequest.getPhotoUrl());

        if (professorRequest.getCadeiraIds() != null) {
            if (professorRequest.getCadeiraIds().isEmpty()){
                professor.getCadeiras().clear();
            } else {
                Set<Cadeira> cadeiras = new HashSet<>(cadeiraRepository.findAllById(professorRequest.getCadeiraIds()));
                 if(cadeiras.size() != professorRequest.getCadeiraIds().size()){
                    throw new BadRequestException("Uma ou mais cadeiras fornecidas para atualização não existem.");
                }
                professor.setCadeiras(cadeiras);
            }
        }
        // A notaGeral é calculada por trigger ou batch, não setada aqui diretamente.
        return convertToDto(professorRepository.save(professor));
    }

    @Transactional
    public void deleteProfessor(Long id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));
        professorRepository.delete(professor);
    }

    @Transactional
    public ProfessorDto addCadeiraToProfessor(Long professorId, Long cadeiraId) {
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", professorId));
        Cadeira cadeira = cadeiraRepository.findById(cadeiraId)
            .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", cadeiraId));
        
        professor.getCadeiras().add(cadeira);
        return convertToDto(professorRepository.save(professor));
    }

    @Transactional
    public ProfessorDto removeCadeiraFromProfessor(Long professorId, Long cadeiraId) {
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", professorId));
        Cadeira cadeira = cadeiraRepository.findById(cadeiraId)
            .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", cadeiraId));

        professor.getCadeiras().remove(cadeira);
        return convertToDto(professorRepository.save(professor));
    }

    @Transactional
    public BigDecimal calculateAndUpdateNotaGeral(Long professorId) {
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", professorId));
        
        Double averageNota = notaCriterioRepository.calculateAverageNotaByProfessorId(professorId);
        BigDecimal notaGeral = (averageNota != null) ? BigDecimal.valueOf(averageNota).setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        professor.setNotaGeral(notaGeral);
        professorRepository.save(professor);
        return notaGeral;
    }


    private ProfessorDto convertToDto(Professor professor) {
        Set<CadeiraDto> cadeiraDtos = professor.getCadeiras() != null ?
            professor.getCadeiras().stream()
            .map(c -> {
                Long cursoId = (c.getCurso() != null) ? c.getCurso().getId() : null;
                String cursoNome = (c.getCurso() != null) ? c.getCurso().getNome() : null;
                // Cadeira.curso is @NotNull and optional=false, so cursoId should not be null here.
                if (cursoId == null) {
                     // This case should ideally not be reached.
                    System.err.println("Critical Error: Cadeira with ID " + c.getId() + " has a null Curso associated, despite being mandatory.");
                    // Depending on desired strictness, could throw an IllegalStateException.
                    // For now, it will proceed and might lead to issues if CadeiraDto constructor expects non-null cursoId.
                }
                return new CadeiraDto(c.getId(), c.getNome(), c.getCargaHoraria(), c.getIsEletiva(), cursoId, cursoNome);
            })
            .collect(Collectors.toSet()) : Collections.emptySet();
        return new ProfessorDto(professor.getId(), professor.getNomeCompleto(), professor.getPhotoUrl(), professor.getNotaGeral(), cadeiraDtos);
    }

    private ProfessorPublicDto convertToPublicDto(Professor professor) {
        BigDecimal notaGeral = professor.getNotaGeral() != null ? professor.getNotaGeral().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        return new ProfessorPublicDto(professor.getId(), professor.getNomeCompleto(), professor.getPhotoUrl(), notaGeral);
    }
} 