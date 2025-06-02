package com.unihub.app.repository;

import com.unihub.app.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CursoRepository extends JpaRepository<Curso, Long> {
    Optional<Curso> findByNome(String nome);

    // Fetch all cursos with their cadeiras (useful for some scenarios, be mindful of N+1)
    @Query("SELECT c FROM Curso c LEFT JOIN FETCH c.cadeiras")
    List<Curso> findAllWithCadeiras();
} 