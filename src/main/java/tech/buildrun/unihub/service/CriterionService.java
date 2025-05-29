package tech.buildrun.unihub.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.buildrun.unihub.dto.CriterionRequest;
import tech.buildrun.unihub.dto.CriterionResponse;
import tech.buildrun.unihub.entity.Criterion;
import tech.buildrun.unihub.exception.ResourceNotFoundException;
import tech.buildrun.unihub.exception.ValidationException;
import tech.buildrun.unihub.repository.CriterionRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço para gerenciamento de critérios de avaliação (Admin-only).
 */
@Service
@RequiredArgsConstructor
public class CriterionService {

    private final CriterionRepository criterionRepository;

    /**
     * Converte uma entidade Criterion para CriterionResponse DTO.
     */
    private CriterionResponse toCriterionResponse(Criterion criterion) {
        return new CriterionResponse(criterion.getId(), criterion.getName(), criterion.getDescription(), criterion.getCreatedAt(), criterion.getUpdatedAt());
    }

    /**
     * Obtém todos os critérios. Requer role ADMIN.
     * @return Lista de CriterionResponse.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<CriterionResponse> getAllCriteria() {
        return criterionRepository.findAll().stream()
                .map(this::toCriterionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Obtém um critério por ID. Requer role ADMIN.
     * @param id ID do critério.
     * @return CriterionResponse.
     * @throws ResourceNotFoundException se o critério não for encontrado.
     */
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public CriterionResponse getCriterionById(UUID id) {
        Criterion criterion = criterionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Critério", "id", id));
        return toCriterionResponse(criterion);
    }

    /**
     * Cria um novo critério. Requer role ADMIN.
     * @param request DTO com dados do critério.
     * @return CriterionResponse do critério criado.
     * @throws ValidationException se o nome do critério já existir.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CriterionResponse createCriterion(CriterionRequest request) {
        if (criterionRepository.existsByName(request.getName())) {
            throw new ValidationException("Já existe um critério com o nome '" + request.getName() + "'.");
        }

        Criterion criterion = new Criterion();
        criterion.setName(request.getName());
        criterion.setDescription(request.getDescription());

        Criterion savedCriterion = criterionRepository.save(criterion);
        return toCriterionResponse(savedCriterion);
    }

    /**
     * Atualiza um critério existente. Requer role ADMIN.
     * @param id ID do critério a ser atualizado.
     * @param request DTO com dados atualizados.
     * @return CriterionResponse do critério atualizado.
     * @throws ResourceNotFoundException se o critério não for encontrado.
     * @throws ValidationException se o nome do critério já existir por outro critério.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public CriterionResponse updateCriterion(UUID id, CriterionRequest request) {
        Criterion criterion = criterionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Critério", "id", id));

        if (!criterion.getName().equals(request.getName()) && criterionRepository.existsByName(request.getName())) {
            throw new ValidationException("Já existe outro critério com o nome '" + request.getName() + "'.");
        }

        criterion.setName(request.getName());
        criterion.setDescription(request.getDescription());

        Criterion updatedCriterion = criterionRepository.save(criterion);
        return toCriterionResponse(updatedCriterion);
    }

    /**
     * Deleta um critério. Requer role ADMIN.
     * @param id ID do critério a ser deletado.
     * @throws ResourceNotFoundException se o critério não for encontrado.
     */
    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteCriterion(UUID id) {
        if (!criterionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Critério", "id", id);
        }
        criterionRepository.deleteById(id);
    }
}