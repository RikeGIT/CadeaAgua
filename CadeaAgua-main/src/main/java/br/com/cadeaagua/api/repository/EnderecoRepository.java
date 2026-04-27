package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.entity.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EnderecoRepository extends JpaRepository<Endereco, Integer> {
    List<Endereco> findByRuaContainingIgnoreCase(String rua);
}
