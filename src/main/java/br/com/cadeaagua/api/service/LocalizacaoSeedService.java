package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.Bairro;
import br.com.cadeaagua.api.entity.Logradouro;
import br.com.cadeaagua.api.repository.BairroRepository;
import br.com.cadeaagua.api.repository.LogradouroRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Service
public class LocalizacaoSeedService {
    private static final String CIDADE_PADRAO = "Patos";

    private final BairroRepository bairroRepository;
    private final LogradouroRepository logradouroRepository;

    public LocalizacaoSeedService(BairroRepository bairroRepository, LogradouroRepository logradouroRepository) {
        this.bairroRepository = bairroRepository;
        this.logradouroRepository = logradouroRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void carregarBaseLocal() throws IOException {
        if (bairroRepository.count() > 0) {
            return;
        }

        carregarBairros();
        carregarLogradouros();
    }

    private void carregarBairros() throws IOException {
        try (BufferedReader reader = abrirCsv("data/patos-bairros.csv")) {
            reader.lines()
                    .skip(1)
                    .map(this::limparBom)
                    .filter(linha -> !linha.isBlank())
                    .forEach(this::salvarBairro);
        }
    }

    private void salvarBairro(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 2) {
            return;
        }

        String nome = campos[0].trim();
        String faixaCep = campos[1].trim();
        if (nome.isEmpty()) {
            return;
        }

        Bairro bairro = bairroRepository
                .findByNomeIgnoreCaseAndCidadeIgnoreCase(nome, CIDADE_PADRAO)
                .orElseGet(Bairro::new);
        bairro.setNome(nome);
        bairro.setCidade(CIDADE_PADRAO);
        bairro.setFaixaCep(faixaCep);
        bairroRepository.save(bairro);
    }

    private void carregarLogradouros() throws IOException {
        try (BufferedReader reader = abrirCsv("data/patos-logradouros.csv")) {
            reader.lines()
                    .skip(1)
                    .map(this::limparBom)
                    .filter(linha -> !linha.isBlank())
                    .forEach(this::salvarLogradouro);
        }
    }

    private void salvarLogradouro(String linha) {
        String[] campos = linha.split(";", -1);
        if (campos.length < 3) {
            return;
        }

        String nomeBairro = campos[0].trim();
        String nomeLogradouro = campos[1].trim();
        String cep = campos[2].trim();
        if (nomeBairro.isEmpty() || nomeLogradouro.isEmpty()) {
            return;
        }

        Bairro bairro = bairroRepository
                .findByNomeIgnoreCaseAndCidadeIgnoreCase(nomeBairro, CIDADE_PADRAO)
                .orElseGet(() -> criarBairro(nomeBairro));

        if (!logradouroRepository.existsByBairroAndNomeIgnoreCaseAndCep(bairro, nomeLogradouro, cep)) {
            Logradouro logradouro = new Logradouro();
            logradouro.setBairro(bairro);
            logradouro.setNome(nomeLogradouro);
            logradouro.setCep(cep);
            logradouroRepository.save(logradouro);
        }
    }

    private Bairro criarBairro(String nome) {
        Bairro bairro = new Bairro();
        bairro.setNome(nome);
        bairro.setCidade(CIDADE_PADRAO);
        return bairroRepository.save(bairro);
    }

    private BufferedReader abrirCsv(String caminho) throws IOException {
        ClassPathResource resource = new ClassPathResource(caminho);
        return new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
    }

    private String limparBom(String linha) {
        return linha.replace("\uFEFF", "");
    }
}
