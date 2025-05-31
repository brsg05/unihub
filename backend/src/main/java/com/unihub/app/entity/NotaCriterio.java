package com.unihub.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "nota_criterios",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"avaliacao_id", "criterio_id"})
    })
@Getter
@Setter
@NoArgsConstructor
public class NotaCriterio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "avaliacao_id", nullable = false)
    private Avaliacao avaliacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criterio_id", nullable = false)
    private Criterio criterio;

    @NotNull
    @Min(1)
    @Max(5) // Ex: escala de 1 a 5. Ajustar conforme a regra de negócio.
    @Column(nullable = false)
    private Integer nota;

    public NotaCriterio(Avaliacao avaliacao, Criterio criterio, Integer nota) {
        this.avaliacao = avaliacao;
        this.criterio = criterio;
        this.nota = nota;
    }
} 