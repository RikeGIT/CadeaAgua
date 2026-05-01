package br.com.cadeaagua.api.Teste_Unitarios;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import br.com.cadeaagua.api.controller.AuthController;
import br.com.cadeaagua.api.entity.Endereco;
import br.com.cadeaagua.api.entity.Usuario;
import br.com.cadeaagua.api.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class LoginCadastroFalhaTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UsuarioRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Test
    void NaoDevePermitirEmailDuplicadoNoCadastro() {
        Usuario usuarioNovo = usuarioValido();

        when(userRepository.findByEmail("bajuju@email.com")).thenReturn(Optional.of(new Usuario()));

        ResponseEntity<?> response = authController.register(usuarioNovo);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("E-mail ja cadastrado!", response.getBody());
    }

    @Test
    void NaoDeveLogarComSenhaIncorreta() {
        Usuario usuarioNoBanco = new Usuario();
        usuarioNoBanco.setSenha("senha12345");

        Usuario dadosLogin = new Usuario();
        dadosLogin.setEmail("jujuba@email.com");
        dadosLogin.setSenha("12345senha");

        when(userRepository.findByEmail("jujuba@email.com")).thenReturn(Optional.of(usuarioNoBanco));
        when(passwordEncoder.matches("12345senha", "senha12345")).thenReturn(false);

        ResponseEntity<?> response = authController.login(dadosLogin);

        assertEquals(401, response.getStatusCode().value());
        assertEquals("Senha invalida!", response.getBody());
    }

    @Test
    void DeveRetornar400QuandoSenhaNaoFoiEnviadaNoLogin() {
        Usuario loginRequest = new Usuario();
        loginRequest.setEmail("paoqueijo@email.com");

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email e senha sao obrigatorios.", response.getBody());
    }

    @Test
    void DeveRetornar404QuandoEmailNaoExiste() {
        Usuario loginRequest = new Usuario();
        loginRequest.setEmail("paoqueijo@email.com");
        loginRequest.setSenha("123");

        when(userRepository.findByEmail("paoqueijo@email.com")).thenReturn(Optional.empty());

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(404, response.getStatusCode().value());
        assertEquals("Usuario nao encontrado!", response.getBody());
    }

    @Test
    void DeveRetornar400SeEnderecoForNulo() {
        Usuario usuarioSemEndereco = usuarioValido();
        usuarioSemEndereco.setEndereco(null);

        ResponseEntity<?> response = authController.register(usuarioSemEndereco);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Endereco completo e obrigatorio.", response.getBody());
    }

    @Test
    void DeveFalharSeSenhaForVaziaNoLogin() {
        Usuario loginRequest = new Usuario();
        loginRequest.setEmail("maria@email.com");
        loginRequest.setSenha("");

        ResponseEntity<?> response = authController.login(loginRequest);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void DeveNegarCadastroAdminComSenhaInvalida() {
        ReflectionTestUtils.setField(authController, "adminSecret", "segredo-admin");
        AuthController.AdminRegisterRequest request = new AuthController.AdminRegisterRequest(usuarioValido(), "senha-errada");

        ResponseEntity<?> response = authController.registerAdmin(request);

        assertEquals(403, response.getStatusCode().value());
        assertEquals("Senha de administrador invalida.", response.getBody());
        verify(userRepository, never()).save(any(Usuario.class));
    }

    private Usuario usuarioValido() {
        Usuario usuario = new Usuario();
        usuario.setNome("Bajuju");
        usuario.setEmail("bajuju@email.com");
        usuario.setTelefone("83999999999");
        usuario.setSenha("123");

        Endereco endereco = new Endereco();
        endereco.setRua("Rua das Flores");
        endereco.setBairro("Centro");
        endereco.setCidade("Patos");
        endereco.setCep("58700-000");
        usuario.setEndereco(endereco);

        return usuario;
    }
}
