package tech.buildrun.unihub.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.Comment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade Comment.
 * Inclui métodos para paginação, ordenação e atualização atômica de votos.
 */
@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    /**
     * Busca comentários para um professor em um critério específico,
     * paginados e ordenados pelo score (votos positivos - negativos).
     * O Pageable deve incluir a ordenação "score,desc" ou "score,asc".
     */
    @Query(value = "SELECT c FROM Comment c " +
            "WHERE c.evaluation.professor.id = :professorId " +
            "AND c.evaluation.criterion.id = :criterionId")
    Page<Comment> findByProfessorIdAndCriterionId(@Param("professorId") UUID professorId,
                                                  @Param("criterionId") UUID criterionId,
                                                  Pageable pageable);

    /**
     * Encontra o comentário com o maior score para um dado professor e critério.
     * Usado para exibir o "top" comentário na página de detalhes do professor.
     */
    @Query(value = "SELECT c.* FROM comments c " +
            "JOIN evaluations e ON c.evaluation_id = e.id " +
            "WHERE e.professor_id = :professorId AND e.criterion_id = :criterionId " +
            "ORDER BY (c.positive_votes_count - c.negative_votes_count) DESC " +
            "LIMIT 1",
            nativeQuery = true)
    Optional<Comment> findTopCommentByProfessorIdAndCriterionId(@Param("professorId") UUID professorId,
                                                                @Param("criterionId") UUID criterionId);

    /**
     * Incrementa atomicamente o contador de votos positivos de um comentário.
     */
    @Modifying
    @Query("UPDATE Comment c SET c.positiveVotesCount = c.positiveVotesCount + 1 WHERE c.id = :commentId")
    void incrementPositiveVotesCount(@Param("commentId") UUID commentId);

    /**
     * Incrementa atomicamente o contador de votos negativos de um comentário.
     */
    @Modifying
    @Query("UPDATE Comment c SET c.negativeVotesCount = c.negativeVotesCount + 1 WHERE c.id = :commentId")
    void incrementNegativeVotesCount(@Param("commentId") UUID commentId);
}