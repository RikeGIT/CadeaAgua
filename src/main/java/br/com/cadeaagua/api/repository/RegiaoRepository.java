package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.RegiaoAbastecimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RegiaoRepository extends JpaRepository<RegiaoAbastecimento, Integer> {
    RegiaoAbastecimento findByBairro(String bairro);

    Optional<RegiaoAbastecimento> findFirstByBairroIgnoreCase(String bairro);
}
