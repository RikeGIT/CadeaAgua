package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.repository.EnderecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enderecos")
@CrossOrigin("*") // Permite acesso do seu frontend
public class EnderecoController {
    @Autowired
    private EnderecoRepository repository;

    @GetMapping
    public List<Endereco> listar() { return repository.findAll(); }

    @PostMapping
    public Endereco criar(@RequestBody Endereco endereco) { return repository.save(endereco); }

    @GetMapping("/buscar")
    public List<Endereco> buscarPorRua(@RequestParam String rua) {
        return repository.findByRuaContainingIgnoreCase(rua);
    }
}