package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.Bairro;
import br.com.cadeaagua.api.entity.Cronograma;
import br.com.cadeaagua.api.entity.RegiaoAbastecimento;
import br.com.cadeaagua.api.repository.BairroRepository;
import br.com.cadeaagua.api.repository.RegiaoRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
public class StatusAbastecimentoService {
    public static final String STATUS_NORMAL = "Normal";

    private final BairroRepository bairroRepository;
    private final RegiaoRepository regiaoRepository;
    private final CronogramaService cronogramaService;

    public StatusAbastecimentoService(BairroRepository bairroRepository,
                                      RegiaoRepository regiaoRepository,
                                      CronogramaService cronogramaService) {
        this.bairroRepository = bairroRepository;
        this.regiaoRepository = regiaoRepository;
        this.cronogramaService = cronogramaService;
    }

    @Transactional
    public Bairro atualizarStatus(Bairro bairro,
                                  String statusAbastecimento,
                                  String descricao,
                                  LocalDate dataInicio,
                                  LocalDate dataFim) {
        String status = normalizarStatus(statusAbastecimento);
        LocalDate inicio = dataInicio == null ? LocalDate.now() : dataInicio;
        LocalDate fim = dataFim == null ? inicio : dataFim;

        if (fim.isBefore(inicio)) {
            throw new IllegalArgumentException("A data final não pode ser anterior à data inicial.");
        }

        bairro.setStatusAbastecimento(status);
        bairro.setDescricaoStatus(trimOrNull(descricao));
        bairro.setDataAtualizacaoStatus(LocalDateTime.now());
        Bairro bairroSalvo = bairroRepository.save(bairro);

        RegiaoAbastecimento regiao = buscarOuCriarRegiao(bairroSalvo);
        Cronograma cronograma = new Cronograma();
        cronograma.setStatus_abastecimento(status);
        cronograma.setDescricao(trimOrNull(descricao));
        cronograma.setData_inicio(inicio);
        cronograma.setData_fim(fim);
        cronograma.setRegiao(regiao);

        cronogramaService.salvarEAlertar(cronograma);
        return bairroSalvo;
    }

    private RegiaoAbastecimento buscarOuCriarRegiao(Bairro bairro) {
        return regiaoRepository.findFirstByBairroIgnoreCase(bairro.getNome())
                .orElseGet(() -> {
                    RegiaoAbastecimento regiao = new RegiaoAbastecimento();
                    regiao.setBairro(bairro.getNome());
                    regiao.setCodigo_oficial(100000 + bairro.getId());
                    regiao.setDescricao("Região operacional do bairro " + bairro.getNome());
                    regiao.setSetor(bairro.getCidade());
                    regiao.setEstado("PB");
                    return regiaoRepository.save(regiao);
                });
    }

    private String normalizarStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return STATUS_NORMAL;
        }
        return status.trim();
    }

    private String trimOrNull(String valor) {
        return valor == null || valor.trim().isEmpty() ? null : valor.trim();
    }
}
