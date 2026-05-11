package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Bairro;
import br.com.cadeaagua.api.entity.Logradouro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogradouroRepository extends JpaRepository<Logradouro, Integer> {
    List<Logradouro> findByBairroIdOrderByNomeAsc(Integer bairroId);

    long countByBairroId(Integer bairroId);

    boolean existsByBairroAndNomeIgnoreCaseAndCep(Bairro bairro, String nome, String cep);

    void deleteByBairroId(Integer bairroId);
}
