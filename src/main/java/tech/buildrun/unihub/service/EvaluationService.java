package tech.buildrun.unihub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.dto.EvaluationRequest;
import tech.buildrun.unihub.dto.EvaluationResponse;
import tech.buildrun.unihub.entity.*;
import tech.buildrun.unihub.exception.ResourceNotFoundException;
import tech.buildrun.unihub.exception.ValidationException;
import tech.buildrun.unihub.repository.*;

import java.util.UUID;

/**
 * Serviço para gerenciamento de avaliações.
 * Inclui registro de avaliação e criação opcional de comentário.
 */
@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final ProfessorRepository professorRepository;
    private final CriterionRepository criterionRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    /**
     * Converte uma entidade Evaluation para EvaluationResponse DTO.
     */
    private EvaluationResponse toEvaluationResponse(Evaluation evaluation) {
        return new EvaluationResponse(
                evaluation.getId(),
                evaluation.getScore(),
                evaluation.getUser().getId(),
                evaluation.getProfessor().getId(),
                evaluation.getCriterion().getId(),
                evaluation.getComment() != null ? evaluation.getComment().getId() : null,
                evaluation.getCreatedAt(),
                evaluation.getUpdatedAt()
        );
    }

    /**
     * Registra uma nova avaliação para um professor em um critério.
     * Inclui a criação opcional de um comentário.
     *
     * @param professorId ID do professor.
     * @param criterionId ID do critério.
     * @param request DTO com a pontuação e texto do comentário (opcional).
     * @return EvaluationResponse da avaliação criada.
     * @throws ResourceNotFoundException se professor ou critério não forem encontrados.
     * @throws ValidationException se o usuário já avaliou este professor neste critério.
     */
    @Transactional
    // Limpa o cache de detalhes do professor e lista de professores ao adicionar uma nova avaliação
    @CacheEvict(value = {"professorDetails", "professorsList"}, allEntries = true)
    public EvaluationResponse createEvaluation(UUID professorId, UUID criterionId, EvaluationRequest request) {
        // Obtém o usuário autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado", "username", username));

        Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", professorId));

        Criterion criterion = criterionRepository.findById(criterionId)
                .orElseThrow(() -> new ResourceNotFoundException("Critério", "id", criterionId));

        // Valida se o usuário já avaliou este professor neste critério
        if (evaluationRepository.findByUserIdAndProfessorIdAndCriterionId(currentUser.getId(), professorId, criterionId).isPresent()) {
            throw new ValidationException("Você já avaliou este professor neste critério.");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setScore(request.getScore());
        evaluation.setUser(currentUser);
        evaluation.setProfessor(professor);
        evaluation.setCriterion(criterion);

        // Se houver texto de comentário, cria e associa o comentário
        if (request.getCommentText() != null && !request.getCommentText().trim().isEmpty()) {
            Comment comment = new Comment();
            comment.setText(request.getCommentText().trim());
            comment.setEvaluation(evaluation); // Associa a avaliação ao comentário
            comment.setUser(currentUser); // O autor do comentário é o avaliador
            evaluation.setComment(comment); // Associa o comentário à avaliação
        }

        Evaluation savedEvaluation = evaluationRepository.save(evaluation);
        return toEvaluationResponse(savedEvaluation);
    }
}
