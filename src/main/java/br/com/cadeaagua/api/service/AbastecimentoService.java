package br.com.cadeaagua.api.service;

import br.com.cadeaagua.api.entity.Cronograma;
import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.entity.RegiaoAbastecimento;
import br.com.cadeaagua.api.repository.CronogramaRepository;
import br.com.cadeaagua.api.repository.EnderecoRepository;
import br.com.cadeaagua.api.repository.RegiaoRepository;
import org.springframework.beans.factory.annotation.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AbastecimentoService {

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private RegiaoRepository regiaoRepository;

    @Autowired
    private CronogramaRepository cronogramaRepository;

    public List<Cronograma> consultarStatusPorEndereco(Integer idEndereco) {
        // 1. Busca o endereço selecionado
        Endereco endereco = enderecoRepository.findById(idEndereco)
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        // 2. Busca a região que atende aquele bairro (Lógica de cruzamento)
        // Nota: Aqui assume-se que 1 bairro = 1 região, conforme a Definição de Dados
        RegiaoAbastecimento regiao = regiaoRepository.findByBairro(endereco.getBairro());

        // 3. Retorna o cronograma daquela região para a data atual
        return cronogramaRepository.findByRegiaoAndData(regiao.getId_regiao(), LocalDate.now());
    }
}
