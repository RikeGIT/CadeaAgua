package br.com.cadeaagua.api.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "regiao_abastecimento")
@Data
public class RegiaoAbastecimento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_regiao;

    @Column(nullable = false)
    private Integer codigo_oficial;

    @Column(length = 1500)
    private String descricao;

    @Column(nullable = false)
    private String bairro;

    @Column(nullable = false)
    private String setor;

    @Column(nullable = false)
    private String estado;
}
