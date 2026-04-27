package br.com.cadeaagua.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "cronograma")
@Data
public class Cronograma {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_cronograma;

    @Column(length = 50, nullable = false)
    private String status_abastecimento;

    @Column(length = 1000)
    private String descricao;

    @Column(nullable = false)
    private LocalDate data_inicio;

    @Column(nullable = false)
    private LocalDate data_fim;

    @ManyToOne
    @JoinColumn(name = "id_regiao", nullable = false)
    private RegiaoAbastecimento regiao;
}
