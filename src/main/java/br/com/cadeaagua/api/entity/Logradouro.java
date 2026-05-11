package br.com.cadeaagua.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(
        name = "logradouro",
        uniqueConstraints = @UniqueConstraint(columnNames = {"id_bairro", "nome", "cep"})
)
@Data
public class Logradouro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 160)
    private String nome;

    @Column(length = 9)
    private String cep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_bairro", nullable = false)
    private Bairro bairro;
}
