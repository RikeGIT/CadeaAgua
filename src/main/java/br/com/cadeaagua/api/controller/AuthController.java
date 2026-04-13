package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.entity.Usuario;
import br.com.cadeaagua.api.repository.EnderecoRepository;
import br.com.cadeaagua.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        // Regra de Negócio: Impedir e-mails duplicados
        if (userRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Erro: E-mail já cadastrado!");
        }
        Endereco enderecoCompleto = enderecoRepository.findById(usuario.getEndereco().getId_endereco())
                .orElseThrow(() -> new RuntimeException("Endereço não encontrado"));

        usuario.setEndereco(enderecoCompleto);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        // Segurança: Criptografia da senha (ISO 25010)
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));

        return ResponseEntity.ok(userRepository.save(usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        // Busca o usuário pelo e-mail
        return userRepository.findByEmail(loginRequest.getEmail())
                .map(user -> {
                    // Verifica se a senha confere
                    if (passwordEncoder.matches(loginRequest.getSenha(), user.getSenha())) {
                        return ResponseEntity.ok("Login realizado com sucesso!");
                    }
                    return ResponseEntity.status(401).body("Senha inválida!");
                })
                .orElse(ResponseEntity.status(404).body("Usuário não encontrado!"));
    }
}