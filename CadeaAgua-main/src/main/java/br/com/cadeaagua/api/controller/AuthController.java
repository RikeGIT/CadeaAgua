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
        if (userRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail já cadastrado!");
        }

        // Salva o endereço novo no banco primeiro, confia no processo.
        // Isso ta gerando o id_endereco no MySQL
        Endereco enderecoSalvo = enderecoRepository.save(usuario.getEndereco());

        // 3. Vincula o endereço salvo (com ID) ao usuário
        usuario.setEndereco(enderecoSalvo);


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