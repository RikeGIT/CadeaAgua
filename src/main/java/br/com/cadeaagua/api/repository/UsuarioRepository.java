package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    List<Usuario> findByEnderecoBairro(String bairro);
    Optional<Usuario> findByEmail(String email);
}