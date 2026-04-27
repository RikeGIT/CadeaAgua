package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {
    // Busca o histórico de alertas de um usuário específico
    List<Notificacao> findByUsuarioId(Long idUsuario);
}