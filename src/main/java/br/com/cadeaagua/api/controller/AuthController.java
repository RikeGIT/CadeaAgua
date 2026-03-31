package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.User;
import br.com.cadeaagua.api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // Regra de Negócio: Impedir e-mails duplicados
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: E-mail já cadastrado!");
        }

        // Segurança: Criptografia da senha (ISO 25010)
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        return ResponseEntity.ok(userRepository.save(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // Busca o usuário pelo e-mail
        return userRepository.findByEmail(loginRequest.getEmail())
                .map(user -> {
                    // Verifica se a senha confere
                    if (passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                        return ResponseEntity.ok("Login realizado com sucesso!");
                    }
                    return ResponseEntity.status(401).body("Senha inválida!");
                })
                .orElse(ResponseEntity.status(404).body("Usuário não encontrado!"));
    }
}