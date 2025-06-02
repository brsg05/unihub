package com.unihub.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "cadeiras",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"nome", "curso_id"})
    })
@Getter
@Setter
@NoArgsConstructor
public class Cadeira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false)
    private String nome;

    @Positive
    @Column(name = "carga_horaria", nullable = false)
    private Integer cargaHoraria;

    @Column(name = "is_eletiva", nullable = false)
    private Boolean isEletiva = false;

    @NotNull(message = "Curso não pode ser nulo")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curso_id", nullable = false)
    private Curso curso;

    @ManyToMany(mappedBy = "cadeiras", fetch = FetchType.LAZY)
    private Set<Professor> professores = new HashSet<>();

    @OneToMany(mappedBy = "cadeira", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private Set<Avaliacao> avaliacoes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Cadeira(String nome, Integer cargaHoraria, Boolean isEletiva, Curso curso) {
        this.nome = nome;
        this.cargaHoraria = cargaHoraria;
        this.isEletiva = isEletiva;
        this.curso = curso;
    }
} 