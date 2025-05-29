package tech.buildrun.unihub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.dto.CommentResponse;
import tech.buildrun.unihub.dto.CommentVoteRequest;
import tech.buildrun.unihub.entity.Comment;
import tech.buildrun.unihub.entity.CommentVote;
import tech.buildrun.unihub.entity.User;
import tech.buildrun.unihub.exception.ResourceNotFoundException;
import tech.buildrun.unihub.exception.ValidationException;
import tech.buildrun.unihub.repository.CommentRepository;
import tech.buildrun.unihub.repository.CommentVoteRepository;
import tech.buildrun.unihub.repository.UserRepository;

import java.util.UUID;

/**
 * Serviço para gerenciamento de comentários e votos.
 * Inclui listagem paginada de comentários e votação atômica.
 */
@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentVoteRepository commentVoteRepository;
    private final UserRepository userRepository;

    /**
     * Converte uma entidade Comment para CommentResponse DTO.
     */
    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getText(),
                comment.getUser().getId(),
                comment.getUser().getUsername(),
                comment.getScore(), // Score é transiente, calculado na entidade
                comment.getPositiveVotesCount(),
                comment.getNegativeVotesCount(),
                comment.getCreatedAt(),
                comment.getUpdatedAt()
        );
    }

    /**
     * Obtém uma página de comentários para um professor e critério específicos.
     * Os comentários são ordenados pelo score (votos positivos - negativos).
     *
     * @param professorId ID do professor.
     * @param criterionId ID do critério.
     * @param pageable Objeto Pageable para paginação e ordenação (ex: PageRequest.of(0, 10, Sort.by("score").descending())).
     * @return Página de CommentResponse.
     */
    @Transactional(readOnly = true)
    public Page<CommentResponse> getCommentsForProfessorAndCriterion(UUID professorId, UUID criterionId, Pageable pageable) {
        // A ordenação por "score" é feita no nível da aplicação, pois o campo é transiente.
        // O repositório busca os comentários e, em seguida, o Spring Data JPA aplica a paginação.
        // A ordenação pelo score transiente é feita em memória após a busca.
        // Para ordenar corretamente por score (positiveVotesCount - negativeVotesCount),
        // a JPQL customizada no repositório já faz isso.
        Page<Comment> commentsPage = commentRepository.findByProfessorIdAndCriterionId(professorId, criterionId, pageable);
        return commentsPage.map(this::toCommentResponse);
    }

    /**
     * Registra um voto em um comentário.
     * Utiliza isolamento de transação SERIALIZABLE para evitar condições de corrida
     * na atualização dos contadores de votos.
     *
     * @param commentId ID do comentário a ser votado.
     * @param request DTO indicando se o voto é positivo (true) ou negativo (false).
     * @throws ResourceNotFoundException se o comentário não for encontrado.
     * @throws ValidationException se o usuário já votou neste comentário.
     */
    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void voteComment(UUID commentId, CommentVoteRequest request) {
        // Obtém o usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado", "username", username));

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("Comentário", "id", commentId));

        // Verifica se o usuário já votou neste comentário
        if (commentVoteRepository.findByUserIdAndCommentId(currentUser.getId(), commentId).isPresent()) {
            throw new ValidationException("Você já votou neste comentário.");
        }

        // Cria o registro do voto
        CommentVote vote = new CommentVote();
        vote.setIsPositive(request.getUp());
        vote.setUser(currentUser);
        vote.setComment(comment);
        commentVoteRepository.save(vote);

        // Atualiza os contadores de votos no comentário
        if (request.getUp()) {
            commentRepository.incrementPositiveVotesCount(commentId);
        } else {
            commentRepository.incrementNegativeVotesCount(commentId);
        }

        // Nota sobre o isolamento:
        // Isolation.SERIALIZABLE garante que, mesmo com múltiplas requisições concorrentes
        // para votar no mesmo comentário, as operações de leitura (verificar voto existente)
        // e escrita (salvar voto, atualizar contadores) serão executadas de forma sequencial,
        // prevenindo race conditions e garantindo a consistência dos contadores.
        // Isso pode impactar a performance em cenários de altíssima concorrência,
        // mas é a forma mais segura de garantir a integridade dos contadores.
        // Alternativas seriam usar bloqueios otimistas (versão) ou pessimistas,
        // ou operações atômicas de banco de dados se o DB as suportar diretamente.
    }
}
