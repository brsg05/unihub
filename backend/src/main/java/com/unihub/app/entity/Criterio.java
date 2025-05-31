package com.unihub.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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
@Table(name = "criterios",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "nome")
    })
@Getter
@Setter
@NoArgsConstructor
public class Criterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false, unique = true)
    private String nome;

    @OneToMany(mappedBy = "criterio")
    private Set<NotaCriterio> notasCriterio = new HashSet<>();

    @OneToMany(mappedBy = "criterio")
    private Set<Comentario> comentarios = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Criterio(String nome) {
        this.nome = nome;
    }
} 