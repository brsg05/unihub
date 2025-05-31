package com.unihub.app.service;

import com.unihub.app.dto.ComentarioSimplificadoDto;
import com.unihub.app.dto.CriterioProfessorDetailDto;
import com.unihub.app.dto.HistoricoAvaliacaoCriterioDto;
import com.unihub.app.dto.AvaliacaoPublicDto;
import com.unihub.app.dto.AvaliacaoNotaPublicDto;
import com.unihub.app.dto.ComentarioPublicDto;
import com.unihub.app.entity.*;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.AvaliacaoRepository;
import com.unihub.app.repository.ComentarioRepository;
import com.unihub.app.repository.CriterioRepository;
import com.unihub.app.repository.ProfessorRepository;
import com.unihub.app.repository.NotaCriterioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PublicQueryService {

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CriterioRepository criterioRepository;

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;
    
    @Autowired
    private NotaCriterioRepository notaCriterioRepository; 

    @Autowired
    private ComentarioRepository comentarioRepository;

    // Method to convert Avaliacao to AvaliacaoPublicDto (can be helper or part of AvaliacaoService if more generic)
    private AvaliacaoPublicDto convertToPublicDto(Avaliacao avaliacao) {
        AvaliacaoPublicDto dto = new AvaliacaoPublicDto();
        dto.setId(avaliacao.getId());
        dto.setData(avaliacao.getDataAvaliacao().toString()); // Assuming dataAvaliacao is LocalDateTime
        dto.setPeriodo(avaliacao.getPeriodo());
        dto.setProfessorNome(avaliacao.getProfessor().getNomeCompleto());
        dto.setCadeiraNome(avaliacao.getCadeira().getNome());

        dto.setNotas(avaliacao.getNotasCriterio().stream()
            .map(nc -> new AvaliacaoNotaPublicDto(nc.getCriterio().getNome(), nc.getNota()))
            .collect(Collectors.toList()));

        // Assuming ComentarioPublicDto needs id, texto, criterioNome, score, createdAt from Comentario entity
        dto.setComentarios(avaliacao.getComentarios().stream()
            .map(c -> new ComentarioPublicDto(c.getId(), c.getTexto(), c.getCriterio() != null ? c.getCriterio().getNome() : "Geral", c.getScore(), c.getCreatedAt()))
            .collect(Collectors.toList()));
        return dto;
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoPublicDto> getCriterionEvaluationHistory(
            Long professorId,
            Long criterioId,
            String periodo,
            Pageable pageable) {
        
        Page<Avaliacao> avaliacoesPage;
        if (periodo != null && !periodo.isEmpty()) {
            avaliacoesPage = avaliacaoRepository.findAvaliacoesByProfessorAndCriterioAndPeriodo(professorId, criterioId, periodo, pageable);
        } else {
            avaliacoesPage = avaliacaoRepository.findAvaliacoesByProfessorAndCriterio(professorId, criterioId, pageable);
        }
        
        return avaliacoesPage.map(this::convertToPublicDto); // Use the converter method
    }

    @Transactional(readOnly = true)
    public CriterioProfessorDetailDto getCriterioProfessorDetails(Long criterioId, Long professorId) {
        Criterio criterio = criterioRepository.findById(criterioId)
            .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", criterioId));
        Professor professor = professorRepository.findById(professorId)
            .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", professorId));

        // Usar a query customizada para buscar o histórico
        // A query `findHistoricoAvaliacoesCriterioProfessor` retorna List<Object[]> ou List<NotaCriterio>
        // Precisamos adaptar o DTO `HistoricoAvaliacaoCriterioDto` e a conversão
        
        // Simplificacao: Buscar todas NotaCriterio e depois filtrar comentários
        List<NotaCriterio> notasDoCriterioParaProfessor = professor.getAvaliacoes().stream()
            .flatMap(av -> av.getNotasCriterio().stream())
            .filter(nc -> nc.getCriterio().getId().equals(criterioId))
            .collect(Collectors.toList());

        List<HistoricoAvaliacaoCriterioDto> historico = notasDoCriterioParaProfessor.stream()
            .map(nc -> {
                Comentario comentarioAssociado = nc.getAvaliacao().getComentarios().stream()
                    .filter(com -> com.getCriterio().getId().equals(criterioId))
                    .findFirst().orElse(null);
                ComentarioSimplificadoDto comDto = null;
                if (comentarioAssociado != null) {
                    comDto = new ComentarioSimplificadoDto(comentarioAssociado.getId(), comentarioAssociado.getTexto(), comentarioAssociado.getScore());
                }
                return new HistoricoAvaliacaoCriterioDto(
                    nc.getAvaliacao().getId(), 
                    nc.getAvaliacao().getPeriodo(), 
                    nc.getNota(), 
                    comDto
                );
            })
            .sorted((h1, h2) -> h2.getPeriodo().compareTo(h1.getPeriodo())) // Ordena por período mais recente
            .collect(Collectors.toList());

        List<Comentario> comentarios = comentarioRepository.findComentariosPorCriterioEProfessorOrdenadosPorScore(professorId, criterioId);
        List<ComentarioSimplificadoDto> principaisComentarios = comentarios.stream()
            .map(c -> new ComentarioSimplificadoDto(c.getId(), c.getTexto(), c.getScore()))
            .collect(Collectors.toList());

        return new CriterioProfessorDetailDto(
            criterio.getId(), 
            criterio.getNome(), 
            professor.getId(), 
            professor.getNomeCompleto(), 
            historico, 
            principaisComentarios
        );
    }
} 