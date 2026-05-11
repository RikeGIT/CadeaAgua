package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Bairro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BairroRepository extends JpaRepository<Bairro, Integer> {
    List<Bairro> findAllByOrderByNomeAsc();

    Optional<Bairro> findByNomeIgnoreCaseAndCidadeIgnoreCase(String nome, String cidade);
}
