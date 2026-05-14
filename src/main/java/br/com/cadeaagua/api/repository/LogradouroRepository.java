package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Bairro;
import br.com.cadeaagua.api.entity.Logradouro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LogradouroRepository extends JpaRepository<Logradouro, Integer> {
    List<Logradouro> findByBairroIdOrderByNomeAsc(Integer bairroId);

    @Query("SELECT l FROM Logradouro l JOIN FETCH l.bairro " +
            "WHERE LOWER(l.nome) LIKE LOWER(CONCAT('%', :termo, '%')) " +
            "ORDER BY l.nome ASC")
    List<Logradouro> buscarPorNome(@Param("termo") String termo);

    long countByBairroId(Integer bairroId);

    boolean existsByBairroAndNomeIgnoreCaseAndCep(Bairro bairro, String nome, String cep);

    void deleteByBairroId(Integer bairroId);
}
