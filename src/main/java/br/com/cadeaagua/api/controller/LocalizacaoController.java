package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Bairro;
import br.com.cadeaagua.api.entity.Logradouro;
import br.com.cadeaagua.api.repository.BairroRepository;
import br.com.cadeaagua.api.repository.LogradouroRepository;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/localizacoes")
@CrossOrigin("*")
public class LocalizacaoController {
    private final BairroRepository bairroRepository;
    private final LogradouroRepository logradouroRepository;

    public LocalizacaoController(BairroRepository bairroRepository, LogradouroRepository logradouroRepository) {
        this.bairroRepository = bairroRepository;
        this.logradouroRepository = logradouroRepository;
    }

    @GetMapping("/bairros")
    public List<BairroResponse> listarBairros() {
        return bairroRepository.findAllByOrderByNomeAsc()
                .stream()
                .map(bairro -> new BairroResponse(bairro, logradouroRepository.countByBairroId(bairro.getId())))
                .toList();
    }

    @GetMapping("/bairros/{bairroId}/logradouros")
    public List<LogradouroResponse> listarLogradouros(@PathVariable Integer bairroId) {
        return logradouroRepository.findByBairroIdOrderByNomeAsc(bairroId)
                .stream()
                .map(LogradouroResponse::new)
                .toList();
    }

    @PostMapping("/bairros")
    public ResponseEntity<?> criarBairro(@RequestBody BairroRequest request) {
        if (request == null || isBlank(request.nome())) {
            return ResponseEntity.badRequest().body("Nome do bairro e obrigatorio.");
        }

        String cidade = normalizarCidade(request.cidade());
        if (bairroRepository.findByNomeIgnoreCaseAndCidadeIgnoreCase(request.nome().trim(), cidade).isPresent()) {
            return ResponseEntity.badRequest().body("Bairro ja cadastrado.");
        }

        Bairro bairro = new Bairro();
        bairro.setNome(request.nome().trim());
        bairro.setCidade(cidade);
        bairro.setFaixaCep(trimOrNull(request.faixaCep()));

        return ResponseEntity.ok(new BairroResponse(bairroRepository.save(bairro)));
    }

    @PutMapping("/bairros/{bairroId}")
    public ResponseEntity<?> atualizarBairro(@PathVariable Integer bairroId, @RequestBody BairroRequest request) {
        if (request == null || isBlank(request.nome())) {
            return ResponseEntity.badRequest().body("Nome do bairro e obrigatorio.");
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
                        return ResponseEntity.badRequest().body("Bairro ja cadastrado.");
                    }

                    bairro.setNome(nome);
                    bairro.setCidade(cidade);
                    bairro.setFaixaCep(trimOrNull(request.faixaCep()));
                    Bairro bairroSalvo = bairroRepository.save(bairro);
                    return ResponseEntity.ok(new BairroResponse(bairroSalvo, logradouroRepository.countByBairroId(bairroSalvo.getId())));
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
            return ResponseEntity.badRequest().body("Nome da rua e obrigatorio.");
        }

        return bairroRepository.findById(bairroId)
                .<ResponseEntity<?>>map(bairro -> {
                    String nome = request.nome().trim();
                    String cep = trimOrNull(request.cep());

                    if (logradouroRepository.existsByBairroAndNomeIgnoreCaseAndCep(bairro, nome, cep)) {
                        return ResponseEntity.badRequest().body("Rua ja cadastrada para este bairro e CEP.");
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
            return ResponseEntity.badRequest().body("Nome da rua e obrigatorio.");
        }

        return logradouroRepository.findById(logradouroId)
                .<ResponseEntity<?>>map(logradouro -> {
                    String nome = request.nome().trim();
                    String cep = trimOrNull(request.cep());
                    boolean duplicado = logradouroRepository
                            .existsByBairroAndNomeIgnoreCaseAndCep(logradouro.getBairro(), nome, cep)
                            && (!logradouro.getNome().equalsIgnoreCase(nome) || !equalsNullable(logradouro.getCep(), cep));

                    if (duplicado) {
                        return ResponseEntity.badRequest().body("Rua ja cadastrada para este bairro e CEP.");
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

    public record BairroRequest(String nome, String cidade, String faixaCep) {
    }

    public record LogradouroRequest(String nome, String cep) {
    }

    public record BairroResponse(Integer id, String nome, String cidade, String faixaCep, long totalLogradouros) {
        public BairroResponse(Bairro bairro) {
            this(bairro, 0);
        }

        public BairroResponse(Bairro bairro, long totalLogradouros) {
            this(bairro.getId(), bairro.getNome(), bairro.getCidade(), bairro.getFaixaCep(), totalLogradouros);
        }
    }

    public record LogradouroResponse(Integer id, String nome, String cep) {
        public LogradouroResponse(Logradouro logradouro) {
            this(logradouro.getId(), logradouro.getNome(), logradouro.getCep());
        }
    }
}
