package com.unihub.app.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "professores")
@Getter
@Setter
@NoArgsConstructor
public class Professor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 255)
    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Size(max = 500)
    @Column(name = "photo_url")
    private String photoUrl;

    @Column(name = "nota_geral", precision = 3, scale = 2)
    private BigDecimal notaGeral; // Calculada

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "professor_cadeiras",
               joinColumns = @JoinColumn(name = "professor_id"),
               inverseJoinColumns = @JoinColumn(name = "cadeira_id"))
    private Set<Cadeira> cadeiras = new HashSet<>();

    @OneToMany(mappedBy = "professor")
    private Set<Avaliacao> avaliacoes = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public Professor(String nomeCompleto, String photoUrl) {
        this.nomeCompleto = nomeCompleto;
        this.photoUrl = photoUrl;
    }
} 