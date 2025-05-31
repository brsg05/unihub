package com.unihub.app.repository;

import com.unihub.app.entity.Criterio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CriterioRepository extends JpaRepository<Criterio, Long> {
    Optional<Criterio> findByNome(String nome);
    Boolean existsByNome(String nome);
} 