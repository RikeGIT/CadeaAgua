package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Notificacao;
import br.com.cadeaagua.api.repository.UsuarioRepository;
import br.com.cadeaagua.api.service.NotificacaoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/notificacoes")
@CrossOrigin("*")
public class NotificacaoController {
    private final NotificacaoService notificacaoService;
    private final UsuarioRepository usuarioRepository;

    public NotificacaoController(NotificacaoService notificacaoService, UsuarioRepository usuarioRepository) {
        this.notificacaoService = notificacaoService;
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/usuarios/{usuarioId}")
    public ResponseEntity<?> listarPorUsuario(@PathVariable Integer usuarioId) {
        if (!usuarioRepository.existsById(usuarioId)) {
            return ResponseEntity.notFound().build();
        }

        List<NotificacaoResponse> notificacoes = notificacaoService.listarHistoricoPorUsuario(usuarioId)
                .stream()
                .map(NotificacaoResponse::new)
                .toList();

        return ResponseEntity.ok(notificacoes);
    }

    public record NotificacaoResponse(Integer id,
                                      String titulo,
                                      String mensagem,
                                      LocalDateTime dataEnvio) {
        public NotificacaoResponse(Notificacao notificacao) {
            this(
                    notificacao.getId_notificacao(),
                    notificacao.getTitulo(),
                    notificacao.getMensagem(),
                    notificacao.getData_envio()
            );
        }
    }
}
