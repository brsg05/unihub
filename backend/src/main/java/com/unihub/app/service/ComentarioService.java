package com.unihub.app.service;

import com.unihub.app.dto.ComentarioDto;
import com.unihub.app.dto.ComentarioVoteRequest;
import com.unihub.app.entity.Comentario;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.ComentarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return convertToDto(comentarioRepository.save(comentario));
    }

    private ComentarioDto convertToDto(Comentario comentario) {
        ComentarioDto dto = new ComentarioDto();
        dto.setId(comentario.getId());
        dto.setTexto(comentario.getTexto());
        dto.setAvaliacaoId(comentario.getAvaliacao().getId());
        dto.setCriterioId(comentario.getCriterio().getId());
        dto.setCriterioNome(comentario.getCriterio().getNome());
        dto.setVotosPositivos(comentario.getVotosPositivos());
        dto.setVotosNegativos(comentario.getVotosNegativos());
        dto.setScore(comentario.getScore());
        dto.setCreatedAt(comentario.getCreatedAt());
        return dto;
    }
} 