package br.com.cadeaagua.api.Teste_Unitarios;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @Test
    void DeveCadastrarUsuarioComSucesso() {
        Usuario usuario = usuarioValido("Bruno", "bruno@email.com", "83999999999", "senha123");

        when(userRepository.findByEmail("bruno@email.com")).thenReturn(Optional.empty());
        when(enderecoRepository.save(any(Endereco.class))).thenReturn(usuario.getEndereco());
        when(passwordEncoder.encode("senha123")).thenReturn("hash_bcrypt_gerado");
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<?> response = authController.register(usuario);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(AuthController.AuthResponse.class, response.getBody());
        verify(enderecoRepository, times(1)).save(any(Endereco.class));
        verify(userRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    void DeveRealizarLoginComSucesso() {
        Usuario usuarioNoBanco = usuarioValido("Bruno", "bruno@email.com", "83999999999", "bruno12345");

        Usuario dadosLogin = new Usuario();
        dadosLogin.setEmail("bruno@email.com");
        dadosLogin.setSenha("senha123");

        when(userRepository.findByEmail("bruno@email.com")).thenReturn(Optional.of(usuarioNoBanco));
        when(passwordEncoder.matches("senha123", "bruno12345")).thenReturn(true);

        ResponseEntity<?> response = authController.login(dadosLogin);

        assertEquals(200, response.getStatusCode().value());
        assertInstanceOf(AuthController.AuthResponse.class, response.getBody());
    }

    @Test
    void DeveSalvarNomeCorretoNoCadastro() {
        Usuario usuario = usuarioValido("Carlos Silva", "carlos@teste.com", "83888888888", "123");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(enderecoRepository.save(any())).thenReturn(usuario.getEndereco());
        when(userRepository.save(any(Usuario.class))).thenReturn(usuario);

        ResponseEntity<?> response = authController.register(usuario);
        AuthController.AuthResponse salvo = (AuthController.AuthResponse) response.getBody();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(salvo);
        assertEquals("Carlos Silva", salvo.nome());
    }

    @Test
    void DeveLogarMesmoComEmailEmMaiuscula() {
        Usuario usuarioDB = usuarioValido("Samuel", "samuel@email.com", "83777777777", "bolobom123");

        Usuario loginRequest = new Usuario();
        loginRequest.setEmail("SAMUEL@EMAIL.COM");
        loginRequest.setSenha("123");

        when(userRepository.findByEmail("SAMUEL@EMAIL.COM")).thenReturn(Optional.of(usuarioDB));
        when(passwordEncoder.matches("123", "bolobom123")).thenReturn(true);

        ResponseEntity<?> response = authController.login(loginRequest);
        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void DeveVincularEnderecoSalvoAoUsuario() {
        Endereco enderecoComId = enderecoValido();
        enderecoComId.setId_endereco(50);

        Usuario usuario = usuarioValido("Maria", "maria@email.com", "83666666666", "123");

        when(userRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(enderecoRepository.save(any())).thenReturn(enderecoComId);
        when(userRepository.save(any())).thenReturn(usuario);

        authController.register(usuario);
        assertEquals(50, usuario.getEndereco().getId_endereco());
    }

    private Usuario usuarioValido(String nome, String email, String telefone, String senha) {
        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTelefone(telefone);
        usuario.setSenha(senha);
        usuario.setEndereco(enderecoValido());
        return usuario;
    }

    private Endereco enderecoValido() {
        Endereco endereco = new Endereco();
        endereco.setRua("Rua das Flores");
        endereco.setBairro("Centro");
        endereco.setCidade("Patos");
        endereco.setCep("58700-000");
        return endereco;
    }
}
