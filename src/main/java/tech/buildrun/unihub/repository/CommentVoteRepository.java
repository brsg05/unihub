package tech.buildrun.unihub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.CommentVote;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade CommentVote.
 */
@Repository
public interface CommentVoteRepository extends JpaRepository<CommentVote, UUID> {
    /**
     * Verifica se um usuário já votou em um comentário específico.
     */
    Optional<CommentVote> findByUserIdAndCommentId(UUID userId, UUID commentId);
}

