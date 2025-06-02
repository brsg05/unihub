package com.unihub.app.repository;

import com.unihub.app.entity.Cadeira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CadeiraRepository extends JpaRepository<Cadeira, Long> {
    Optional<Cadeira> findByNomeAndCursoId(String nome, Long cursoId);
    Boolean existsByNomeAndCursoId(String nome, Long cursoId);
    Boolean existsByCursoId(Long cursoId);
} 