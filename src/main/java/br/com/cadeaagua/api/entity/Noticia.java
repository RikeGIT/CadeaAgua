package br.com.cadeaagua.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name="noticias")
@Data
public class Noticia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_notica;

    @Column (length = 300, nullable = false)
    private String titulo;

    @Column (length = 2000, nullable = false)
    private String descricao;

    @Column (length = 200)
    private String imagem_url;

    @Column (nullable = false)
    private LocalDate data_publicacao;
}