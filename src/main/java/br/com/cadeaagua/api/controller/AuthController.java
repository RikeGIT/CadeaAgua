package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.entity.Usuario;
import br.com.cadeaagua.api.repository.EnderecoRepository;
import br.com.cadeaagua.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final String PERFIL_USUARIO = "USUARIO";
    private static final String PERFIL_ADMIN = "ADMIN";

    @Autowired
    private EnderecoRepository enderecoRepository;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Value("${app.admin.secret:cadeagua-admin-2026}")
    private String adminSecret;

    public record AuthResponse(Integer id, String nome, String email, String telefone, String perfil) {
        public AuthResponse(Usuario usuario) {
            this(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTelefone(), normalizarPerfil(usuario.getPerfil()));
        }
    }

    public record AdminRegisterRequest(Usuario usuario, String senhaAdministrador) {
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Usuario usuario) {
        return registrarUsuario(usuario, PERFIL_USUARIO);
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRegisterRequest request) {
        if (request == null || request.usuario() == null) {
            return ResponseEntity.badRequest().body("Dados do usuario sao obrigatorios.");
        }

        if (isBlank(request.senhaAdministrador()) || !request.senhaAdministrador().equals(adminSecret)) {
            return ResponseEntity.status(403).body("Senha de administrador invalida.");
        }

        return registrarUsuario(request.usuario(), PERFIL_ADMIN);
    }

    private ResponseEntity<?> registrarUsuario(Usuario usuario, String perfil) {
        if (usuario == null) {
            return ResponseEntity.badRequest().body("Dados do usuario sao obrigatorios.");
        }

        if (isBlank(usuario.getNome()) || isBlank(usuario.getEmail()) || isBlank(usuario.getTelefone()) || isBlank(usuario.getSenha())) {
            return ResponseEntity.badRequest().body("Nome, email, telefone e senha sao obrigatorios.");
        }

        if (usuario.getEndereco() == null
                || isBlank(usuario.getEndereco().getRua())
                || isBlank(usuario.getEndereco().getBairro())
                || isBlank(usuario.getEndereco().getCidade())
                || isBlank(usuario.getEndereco().getCep())) {
            return ResponseEntity.badRequest().body("Endereco completo e obrigatorio.");
        }

        if (userRepository.findByEmail(usuario.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("E-mail ja cadastrado!");
        }

        Endereco enderecoSalvo = enderecoRepository.save(usuario.getEndereco());
        usuario.setEndereco(enderecoSalvo);
        usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        usuario.setPerfil(normalizarPerfil(perfil));

        Usuario usuarioSalvo = userRepository.save(usuario);
        return ResponseEntity.ok(new AuthResponse(usuarioSalvo));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Usuario loginRequest) {
        if (loginRequest == null || isBlank(loginRequest.getEmail()) || isBlank(loginRequest.getSenha())) {
            return ResponseEntity.badRequest().body("Email e senha sao obrigatorios.");
        }

        return userRepository.findByEmail(loginRequest.getEmail())
                .<ResponseEntity<?>>map(user -> {
                    if (passwordEncoder.matches(loginRequest.getSenha(), user.getSenha())) {
                        return ResponseEntity.ok(new AuthResponse(user));
                    }
                    return ResponseEntity.status(401).body("Senha invalida!");
                })
                .orElseGet(() -> ResponseEntity.status(404).body("Usuario nao encontrado!"));
    }

    private boolean isBlank(String valor) {
        return valor == null || valor.trim().isEmpty();
    }

    private static String normalizarPerfil(String perfil) {
        return PERFIL_ADMIN.equals(perfil) ? PERFIL_ADMIN : PERFIL_USUARIO;
    }
}
