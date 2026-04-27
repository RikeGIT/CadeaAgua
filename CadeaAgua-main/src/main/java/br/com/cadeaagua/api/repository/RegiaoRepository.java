package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.RegiaoAbastecimento;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegiaoRepository extends JpaRepository<RegiaoAbastecimento, Integer> {
    RegiaoAbastecimento findByBairro(String bairro);
}