package br.com.cadeaagua.api.repository;

import br.com.cadeaagua.api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Método para buscar por e-mail
    Optional<User> findByEmail(String email);
}
