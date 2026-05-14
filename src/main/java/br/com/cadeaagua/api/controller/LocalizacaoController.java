package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Bairro;
import br.com.cadeaagua.api.entity.Cronograma;
import br.com.cadeaagua.api.entity.Logradouro;
import br.com.cadeaagua.api.repository.BairroRepository;
import br.com.cadeaagua.api.repository.CronogramaRepository;
import br.com.cadeaagua.api.repository.LogradouroRepository;
import br.com.cadeaagua.api.service.StatusAbastecimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/localizacoes")
@CrossOrigin("*")
public class LocalizacaoController {
    private final BairroRepository bairroRepository;
    private final LogradouroRepository logradouroRepository;
    private final CronogramaRepository cronogramaRepository;
    private final StatusAbastecimentoService statusAbastecimentoService;

    public LocalizacaoController(BairroRepository bairroRepository,
                                 LogradouroRepository logradouroRepository,
                                 CronogramaRepository cronogramaRepository,
                                 StatusAbastecimentoService statusAbastecimentoService) {
        this.bairroRepository = bairroRepository;
        this.logradouroRepository = logradouroRepository;
        this.cronogramaRepository = cronogramaRepository;
        this.statusAbastecimentoService = statusAbastecimentoService;
    }

    @GetMapping("/bairros")
    public List<BairroResponse> listarBairros() {
        return bairroRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(bairro -> new BairroResponse(bairro, logradouroRepository.countByBairroId(bairro.getId()), statusAtual(bairro)))
                .toList();
    }

    @GetMapping("/bairros/{bairroId}/logradouros")
    public List<LogradouroResponse> listarLogradouros(@PathVariable Integer bairroId) {
        return logradouroRepository.findByBairroIdOrderByNomeAsc(bairroId)
                .stream()
                .map(LogradouroResponse::new)
                .toList();
    }

    @GetMapping("/logradouros/buscar")
    public ResponseEntity<?> pesquisarLogradouros(@RequestParam(name = "termo", required = false) String termo) {
        if (isBlank(termo) || termo.trim().length() < 2) {
            return ResponseEntity.badRequest().body("Informe pelo menos 2 caracteres para pesquisar.");
        }

        List<PesquisaRuaResponse> resultados = logradouroRepository.buscarPorNome(termo.trim())
                .stream()
                .limit(20)
                .map(logradouro -> new PesquisaRuaResponse(logradouro, statusAtual(logradouro.getBairro())))
                .toList();

        return ResponseEntity.ok(resultados);
    }

    @PostMapping("/bairros")
    public ResponseEntity<?> criarBairro(@RequestBody BairroRequest request) {
        if (request == null || isBlank(request.nome())) {
            return ResponseEntity.badRequest().body("Nome do bairro é obrigatório.");
        }

        String cidade = normalizarCidade(request.cidade());
        if (bairroRepository.findByNomeIgnoreCaseAndCidadeIgnoreCase(request.nome().trim(), cidade).isPresent()) {
            return ResponseEntity.badRequest().body("Bairro já cadastrado.");
        }

        Bairro bairro = new Bairro();
        bairro.setNome(request.nome().trim());
        bairro.setCidade(cidade);
        bairro.setFaixaCep(trimOrNull(request.faixaCep()));

        Bairro bairroSalvo = bairroRepository.save(bairro);
        return ResponseEntity.ok(new BairroResponse(bairroSalvo, 0, statusAtual(bairroSalvo)));
    }

    @PutMapping("/bairros/{bairroId}")
    public ResponseEntity<?> atualizarBairro(@PathVariable Integer bairroId, @RequestBody BairroRequest request) {
        if (request == null || isBlank(request.nome())) {
            return ResponseEntity.badRequest().body("Nome do bairro é obrigatório.");
        }

        return bairroRepository.findById(bairroId)
                .<ResponseEntity<?>>map(bairro -> {
                    String nome = request.nome().trim();
                    String cidade = normalizarCidade(request.cidade());
                    boolean nomeDuplicado = bairroRepository
                            .findByNomeIgnoreCaseAndCidadeIgnoreCase(nome, cidade)
                            .filter(outro -> !outro.getId().equals(bairroId))
                            .isPresent();

                    if (nomeDuplicado) {
                        return ResponseEntity.badRequest().body("Bairro já cadastrado.");
                    }

                    bairro.setNome(nome);
                    bairro.setCidade(cidade);
                    bairro.setFaixaCep(trimOrNull(request.faixaCep()));
                    Bairro bairroSalvo = bairroRepository.save(bairro);
                    return ResponseEntity.ok(new BairroResponse(
                            bairroSalvo,
                            logradouroRepository.countByBairroId(bairroSalvo.getId()),
                            statusAtual(bairroSalvo)
                    ));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/bairros/{bairroId}/status")
    public ResponseEntity<?> atualizarStatusBairro(@PathVariable Integer bairroId, @RequestBody StatusBairroRequest request) {
        if (request == null || isBlank(request.statusAbastecimento())) {
            return ResponseEntity.badRequest().body("Status de abastecimento é obrigatório.");
        }

        return bairroRepository.findById(bairroId)
                .<ResponseEntity<?>>map(bairro -> {
                    try {
                        Bairro bairroAtualizado = statusAbastecimentoService.atualizarStatus(
                                bairro,
                                request.statusAbastecimento(),
                                request.descricao(),
                                request.dataInicio(),
                                request.dataFim()
                        );

                        return ResponseEntity.ok(new BairroResponse(
                                bairroAtualizado,
                                logradouroRepository.countByBairroId(bairroAtualizado.getId()),
                                statusAtual(bairroAtualizado)
                        ));
                    } catch (IllegalArgumentException error) {
                        return ResponseEntity.badRequest().body(error.getMessage());
                    }
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/bairros/{bairroId}")
    @Transactional
    public ResponseEntity<?> excluirBairro(@PathVariable Integer bairroId) {
        if (!bairroRepository.existsById(bairroId)) {
            return ResponseEntity.notFound().build();
        }

        logradouroRepository.deleteByBairroId(bairroId);
        bairroRepository.deleteById(bairroId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bairros/{bairroId}/logradouros")
    public ResponseEntity<?> criarLogradouro(@PathVariable Integer bairroId, @RequestBody LogradouroRequest request) {
        if (request == null || isBlank(request.nome())) {
            return ResponseEntity.badRequest().body("Nome da rua é obrigatório.");
        }

        return bairroRepository.findById(bairroId)
                .<ResponseEntity<?>>map(bairro -> {
                    String nome = request.nome().trim();
                    String cep = trimOrNull(request.cep());

                    if (logradouroRepository.existsByBairroAndNomeIgnoreCaseAndCep(bairro, nome, cep)) {
                        return ResponseEntity.badRequest().body("Rua já cadastrada para este bairro e CEP.");
                    }

                    Logradouro logradouro = new Logradouro();
                    logradouro.setBairro(bairro);
                    logradouro.setNome(nome);
                    logradouro.setCep(cep);
                    return ResponseEntity.ok(new LogradouroResponse(logradouroRepository.save(logradouro)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/logradouros/{logradouroId}")
    public ResponseEntity<?> atualizarLogradouro(@PathVariable Integer logradouroId, @RequestBody LogradouroRequest request) {
        if (request == null || isBlank(request.nome())) {
            return ResponseEntity.badRequest().body("Nome da rua é obrigatório.");
        }

        return logradouroRepository.findById(logradouroId)
                .<ResponseEntity<?>>map(logradouro -> {
                    String nome = request.nome().trim();
                    String cep = trimOrNull(request.cep());
                    boolean duplicado = logradouroRepository
                            .existsByBairroAndNomeIgnoreCaseAndCep(logradouro.getBairro(), nome, cep)
                            && (!logradouro.getNome().equalsIgnoreCase(nome) || !equalsNullable(logradouro.getCep(), cep));

                    if (duplicado) {
                        return ResponseEntity.badRequest().body("Rua já cadastrada para este bairro e CEP.");
                    }

                    logradouro.setNome(nome);
                    logradouro.setCep(cep);
                    return ResponseEntity.ok(new LogradouroResponse(logradouroRepository.save(logradouro)));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/logradouros/{logradouroId}")
    public ResponseEntity<?> excluirLogradouro(@PathVariable Integer logradouroId) {
        if (!logradouroRepository.existsById(logradouroId)) {
            return ResponseEntity.notFound().build();
        }

        logradouroRepository.deleteById(logradouroId);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private String trimOrNull(String valor) {
        return isBlank(valor) ? null : valor.trim();
    }

    private String normalizarCidade(String cidade) {
        return isBlank(cidade) ? "Patos" : cidade.trim();
    }

    private boolean equalsNullable(String primeiro, String segundo) {
        if (primeiro == null) {
            return segundo == null;
        }
        return primeiro.equals(segundo);
    }

    private StatusAbastecimentoResponse statusAtual(Bairro bairro) {
        List<Cronograma> cronogramasAtivos = cronogramaRepository.findAtivosPorBairro(bairro.getNome(), LocalDate.now());
        if (!cronogramasAtivos.isEmpty()) {
            Cronograma cronograma = cronogramasAtivos.get(0);
            return new StatusAbastecimentoResponse(
                    normalizarStatus(cronograma.getStatus_abastecimento()),
                    cronograma.getDescricao(),
                    cronograma.getData_inicio(),
                    cronograma.getData_fim(),
                    bairro.getDataAtualizacaoStatus(),
                    true
            );
        }

        return new StatusAbastecimentoResponse(
                normalizarStatus(bairro.getStatusAbastecimento()),
                bairro.getDescricaoStatus(),
                null,
                null,
                bairro.getDataAtualizacaoStatus(),
                false
        );
    }

    private String normalizarStatus(String status) {
        return isBlank(status) ? StatusAbastecimentoService.STATUS_NORMAL : status.trim();
    }

    public record BairroRequest(String nome, String cidade, String faixaCep) {
    }

    public record LogradouroRequest(String nome, String cep) {
    }

    public record StatusBairroRequest(String statusAbastecimento, String descricao, LocalDate dataInicio, LocalDate dataFim) {
    }

    public record StatusAbastecimentoResponse(String status,
                                              String descricao,
                                              LocalDate dataInicio,
                                              LocalDate dataFim,
                                              LocalDateTime dataAtualizacao,
                                              boolean cronogramaAtivo) {
    }

    public record BairroResponse(Integer id,
                                 String nome,
                                 String cidade,
                                 String faixaCep,
                                 long totalLogradouros,
                                 String statusAbastecimento,
                                 String descricaoStatus,
                                 LocalDate dataInicioStatus,
                                 LocalDate dataFimStatus,
                                 LocalDateTime dataAtualizacaoStatus,
                                 boolean cronogramaAtivo) {
        public BairroResponse(Bairro bairro) {
            this(bairro, 0, new StatusAbastecimentoResponse(
                    StatusAbastecimentoService.STATUS_NORMAL,
                    null,
                    null,
                    null,
                    bairro.getDataAtualizacaoStatus(),
                    false
            ));
        }

        public BairroResponse(Bairro bairro, long totalLogradouros, StatusAbastecimentoResponse status) {
            this(
                    bairro.getId(),
                    bairro.getNome(),
                    bairro.getCidade(),
                    bairro.getFaixaCep(),
                    totalLogradouros,
                    status.status(),
                    status.descricao(),
                    status.dataInicio(),
                    status.dataFim(),
                    status.dataAtualizacao(),
                    status.cronogramaAtivo()
            );
        }
    }

    public record LogradouroResponse(Integer id, String nome, String cep) {
        public LogradouroResponse(Logradouro logradouro) {
            this(logradouro.getId(), logradouro.getNome(), logradouro.getCep());
        }
    }

    public record PesquisaRuaResponse(Integer id,
                                      String nome,
                                      String cep,
                                      Integer bairroId,
                                      String bairroNome,
                                      String cidade,
                                      StatusAbastecimentoResponse abastecimento) {
        public PesquisaRuaResponse(Logradouro logradouro, StatusAbastecimentoResponse status) {
            this(
                    logradouro.getId(),
                    logradouro.getNome(),
                    logradouro.getCep(),
                    logradouro.getBairro().getId(),
                    logradouro.getBairro().getNome(),
                    logradouro.getBairro().getCidade(),
                    status
            );
        }
    }
}
