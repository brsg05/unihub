package tech.buildrun.unihub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.dto.CommentResponse;
import tech.buildrun.unihub.dto.ProfessorRequest;
import tech.buildrun.unihub.dto.ProfessorResponse;
import tech.buildrun.unihub.entity.Criterion;
import tech.buildrun.unihub.entity.Professor;
import tech.buildrun.unihub.exception.ResourceNotFoundException;
import tech.buildrun.unihub.exception.ValidationException;
import tech.buildrun.unihub.repository.CommentRepository;
import tech.buildrun.unihub.repository.CriterionRepository;
import tech.buildrun.unihub.repository.ProfessorRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de professores.
 * Inclui CRUD (Admin-only), busca, cálculo de médias e top comentários.
 */
@Service
@RequiredArgsConstructor
public class ProfessorService {

    private final ProfessorRepository professorRepository;
    private final CriterionRepository criterionRepository;
    private final CommentRepository commentRepository;

    /**
     * Converte uma entidade Professor para ProfessorResponse DTO.
     * Não inclui médias ou comentários "top" nesta conversão básica.
     */
    private ProfessorResponse toProfessorResponse(Professor professor) {
        ProfessorResponse response = new ProfessorResponse();
        response.setId(professor.getId());
        response.setName(professor.getName());
        response.setEmail(professor.getEmail());
        response.setDepartment(professor.getDepartment());
        response.setCreatedAt(professor.getCreatedAt());
        response.setUpdatedAt(professor.getUpdatedAt());
        return response;
    }

    /**
     * Obtém todos os professores, com opção de buscar por nome ou pelos top N professores.
     *
     * @param name Nome para busca (opcional).
     * @param topN Número de professores com maior média para retornar (opcional).
     * @return Lista de ProfessorResponse.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "professorsList", key = "{#name, #topN}") // Cacheia a lista de professores
    public List<ProfessorResponse> getAllProfessors(String name, Integer topN) {
        List<Professor> professors;
        if (name != null && !name.isEmpty()) {
            professors = professorRepository.findByNameContainingIgnoreCase(name);
        } else if (topN != null && topN > 0) {
            // Retorna os top N professores com suas médias
            // O método do repositório retorna Object[], então precisamos mapeá-lo
            List<Object[]> topProfessorsData = professorRepository.findTopNProfessorsByAverageScore(topN);
            return topProfessorsData.stream().map(data -> {
                ProfessorResponse response = new ProfessorResponse();
                response.setId((UUID) data[0]);
                response.setName((String) data[1]);
                response.setEmail((String) data[2]);
                response.setDepartment((String) data[3]);
                // O AVG retorna um Double, mas pode vir como BigDecimal dependendo do driver/DB
                if (data[4] instanceof BigDecimal) {
                    response.setAverageScore(((BigDecimal) data[4]).doubleValue());
                } else if (data[4] instanceof Double) {
                    response.setAverageScore((Double) data[4]);
                }
                return response;
            }).collect(Collectors.toList());
        } else {
            professors = professorRepository.findAll();
        }

        return professors.stream()
                .map(this::toProfessorResponseWithAverages) // Inclui médias para todos os resultados, se não for topN
                .collect(Collectors.toList());
    }

    /**
     * Obtém um professor por ID, incluindo médias gerais, médias por critério e comentários "top".
     *
     * @param id ID do professor.
     * @return ProfessorResponse completo.
     * @throws ResourceNotFoundException se o professor não for encontrado.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "professorDetails", key = "#id") // Cacheia detalhes de professor
    public ProfessorResponse getProfessorById(UUID id) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));

        ProfessorResponse response = toProfessorResponse(professor);

        // Calcula a média geral
        Double overallAverage = professorRepository.calculateOverallAverageScore(id);
        response.setAverageScore(overallAverage != null ?
                BigDecimal.valueOf(overallAverage).setScale(2, RoundingMode.HALF_UP).doubleValue() : null);

        // Calcula médias por critério e encontra o comentário "top" por critério
        Map<String, Double> criteriaAverages = new java.util.HashMap<>();
        List<CommentResponse> topComments = new java.util.ArrayList<>();

        List<Criterion> criteria = criterionRepository.findAll();
        for (Criterion criterion : criteria) {
            Double criterionAverage = professorRepository.calculateAverageScoreByCriterion(id, criterion.getId());
            if (criterionAverage != null) {
                criteriaAverages.put(criterion.getName(),
                        BigDecimal.valueOf(criterionAverage).setScale(2, RoundingMode.HALF_UP).doubleValue());
            }

            // Encontra o comentário "top" para este critério e professor
            commentRepository.findTopCommentByProfessorIdAndCriterionId(id, criterion.getId())
                    .ifPresent(comment -> {
                        CommentResponse commentResponse = new CommentResponse();
                        commentResponse.setId(comment.getId());
                        commentResponse.setText(comment.getText());
                        commentResponse.setUserId(comment.getUser().getId());
                        commentResponse.setUsername(comment.getUser().getUsername());
                        commentResponse.setPositiveVotesCount(comment.getPositiveVotesCount());
                        commentResponse.setNegativeVotesCount(comment.getNegativeVotesCount());
                        commentResponse.setScore(comment.getScore()); // Score é transiente
                        commentResponse.setCreatedAt(comment.getCreatedAt());
                        commentResponse.setUpdatedAt(comment.getUpdatedAt());
                        topComments.add(commentResponse);
                    });
        }
        response.setCriteriaAverages(criteriaAverages);
        response.setTopComments(topComments);

        return response;
    }

    /**
     * Cria um novo professor. Requer role ADMIN.
     *
     * @param request DTO com dados do professor.
     * @return ProfessorResponse do professor criado.
     * @throws ValidationException se o email já estiver em uso.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    // @CacheEvict(value = {"professorsList", "professorDetails"}, allEntries = true) // Limpa caches relacionados a professores
    public ProfessorResponse createProfessor(ProfessorRequest request) {
        if (request.getEmail() != null && professorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("E-mail do professor já está em uso.");
        }

        Professor professor = new Professor();
        professor.setName(request.getName());
        professor.setEmail(request.getEmail());
        professor.setDepartment(request.getDepartment());

        Professor savedProfessor = professorRepository.save(professor);
        return toProfessorResponse(savedProfessor);
    }

    /**
     * Atualiza um professor existente. Requer role ADMIN.
     *
     * @param id ID do professor a ser atualizado.
     * @param request DTO com dados atualizados.
     * @return ProfessorResponse do professor atualizado.
     * @throws ResourceNotFoundException se o professor não for encontrado.
     * @throws ValidationException se o email já estiver em uso por outro professor.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    // @CacheEvict(value = {"professorsList", "professorDetails"}, allEntries = true) // Limpa caches relacionados a professores
    public ProfessorResponse updateProfessor(UUID id, ProfessorRequest request) {
        Professor professor = professorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Professor", "id", id));

        if (request.getEmail() != null && !request.getEmail().equals(professor.getEmail()) &&
                professorRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ValidationException("E-mail do professor já está em uso por outro professor.");
        }

        professor.setName(request.getName());
        professor.setEmail(request.getEmail());
        professor.setDepartment(request.getDepartment());

        Professor updatedProfessor = professorRepository.save(professor);
        return toProfessorResponse(updatedProfessor);
    }

    /**
     * Deleta um professor. Requer role ADMIN.
     *
     * @param id ID do professor a ser deletado.
     * @throws ResourceNotFoundException se o professor não for encontrado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    // @CacheEvict(value = {"professorsList", "professorDetails"}, allEntries = true) // Limpa caches relacionados a professores
    public void deleteProfessor(UUID id) {
        if (!professorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Professor", "id", id);
        }
        professorRepository.deleteById(id);
    }

    /**
     * Helper para incluir médias em ProfessorResponse quando não é uma busca por topN.
     */
    private ProfessorResponse toProfessorResponseWithAverages(Professor professor) {
        ProfessorResponse response = toProfessorResponse(professor);
        Double overallAverage = professorRepository.calculateOverallAverageScore(professor.getId());
        response.setAverageScore(overallAverage != null ?
                BigDecimal.valueOf(overallAverage).setScale(2, RoundingMode.HALF_UP).doubleValue() : null);
        return response;
    }
}

