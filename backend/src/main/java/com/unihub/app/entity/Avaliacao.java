package com.unihub.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Table(name = "avaliacoes",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "professor_id", "cadeira_id", "periodo"})
    })
@Getter
@Setter
@NoArgsConstructor
public class Avaliacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "data_avaliacao", nullable = false)
    private LocalDateTime dataAvaliacao = LocalDateTime.now();

    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String periodo; // Ex: "2023.1"

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user; // Usuário que fez a avaliação

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private Professor professor; // Professor avaliado

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cadeira_id", nullable = false)
    private Cadeira cadeira; // Cadeira (disciplina) avaliada

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<NotaCriterio> notasCriterio = new HashSet<>();

    @OneToMany(mappedBy = "avaliacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comentario> comentarios = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Avaliacao(String periodo, User user, Professor professor, Cadeira cadeira) {
        this.periodo = periodo;
        this.user = user;
        this.professor = professor;
        this.cadeira = cadeira;
    }

    // Helper methods to manage bidirectional relationships
    public void addNotaCriterio(NotaCriterio nota) {
        notasCriterio.add(nota);
        nota.setAvaliacao(this);
    }

    public void removeNotaCriterio(NotaCriterio nota) {
        notasCriterio.remove(nota);
        nota.setAvaliacao(null);
    }

    public void addComentario(Comentario comentario) {
        comentarios.add(comentario);
        comentario.setAvaliacao(this);
    }

    public void removeComentario(Comentario comentario) {
        comentarios.remove(comentario);
        comentario.setAvaliacao(null);
    }
} 