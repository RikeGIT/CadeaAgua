package br.com.cadeaagua.api.Teste_Unitarios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.cadeaagua.api.controller.AuthController;
import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.entity.Usuario;
import br.com.cadeaagua.api.repository.EnderecoRepository;
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
class LoginCadastroSucessoTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UsuarioRepository userRepository;

    @Mock
    private EnderecoRepository enderecoRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    // TESTE 1: CADASTRO COM SUCESSO
    @Test
    void deveCadastrarUsuarioComSucesso() {
        Endereco endereco = new Endereco();
        Usuario usuario = new Usuario(null, "Bruno", "bruno@email.com", "senha123", endereco);

        // Configura o comportamento dos mocks
        when(userRepository.findByEmail("bruno@email.com")).thenReturn(Optional.empty());
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(endereco);
        when(passwordEncoder.encode("senha123")).thenReturn("hash_bcrypt_gerado");
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<?> response = authController.register(usuario);

        assertEquals(200, response.getStatusCode().value());
        verify(enderecoRepository, times(1)).save(any(Endereco.class));
        verify(userRepository, times(1)).save(any(Usuario.class));
    }

    // TESTE 2: LOGIN COM SUCESSO
    @Test
    void deveRealizarLoginComSucesso() {
        Usuario usuarioNoBanco = new Usuario();
        usuarioNoBanco.setEmail("bruno@email.com");
        usuarioNoBanco.setSenha("bruno12345");

        Usuario dadosLogin = new Usuario();
        dadosLogin.setEmail("bruno@email.com");
        dadosLogin.setSenha("senha123");

        when(userRepository.findByEmail("bruno@email.com")).thenReturn(Optional.of(usuarioNoBanco));
        when(passwordEncoder.matches("senha123", "bruno12345")).thenReturn(true);

        ResponseEntity<?> response = authController.login(dadosLogin);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Login realizado com sucesso!", response.getBody());
    }
}