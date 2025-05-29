package tech.buildrun.unihub.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tech.buildrun.unihub.entity.Professor;

import java.util.List;
import java.util.UUID;

/**
 * Repositório para a entidade Professor.
 * Inclui métodos personalizados para cálculo de médias.
 */
@Repository
public interface ProfessorRepository extends JpaRepository<Professor, UUID> {

    // Busca professores por nome (case-insensitive)
    List<Professor> findByNameContainingIgnoreCase(String name);

    /**
     * Calcula a média geral das avaliações para um professor específico.
     * Retorna um Double, ou null se não houver avaliações.
     */
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.professor.id = :professorId")
    Double calculateOverallAverageScore(@Param("professorId") UUID professorId);

    /**
     * Calcula a média das avaliações para um professor em um critério específico.
     * Retorna um Double, ou null se não houver avaliações para aquele critério.
     */
    @Query("SELECT AVG(e.score) FROM Evaluation e WHERE e.professor.id = :professorId AND e.criterion.id = :criterionId")
    Double calculateAverageScoreByCriterion(@Param("professorId") UUID professorId, @Param("criterionId") UUID criterionId);

    /**
     * Busca os N professores com as maiores médias gerais.
     * Retorna uma lista de objetos que contêm o Professor e sua média.
     * Nota: Para paginação e ordenação de resultados complexos, é comum usar Pageable
     * ou construir uma DTO específica para o resultado. Aqui, estamos simplificando
     * para o Top N.
     */
    @Query(value = "SELECT p.id, p.name, p.email, p.department, AVG(e.score) as averageScore " +
            "FROM professors p JOIN evaluations e ON p.id = e.professor_id " +
            "GROUP BY p.id, p.name, p.email, p.department " +
            "ORDER BY averageScore DESC LIMIT :topN",
            nativeQuery = true)
    List<Object[]> findTopNProfessorsByAverageScore(@Param("topN") int topN);
}