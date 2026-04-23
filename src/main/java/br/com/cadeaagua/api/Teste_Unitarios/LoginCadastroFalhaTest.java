package br.com.cadeaagua.api.Teste_Unitarios;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.cadeaagua.api.controller.AuthController;
import br.com.cadeaagua.api.entity.Usuario;
import br.com.cadeaagua.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LoginCadastroFalhaTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UsuarioRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    // TESTE 3: FALHA NO CADASTRO (E-MAIL JÁ EXISTE)
    @Test
    void naoDevePermitirEmailDuplicadoNoCadastro() {
        Usuario usuarioNovo = new Usuario();
        usuarioNovo.setEmail("bajuju@email.com");

        // Simula que o banco já encontrou este e-mail
        when(userRepository.findByEmail("bajuju@email.com")).thenReturn(Optional.of(new Usuario()));

        ResponseEntity<?> response = authController.register(usuarioNovo);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("E-mail já cadastrado!", response.getBody());
    }

    // TESTE 4: FALHA NO LOGIN (SENHA INCORRETA)
    @Test
    void naoDeveLogarComSenhaIncorreta() {
        Usuario usuarioNoBanco = new Usuario();
        usuarioNoBanco.setSenha("senha12345");

        Usuario dadosLogin = new Usuario();
        dadosLogin.setEmail("jujuba@email.com");
        dadosLogin.setSenha("12345senha");

        when(userRepository.findByEmail("jujuba@email.com")).thenReturn(Optional.of(usuarioNoBanco));
        // Simula que o BCrypt negou a senha
        when(passwordEncoder.matches("12345senha", "senha12345")).thenReturn(false);

        ResponseEntity<?> response = authController.login(dadosLogin);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Senha inválida!", response.getBody());
    }
}