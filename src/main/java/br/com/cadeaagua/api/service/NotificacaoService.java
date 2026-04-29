package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.*;
import br.com.cadeaagua.api.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private final RestTemplate restTemplate = new RestTemplate();

    // n8n url
    private final String N8N_URL = "https://webhook.testesamas.me/webhook/cadeaAgua";

    /**
     * Dispara alertas para todos os utilizadores vinculados ao bairro em rodízio.
     */
    public void processarAlertasDeRodizio(String bairro, String statusCronograma) {
        // 1. Busca os utilizadores filtrando pelo relacionamento com Endereço
        List<Usuario> moradores = usuarioRepository.findByEnderecoBairro(bairro);

        String mensagemAlerta = String.format(
                "Atenção: O status de abastecimento para o bairro %s mudou para: %s.",
                bairro, statusCronograma
        );

        // 2. Persiste as notificações individualmente para o histórico e dispara o Webhook
        for (Usuario morador : moradores) {

            // Salva na Base de Dados
            Notificacao notificacao = new Notificacao();
            notificacao.setTitulo("Alerta Cadê a Água?");
            notificacao.setMensagem(mensagemAlerta);
            notificacao.setData_envio(LocalDateTime.now());
            notificacao.setUsuario(morador);

            notificacaoRepository.save(notificacao);

            // n8n
            enviarParaN8n(morador, bairro, statusCronograma, mensagemAlerta);
        }
    }

    private void enviarParaN8n(Usuario morador, String bairro, String status, String mensagem) {
        try {
            // payload JSON
            Map<String, Object> payload = new HashMap<>();
            payload.put("nome", morador.getNome());
            payload.put("email", morador.getEmail());
            payload.put("telefone", morador.getTelefone());
            payload.put("bairro", bairro);
            payload.put("status_abastecimento", status);
            payload.put("mensagem", mensagem);

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            // POST
            restTemplate.postForEntity(N8N_URL, request, String.class);

            System.out.println("Webhook enviado ao n8n com sucesso para: " + morador.getNome());

        } catch (Exception e) {
            // n8n offline, avisa a gente
            System.err.println("Falha ao enviar webhook ao n8n para " + morador.getNome() + ": " + e.getMessage());
        }
    }
}