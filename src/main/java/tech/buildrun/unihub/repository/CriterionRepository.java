package tech.buildrun.unihub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.Criterion;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade Criterion.
 */
@Repository
public interface CriterionRepository extends JpaRepository<Criterion, UUID> {
    Optional<Criterion> findByName(String name);
    boolean existsByName(String name);
}
