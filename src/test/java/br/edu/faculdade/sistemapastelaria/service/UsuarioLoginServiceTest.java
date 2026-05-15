package br.edu.faculdade.sistemapastelaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.CodigoEmailDTO;
import br.edu.faculdade.sistemapastelaria.dto.LoginCodigoRespostaDTO;
import br.edu.faculdade.sistemapastelaria.dto.LoginDTO;
import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;
import br.edu.faculdade.sistemapastelaria.model.Usuario;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;

class UsuarioLoginServiceTest {

    private final UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final EmailCodigoService emailCodigoService = mock(EmailCodigoService.class);
    private final UsuarioService service = new UsuarioService(
            usuarioRepository,
            passwordEncoder,
            emailCodigoService);

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveIniciarCodigoQuandoCredenciaisForemValidas() {
        MockHttpSession session = new MockHttpSession();
        Usuario usuario = usuarioAtivo();

        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("123456", "senha-criptografada")).thenReturn(true);
        when(emailCodigoService.mascararEmail("admin@email.com")).thenReturn("a***@email.com");

        LoginCodigoRespostaDTO resposta = service.loginUsuario(new LoginDTO(" ADMIN ", "123456"), session);

        assertTrue(resposta.isCodigoEnviado());
        assertEquals("a***@email.com", resposta.getEmailMascarado());
        verify(usuarioRepository).findByLogin("admin");
        verify(emailCodigoService).iniciarCodigo(session, 42L, "admin@email.com");
    }

    @Test
    void deveRejeitarLoginInexistenteSemIniciarCodigo() {
        MockHttpSession session = new MockHttpSession();
        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.loginUsuario(new LoginDTO("admin", "123456"), session));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Login ou senha invalidos", exception.getReason());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verifyNoInteractions(emailCodigoService);
    }

    @Test
    void deveRejeitarSenhaInvalidaSemIniciarCodigo() {
        MockHttpSession session = new MockHttpSession();
        Usuario usuario = usuarioAtivo();

        when(usuarioRepository.findByLogin("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("errada", "senha-criptografada")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.loginUsuario(new LoginDTO("admin", "errada"), session));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Login ou senha invalidos", exception.getReason());
        verify(emailCodigoService, never()).iniciarCodigo(session, 42L, "admin@email.com");
    }

    @Test
    void deveAutenticarUsuarioAposCodigoValido() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession();
        Usuario usuario = usuarioAtivo();

        when(emailCodigoService.validarCodigo(session, "123456")).thenReturn(42L);
        when(usuarioRepository.findById(42L)).thenReturn(Optional.of(usuario));

        UsuarioDTO resposta = service.verificarCodigo(new CodigoEmailDTO("123456"), request);

        assertEquals(42L, resposta.getId());
        assertEquals("Administrador", resposta.getNome());
        assertEquals("admin", resposta.getLogin());
        assertEquals("admin@email.com", resposta.getEmail());
        assertNull(resposta.getSenha());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("admin", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_INTERNO".equals(authority.getAuthority())));

        Object sessionContext = request.getSession(false).getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertTrue(sessionContext instanceof SecurityContext);
        assertEquals(authentication, ((SecurityContext) sessionContext).getAuthentication());
    }

    @Test
    void deveRejeitarCodigoValidoQuandoUsuarioEstiverInativo() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpSession session = (MockHttpSession) request.getSession();
        Usuario usuario = usuarioAtivo();
        usuario.setAtivo(false);

        when(emailCodigoService.validarCodigo(session, "123456")).thenReturn(42L);
        when(usuarioRepository.findById(42L)).thenReturn(Optional.of(usuario));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.verificarCodigo(new CodigoEmailDTO("123456"), request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Usuario inativo", exception.getReason());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private Usuario usuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setId(42L);
        usuario.setNome("Administrador");
        usuario.setLogin("admin");
        usuario.setEmail("admin@email.com");
        usuario.setSenha("senha-criptografada");
        usuario.setAtivo(true);
        return usuario;
    }
}
