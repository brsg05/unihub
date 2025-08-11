package com.unihub.app.service;

import com.unihub.app.dto.*;
import com.unihub.app.entity.*;
import com.unihub.app.exception.BadRequestException;
import com.unihub.app.exception.ResourceNotFoundException;
import com.unihub.app.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AvaliacaoService {

    @Autowired
    private AvaliacaoRepository avaliacaoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CadeiraRepository cadeiraRepository;

    @Autowired
    private CriterioRepository criterioRepository;

    @Autowired
    private ProfessorService professorService; // Para recalcular nota geral

    @Transactional
    public AvaliacaoDto createAvaliacao(AvaliacaoRequest avaliacaoRequest) {

        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        User currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "Email", userDetails.getUsername()));

        Professor professor = professorRepository.findById(avaliacaoRequest.getProfessorId())
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", avaliacaoRequest.getProfessorId()));

        Cadeira cadeira = cadeiraRepository.findById(avaliacaoRequest.getCadeiraId())
                .orElseThrow(() -> new ResourceNotFoundException("Cadeira", "id", avaliacaoRequest.getCadeiraId()));

        // Validar se o professor leciona a cadeira
        if (!professor.getCadeiras().contains(cadeira)) {
            throw new BadRequestException("Professor " + professor.getNomeCompleto() + " não leciona a cadeira " + cadeira.getNome());
        }
        
        // Validar se já existe uma avaliação para este usuário, professor, cadeira e período
        Optional<Avaliacao> existingAvaliacao = avaliacaoRepository
            .findByUserIdAndProfessorIdAndCadeiraIdAndPeriodo(currentUser.getId(), professor.getId(), cadeira.getId(), avaliacaoRequest.getPeriodo());
        if(existingAvaliacao.isPresent()){
            throw new BadRequestException("Usuário já avaliou este professor nesta cadeira para o período " + avaliacaoRequest.getPeriodo());
        }

        Avaliacao avaliacao = new Avaliacao();
        avaliacao.setUser(currentUser);
        avaliacao.setProfessor(professor);
        avaliacao.setCadeira(cadeira);
        avaliacao.setPeriodo(avaliacaoRequest.getPeriodo());
        avaliacao.setDataAvaliacao(LocalDateTime.now());

        for (NotaCriterioRequest notaReq : avaliacaoRequest.getNotasCriterios()) {
            Criterio criterio = criterioRepository.findById(notaReq.getCriterioId())
                    .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", notaReq.getCriterioId()));
            NotaCriterio notaCriterio = new NotaCriterio(avaliacao, criterio, notaReq.getNota());
            avaliacao.addNotaCriterio(notaCriterio);
        }

        if (avaliacaoRequest.getComentarios() != null) {
            for (ComentarioRequest comReq : avaliacaoRequest.getComentarios()) {
                Criterio criterio = criterioRepository.findById(comReq.getCriterioId())
                        .orElseThrow(() -> new ResourceNotFoundException("Criterio", "id", comReq.getCriterioId()));
                Comentario comentario = new Comentario(comReq.getTexto(), avaliacao, criterio);
                avaliacao.addComentario(comentario);
            }
        }
        Avaliacao savedAvaliacao = avaliacaoRepository.save(avaliacao);
        // Após salvar, recalcular a nota geral do professor
        professorService.calculateAndUpdateNotaGeral(professor.getId());
        return convertToDto(savedAvaliacao);
    }

    @Transactional(readOnly = true)
    public Page<AvaliacaoPublicDto> getAvaliacoesPublicasPage(Long professorId, Long cadeiraId, String periodo, Pageable pageable) {
        // Assuming AvaliacaoRepository has a method like findByProfessorIdAndCadeiraIdAndPeriodo(Long professorId, Long cadeiraId, String periodo, Pageable pageable)
        // Or findByProfessorAndCadeiraAndPeriodo (if entities are passed)
        // For now, let's assume a method exists that can take Pageable.
        // If not, this would need a custom query or specification.
        Page<Avaliacao> avaliacoesPage = avaliacaoRepository.findByProfessorIdAndCadeiraIdAndPeriodo(professorId, cadeiraId, periodo, pageable);
        return avaliacoesPage.map(this::convertToPublicDto);
    }

    @Transactional(readOnly = true)
    public List<AvaliacaoPublicDto> getAvaliacoesPublicas(Long professorId, Long cadeiraId, String periodo) {
        List<Avaliacao> avaliacoes = avaliacaoRepository.findAvaliacoesByProfessorCadeiraPeriodo(professorId, cadeiraId, periodo);
        return avaliacoes.stream().map(this::convertToPublicDto).collect(Collectors.toList());
    }


    private AvaliacaoDto convertToDto(Avaliacao avaliacao) {
        AvaliacaoDto dto = new AvaliacaoDto();
        dto.setId(avaliacao.getId());
        dto.setDataAvaliacao(avaliacao.getDataAvaliacao());
        dto.setPeriodo(avaliacao.getPeriodo());
        dto.setProfessorId(avaliacao.getProfessor().getId());
        dto.setCadeiraId(avaliacao.getCadeira().getId());
        // Não incluir usuarioId por padrão

        dto.setNotasCriterios(avaliacao.getNotasCriterio().stream().map(nc -> {
            NotaCriterioDto ncDto = new NotaCriterioDto();
            ncDto.setId(nc.getId());
            ncDto.setCriterio(new CriterioDto(nc.getCriterio().getId(), nc.getCriterio().getNome()));
            ncDto.setNota(nc.getNota());
            return ncDto;
        }).collect(Collectors.toList()));

        dto.setComentarios(avaliacao.getComentarios().stream().map(c -> {
            ComentarioDto cDto = new ComentarioDto();
            cDto.setId(c.getId());
            cDto.setTexto(c.getTexto());
            cDto.setAvaliacaoId(c.getAvaliacao().getId());
            cDto.setCriterioId(c.getCriterio().getId());
            cDto.setCriterioNome(c.getCriterio().getNome());
            cDto.setVotosPositivos(c.getVotosPositivos());
            cDto.setVotosNegativos(c.getVotosNegativos());
            cDto.setScore(c.getScore());
            cDto.setCreatedAt(c.getCreatedAt());
            return cDto;
        }).collect(Collectors.toList()));
        return dto;
    }

    private AvaliacaoPublicDto convertToPublicDto(Avaliacao avaliacao) {
        AvaliacaoPublicDto dto = new AvaliacaoPublicDto();
        dto.setId(avaliacao.getId());
        dto.setData(avaliacao.getDataAvaliacao().toString());
        dto.setPeriodo(avaliacao.getPeriodo());
        dto.setProfessorNome(avaliacao.getProfessor().getNomeCompleto());
        dto.setCadeiraNome(avaliacao.getCadeira().getNome());

        dto.setNotas(avaliacao.getNotasCriterio().stream()
            .map(nc -> new AvaliacaoNotaPublicDto(nc.getCriterio().getNome(), nc.getNota()))
            .collect(Collectors.toList()));

        dto.setComentarios(avaliacao.getComentarios().stream()
            .map(c -> new ComentarioPublicDto(c.getId(), c.getTexto(), c.getCriterio() != null ? c.getCriterio().getNome() : "Geral", c.getScore(), c.getCreatedAt()))
            .collect(Collectors.toList()));
        return dto;
    }
} 