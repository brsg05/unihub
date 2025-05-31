package com.unihub.app.repository;

import com.unihub.app.entity.NotaCriterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotaCriterioRepository extends JpaRepository<NotaCriterio, Long> {
    List<NotaCriterio> findByAvaliacaoId(Long avaliacaoId);

    // Para calcular a nota geral de um professor
    @Query("SELECT AVG(nc.nota) FROM NotaCriterio nc JOIN nc.avaliacao a WHERE a.professor.id = :professorId")
    Double calculateAverageNotaByProfessorId(@Param("professorId") Long professorId);
} 