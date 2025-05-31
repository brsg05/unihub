package com.unihub.app.repository;

import com.unihub.app.entity.Avaliacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AvaliacaoRepository extends JpaRepository<Avaliacao, Long> {

    Optional<Avaliacao> findByUserIdAndProfessorIdAndCadeiraIdAndPeriodo(Long userId, Long professorId, Long cadeiraId, String periodo);

    List<Avaliacao> findByProfessorIdAndCadeiraIdAndPeriodo(Long professorId, Long cadeiraId, String periodo);

    Page<Avaliacao> findByProfessorIdAndCadeiraIdAndPeriodo(Long professorId, Long cadeiraId, String periodo, Pageable pageable);

    List<Avaliacao> findByProfessorId(Long professorId);

    // Query to find evaluations for a specific professor that contain a grade for a specific criterion
    @Query("SELECT DISTINCT a FROM Avaliacao a JOIN a.notasCriterio nc WHERE a.professor.id = :professorId AND nc.criterio.id = :criterioId")
    Page<Avaliacao> findAvaliacoesByProfessorAndCriterio(
            @Param("professorId") Long professorId,
            @Param("criterioId") Long criterioId,
            Pageable pageable
    );
    
    // Query to find evaluations for a specific professor, criterion, and periodo
    @Query("SELECT DISTINCT a FROM Avaliacao a JOIN a.notasCriterio nc WHERE a.professor.id = :professorId AND nc.criterio.id = :criterioId AND a.periodo = :periodo")
    Page<Avaliacao> findAvaliacoesByProfessorAndCriterioAndPeriodo(
            @Param("professorId") Long professorId,
            @Param("criterioId") Long criterioId,
            @Param("periodo") String periodo,
            Pageable pageable
    );

    // Query para calcular a média de notas de um critério para um professor específico
    @Query("SELECT AVG(nc.nota) FROM NotaCriterio nc " +
           "JOIN nc.avaliacao a " +
           "WHERE a.professor.id = :professorId AND nc.criterio.id = :criterioId")
    Double findAverageNotaForCriterioByProfessor(@Param("professorId") Long professorId, @Param("criterioId") Long criterioId);

    // Query para buscar avaliações de um professor em uma cadeira para um período
    @Query("SELECT a FROM Avaliacao a WHERE a.professor.id = :professorId AND a.cadeira.id = :cadeiraId AND a.periodo = :periodo")
    List<Avaliacao> findAvaliacoesByProfessorCadeiraPeriodo(
            @Param("professorId") Long professorId,
            @Param("cadeiraId") Long cadeiraId,
            @Param("periodo") String periodo
    );

    // Query para histórico de avaliações de um critério específico para um professor
    @Query("SELECT nc FROM NotaCriterio nc JOIN nc.avaliacao a " +
           "WHERE a.professor.id = :professorId AND nc.criterio.id = :criterioId ORDER BY a.dataAvaliacao DESC")
    List<Object[]> findHistoricoAvaliacoesCriterioProfessor(
            @Param("professorId") Long professorId,
            @Param("criterioId") Long criterioId
    );

} 