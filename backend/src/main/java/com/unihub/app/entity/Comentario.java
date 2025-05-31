package com.unihub.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comentarios")
@Getter
@Setter
@NoArgsConstructor
public class Comentario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao; // Avaliação à qual o comentário está ligado

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_id", nullable = false)
    private Criterio criterio; // Critério específico ao qual o comentário se refere

    @Column(name = "votos_positivos", nullable = false)
    private Integer votosPositivos = 0;

    @Column(name = "votos_negativos", nullable = false)
    private Integer votosNegativos = 0;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Comentario(String texto, Avaliacao avaliacao, Criterio criterio) {
        this.texto = texto;
        this.avaliacao = avaliacao;
        this.criterio = criterio;
    }

    public int getScore() {
        return votosPositivos - votosNegativos;
    }
} 