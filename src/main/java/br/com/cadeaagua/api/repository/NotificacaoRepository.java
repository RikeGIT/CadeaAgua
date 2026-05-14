package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface NotificacaoRepository extends JpaRepository<Notificacao, Integer> {
    // Busca o histórico de alertas de um usuário específico
    @Query("SELECT n FROM Notificacao n WHERE n.usuario.id = :usuarioId " +
            "ORDER BY n.data_envio DESC, n.id_notificacao DESC")
    List<Notificacao> findHistoricoPorUsuario(@Param("usuarioId") Integer usuarioId);
}
