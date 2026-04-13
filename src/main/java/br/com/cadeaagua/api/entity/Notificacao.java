package br.com.cadeaagua.api.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "notificacao")
@Data
public class Notificacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_notificacao;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000, nullable = false)
    private String mensagem;

    @Column(nullable = false, columnDefinition = "datetime")
    private LocalDateTime data_envio;

    @ManyToOne
    @JoinColumn(name = "id_usuario", nullable = false)
    private Usuario usuario;
}