package tech.buildrun.unihub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.Evaluation;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade Evaluation.
 */
@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, UUID> {
    /**
     * Verifica se um usuário já avaliou um professor em um critério específico.
     */
    Optional<Evaluation> findByUserIdAndProfessorIdAndCriterionId(UUID userId, UUID professorId, UUID criterionId);
}
