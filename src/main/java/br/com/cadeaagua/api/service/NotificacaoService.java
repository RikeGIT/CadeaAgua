package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.*;
import br.com.cadeaagua.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Dispara alertas para todos os usuários vinculados ao bairro em rodízio.
     */
    public void processarAlertasDeRodizio(String bairro, String statusCronograma) {
        // 1. Busca os usuários filtrando pelo relacionamento com Endereço
        List<Usuario> moradores = usuarioRepository.findByEnderecoBairro(bairro);

        String mensagemAlerta = String.format(
                "Atenção: O status de abastecimento para o bairro %s mudou para: %s.",
                bairro, statusCronograma
        );

        // 2. Persiste as notificações individualmente para o histórico
        for (Usuario morador : moradores) {
            Notificacao notificacao = new Notificacao();
            notificacao.setTitulo("Alerta Cadê a Água?");
            notificacao.setMensagem(mensagemAlerta);
            notificacao.setData_envio(LocalDateTime.now());
            notificacao.setUsuario(morador); // FK para a tabela users

            notificacaoRepository.save(notificacao);
        }
    }
}