package br.com.cadeaagua.api.controller;


import br.com.cadeaagua.api.entity.RegiaoAbastecimento;
import br.com.cadeaagua.api.repository.RegiaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/regioes")
public class RegiaoController {
    @Autowired
    private RegiaoRepository repository;

    @GetMapping
    public List<RegiaoAbastecimento> listar() { return repository.findAll(); }

    @PostMapping
    public RegiaoAbastecimento criar(@RequestBody RegiaoAbastecimento regiao) {
        return repository.save(regiao);
    }
}
