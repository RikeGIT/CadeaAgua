package br.com.cadeaagua.api.controller;

import br.com.cadeaagua.api.entity.Usuario;
import br.com.cadeaagua.api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 1. Método para buscar as informações e exibir no perfil (GET)
    @GetMapping("/{id}")
    public ResponseEntity<?> buscarUsuario(@PathVariable Integer id) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }

        Usuario usuario = usuarioOpt.get();
        // Por segurança, NUNCA devolvemos a senha para o front-end
        usuario.setSenha(null);

        return ResponseEntity.ok(usuario);
    }

    // 2. Método para atualizar o perfil (nome, telefone e opcionalmente a senha) (PUT)
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarUsuario(@PathVariable Integer id, @RequestBody Usuario dadosAtualizados) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }

        Usuario usuarioExistente = usuarioOpt.get();

        // Atualiza apenas os campos permitidos
        if (dadosAtualizados.getFotoPerfil() != null) {
            usuarioExistente.setFotoPerfil(dadosAtualizados.getFotoPerfil());
        }
        if (dadosAtualizados.getNome() != null && !dadosAtualizados.getNome().trim().isEmpty()) {
            usuarioExistente.setNome(dadosAtualizados.getNome());
        }

        if (dadosAtualizados.getTelefone() != null && !dadosAtualizados.getTelefone().trim().isEmpty()) {
            usuarioExistente.setTelefone(dadosAtualizados.getTelefone());
        }

        // Se o usuário mandou uma nova senha, ela é codificada e atualizada
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().trim().isEmpty()) {
            usuarioExistente.setSenha(passwordEncoder.encode(dadosAtualizados.getSenha()));
        }

        usuarioRepository.save(usuarioExistente);

        return ResponseEntity.ok("Perfil atualizado com sucesso!");
    }

    // 3. Método para deletar a própria conta (DELETE)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletarConta(@PathVariable Integer id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Usuário não encontrado.");
        }

        usuarioRepository.deleteById(id);

        return ResponseEntity.ok("Conta deletada com sucesso.");
    }
}