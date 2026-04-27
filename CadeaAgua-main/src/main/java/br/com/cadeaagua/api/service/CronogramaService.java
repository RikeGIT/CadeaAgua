package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.Cronograma;
import br.com.cadeaagua.api.entity.RegiaoAbastecimento;
import br.com.cadeaagua.api.repository.CronogramaRepository;
import br.com.cadeaagua.api.repository.RegiaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

@Service
public class CronogramaService {

    @Autowired
    private CronogramaRepository cronogramaRepository;

    @Autowired
    private RegiaoRepository regiaoRepository;

    @Autowired
    private NotificacaoService notificacaoService;

    @Transactional
    public Cronograma salvarEAlertar(Cronograma cronograma) {
        // 1. Busca a Região completa no banco para evitar os campos null
        RegiaoAbastecimento regiaoCompleta = regiaoRepository.findById(cronograma.getRegiao().getId_regiao())
                .orElseThrow(() -> new RuntimeException("Região não encontrada"));

        // 2. Vincula a região populada ao cronograma
        cronograma.setRegiao(regiaoCompleta);

        // 3. Salva no banco de dados
        Cronograma salvo = cronogramaRepository.save(cronograma);

        // 4. Dispara alertas baseados no bairro da região carregada [cite: 6, 10]
        if (!salvo.getStatus_abastecimento().equalsIgnoreCase("Normal")) {
            notificacaoService.processarAlertasDeRodizio(
                    salvo.getRegiao().getBairro(),
                    salvo.getStatus_abastecimento()
            );
        }

        return salvo;
    }
}