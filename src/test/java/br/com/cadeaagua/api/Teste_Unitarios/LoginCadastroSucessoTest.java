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
    void DeveCadastrarUsuarioComSucesso() {
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
    void DeveRealizarLoginComSucesso() {
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

    // TESTE 3: CADASTRO: Verifica se o nome do usuário é salvo corretamente
    @Test
    void DeveSalvarNomeCorretoNoCadastro() {
        Usuario usuario = new Usuario(null, "Carlos Silva", "carlos@teste.com", "123", new Endereco());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(enderecoRepository.save(any())).thenReturn(new Endereco());
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<?> response = authController.register(usuario);
        Usuario salvo = (Usuario) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Carlos Silva", salvo.getNome());
    }

    // TESTE 4: LOGIN: Verifica se o sistema ignora Diferença entre Maiúsculas/Minúsculas no e-mail (se o repo permitir)
    @Test
    void DeveLogarMesmoComEmailEmMaiuscula() {
        Usuario usuarioDB = new Usuario();
        usuarioDB.setEmail("samuel@email.com");
        usuarioDB.setSenha("bolobom123");

        Usuario loginRequest = new Usuario();
        loginRequest.setEmail("SAMUEL@EMAIL.COM");
        loginRequest.setSenha("123");

        when(userRepository.findByEmail("SAMUEL@EMAIL.COM")).thenReturn(Optional.of(usuarioDB));
        when(passwordEncoder.matches("123", "bolobom123")).thenReturn(true);

        ResponseEntity<?> response = authController.login(loginRequest);
        assertEquals(200, response.getStatusCode().value());
    }

    // TESTE 5: CADASTRO: Verifica se o ID do endereço retornado é vinculado ao usuário
    @Test
    void DeveVincularEnderecoSalvoAoUsuario() {
        Endereco enderecoComId = new Endereco();
        enderecoComId.setId_endereco(50); // Simula ID gerado pelo MySQL

        Usuario usuario = new Usuario();
        usuario.setEndereco(new Endereco());

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(enderecoRepository.save(any())).thenReturn(enderecoComId);
        when(userRepository.save(any())).thenReturn(usuario);

        authController.register(usuario);
        assertEquals(50, usuario.getEndereco().getId_endereco());
    }
}
