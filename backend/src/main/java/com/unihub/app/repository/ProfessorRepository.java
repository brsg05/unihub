package com.unihub.app.repository;

import com.unihub.app.entity.Professor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Page<Professor> findByNomeCompletoContainingIgnoreCase(String nome, Pageable pageable);

    List<Professor> findByNomeCompletoContainingIgnoreCase(String nome);

    // Query para buscar Top X professores pela notaGeral
    @Query(value = "SELECT * FROM professores p ORDER BY p.nota_geral DESC NULLS LAST LIMIT :limit", nativeQuery = true)
    List<Professor> findTopXByOrderByNotaGeralDesc(@Param("limit") int limit);

    // Query para filtrar professores que lecionaram em um determinado período (mais complexa, envolve Avaliacao)
    // Esta é uma simplificação. Uma query mais precisa dependeria de como "periodo" é associado a professor.
    // Assumindo que queremos professores com avaliações no período:
    @Query("SELECT DISTINCT p FROM Professor p JOIN p.avaliacoes a WHERE a.periodo = :periodo")
    Page<Professor> findByPeriodoLecionado(@Param("periodo") String periodo, Pageable pageable);
} 