package com.unihub.app.service;

import com.unihub.app.dto.ComentarioDto;
import com.unihub.app.dto.ComentarioVoteRequest;
import com.unihub.app.entity.Comentario;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.ComentarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComentarioService {

    @Autowired
    private ComentarioRepository comentarioRepository;

    @Transactional
    public ComentarioDto voteOnComentario(Long comentarioId, ComentarioVoteRequest voteRequest) {
        Comentario comentario = comentarioRepository.findById(comentarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario", "id", comentarioId));

        if ("UPVOTE".equalsIgnoreCase(voteRequest.getType())) {
            comentario.setVotosPositivos(comentario.getVotosPositivos() + 1);
        } else if ("DOWNVOTE".equalsIgnoreCase(voteRequest.getType())) {
            comentario.setVotosNegativos(comentario.getVotosNegativos() + 1);
        } else {
            throw new BadRequestException("Tipo de voto inválido: " + voteRequest.getType());
        }
        
        // Garantir que os valores nunca sejam negativos
        if (comentario.getVotosPositivos() < 0) {
            comentario.setVotosPositivos(0);
        }
        if (comentario.getVotosNegativos() < 0) {
            comentario.setVotosNegativos(0);
        }
        
        return convertToDto(comentarioRepository.save(comentario));
    }

    public Page<ComentarioDto> getComentariosPorProfessorECadeira(Long professorId, Long cadeiraId, Pageable pageable) {
        Page<Comentario> comentarios = comentarioRepository.findComentariosPorProfessorECadeira(professorId, cadeiraId, pageable);
        return comentarios.map(this::convertToDto);
    }

    public Page<ComentarioDto> getComentariosPorCriterioEProfessor(Long professorId, Long criterioId, Long cadeiraId, String periodo, Pageable pageable) {
        System.out.println("DEBUG - Pageable sort: " + pageable.getSort());
        System.out.println("DEBUG - cadeiraId: " + cadeiraId + ", periodo: " + periodo);
        
        // Verificar se a ordenação é por votos e converter para score
        Pageable adjustedPageable = adjustPageableForScore(pageable);
        System.out.println("DEBUG - Adjusted Pageable sort: " + adjustedPageable.getSort());
        
        LocalDateTime dataCorte = null;
        if (periodo != null && !periodo.isEmpty()) {
            LocalDateTime agora = LocalDateTime.now();
            switch (periodo) {
                case "ultima-semana":
                    dataCorte = agora.minusWeeks(1);
                    break;
                case "ultimo-mes":
                    dataCorte = agora.minusMonths(1);
                    break;
                case "ultimo-semestre":
                    dataCorte = agora.minusMonths(6);
                    break;
                case "ultimo-ano":
                    dataCorte = agora.minusYears(1);
                    break;
            }
        }
        
        Page<Comentario> comentarios;
        if (cadeiraId == null && dataCorte == null) {
            // Sem filtros - usar query simples
            comentarios = comentarioRepository.findComentariosPorCriterioEProfessor(professorId, criterioId, pageable);
        } else if (cadeiraId != null && dataCorte == null) {
            // Apenas filtro de cadeira
            comentarios = comentarioRepository.findComentariosPorCriterioEProfessorComCadeira(professorId, criterioId, cadeiraId, pageable);
        } else if (cadeiraId == null && dataCorte != null) {
            // Apenas filtro de período
            comentarios = comentarioRepository.findComentariosPorCriterioEProfessorComPeriodo(professorId, criterioId, dataCorte, pageable);
        } else {
            // Ambos os filtros
            comentarios = comentarioRepository.findComentariosPorCriterioEProfessorComFiltros(professorId, criterioId, cadeiraId, dataCorte, pageable);
        }
        
        return comentarios.map(this::convertToDto);
    }

    private Pageable adjustPageableForScore(Pageable original) {
        if (original.getSort().isEmpty()) {
            return original;
        }
        
        Sort adjustedSort = Sort.unsorted();
        for (Sort.Order order : original.getSort()) {
            if ("votosPositivos".equals(order.getProperty()) || "votosNegativos".equals(order.getProperty())) {
                // Converter para ordenação por score (votosPositivos - votosNegativos)
                adjustedSort = adjustedSort.and(Sort.by(order.getDirection(), "score"));
            } else {
                adjustedSort = adjustedSort.and(Sort.by(order.getDirection(), order.getProperty()));
            }
        }
        
        return PageRequest.of(original.getPageNumber(), original.getPageSize(), adjustedSort);
    }

    private ComentarioDto convertToDto(Comentario comentario) {
        ComentarioDto dto = new ComentarioDto();
        dto.setId(comentario.getId());
        dto.setTexto(comentario.getTexto());
        dto.setAvaliacaoId(comentario.getAvaliacao().getId());
        dto.setCriterioId(comentario.getCriterio().getId());
        dto.setCriterioNome(comentario.getCriterio().getNome());
        
        // Adicionar nome da cadeira através da avaliação
        if (comentario.getAvaliacao() != null && comentario.getAvaliacao().getCadeira() != null) {
            dto.setCadeiraNome(comentario.getAvaliacao().getCadeira().getNome());
        }
        
        dto.setVotosPositivos(comentario.getVotosPositivos());
        dto.setVotosNegativos(comentario.getVotosNegativos());
        dto.setScore(comentario.getScore());
        dto.setCreatedAt(comentario.getCreatedAt());
        // Por enquanto, não controlaremos voto único por usuário
        dto.setUserVoteType(null);
        return dto;
    }
}