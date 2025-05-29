package tech.buildrun.unihub.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Repositório para a entidade User.
 * Fornece métodos CRUD e de consulta personalizados.
 */
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}