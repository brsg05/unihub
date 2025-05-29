package tech.buildrun.unihub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.Role;

import java.util.Optional;

/**
 * Repositório para a entidade Role.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(String name);
}
