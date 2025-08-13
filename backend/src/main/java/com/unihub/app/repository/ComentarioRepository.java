package com.unihub.app.repository;

import com.unihub.app.entity.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ComentarioRepository extends JpaRepository<Comentario, Long> {
    List<Comentario> findByAvaliacaoId(Long avaliacaoId);

    List<Comentario> findByAvaliacaoProfessorIdAndAvaliacaoCadeiraIdAndCriterioIdOrderByVotosPositivosDescVotosNegativosAsc(
        Long professorId, Long cadeiraId, Long criterioId);

    // Busca o principal comentário para um critério de um professor (maior score)
    @Query("SELECT c FROM Comentario c " +
           "JOIN c.avaliacao av " +
           "WHERE av.professor.id = :professorId AND c.criterio.id = :criterioId " +
           "ORDER BY (c.votosPositivos - c.votosNegativos) DESC, c.createdAt DESC")
    List<Comentario> findPrincipalComentarioPorCriterioEProfessor(@Param("professorId") Long professorId, @Param("criterioId") Long criterioId);

    // Busca comentários para a página de critério de um professor, ordenados por score
     @Query("SELECT c FROM Comentario c " +
           "JOIN c.avaliacao av " +
           "WHERE av.professor.id = :professorId AND c.criterio.id = :criterioId " +
           "ORDER BY (c.votosPositivos - c.votosNegativos) DESC, c.createdAt DESC")
    List<Comentario> findComentariosPorCriterioEProfessorOrdenadosPorScore(@Param("professorId") Long professorId, @Param("criterioId") Long criterioId);

    // Busca todos os comentários de um professor em uma cadeira específica
    @Query("SELECT c FROM Comentario c " +
           "JOIN c.avaliacao av " +
           "WHERE av.professor.id = :professorId AND av.cadeira.id = :cadeiraId " +
           "ORDER BY (c.votosPositivos - c.votosNegativos) DESC, c.createdAt DESC")
    Page<Comentario> findComentariosPorProfessorECadeira(@Param("professorId") Long professorId, @Param("cadeiraId") Long cadeiraId, Pageable pageable);

    @Query("SELECT c FROM Comentario c WHERE c.avaliacao.professor.id = :professorId AND c.criterio.id = :criterioId")
    Page<Comentario> findComentariosPorCriterioEProfessor(@Param("professorId") Long professorId, @Param("criterioId") Long criterioId, Pageable pageable);

    @Query("SELECT c FROM Comentario c " +
       "JOIN c.avaliacao av " +
       "WHERE av.professor.id = :professorId " +
       "AND c.criterio.id = :criterioId " +
       "AND av.cadeira.id = :cadeiraId")
    Page<Comentario> findComentariosPorCriterioEProfessorComCadeira(
        @Param("professorId") Long professorId,
        @Param("criterioId") Long criterioId,
        @Param("cadeiraId") Long cadeiraId,
        Pageable pageable);

    @Query("SELECT c FROM Comentario c " +
       "JOIN c.avaliacao av " +
       "WHERE av.professor.id = :professorId " +
       "AND c.criterio.id = :criterioId " +
       "AND c.createdAt >= :dataCorte " +
       "ORDER BY c.createdAt DESC")
    Page<Comentario> findComentariosPorCriterioEProfessorComPeriodo(
        @Param("professorId") Long professorId,
        @Param("criterioId") Long criterioId,
        @Param("dataCorte") LocalDateTime dataCorte,
        Pageable pageable);

    @Query("SELECT c FROM Comentario c " +
       "JOIN c.avaliacao av " +
       "WHERE av.professor.id = :professorId " +
       "AND c.criterio.id = :criterioId " +
       "AND av.cadeira.id = :cadeiraId " +
       "AND c.createdAt >= :dataCorte " +
       "ORDER BY c.createdAt DESC")
    Page<Comentario> findComentariosPorCriterioEProfessorComFiltros(
        @Param("professorId") Long professorId,
        @Param("criterioId") Long criterioId,
        @Param("cadeiraId") Long cadeiraId,
        @Param("dataCorte") LocalDateTime dataCorte,
        Pageable pageable);
}