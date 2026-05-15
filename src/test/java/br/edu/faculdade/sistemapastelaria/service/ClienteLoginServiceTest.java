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
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.ClienteLoginDTO;
import br.edu.faculdade.sistemapastelaria.dto.SessaoDTO;
import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.repository.ClienteRepository;

class ClienteLoginServiceTest {

    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final ClienteService service = new ClienteService(clienteRepository, passwordEncoder);

    @AfterEach
    void limparContextoSeguranca() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarClienteQuandoCredenciaisForemValidas() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cliente cliente = clienteComSenha();

        when(clienteRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("123456", "senha-criptografada")).thenReturn(true);

        SessaoDTO sessao = service.loginCliente(new ClienteLoginDTO(" CLIENTE@EMAIL.COM ", "123456"), request);

        assertEquals("CLIENTE", sessao.getTipo());
        assertEquals(7L, sessao.getId());
        assertEquals("Cliente", sessao.getNome());
        assertEquals("cliente@email.com", sessao.getEmail());
        verify(clienteRepository).findByEmail("cliente@email.com");

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("cliente@email.com", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_CLIENTE".equals(authority.getAuthority())));

        Object sessionContext = request.getSession(false).getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertTrue(sessionContext instanceof SecurityContext);
        assertEquals(authentication, ((SecurityContext) sessionContext).getAuthentication());
    }

    @Test
    void deveRejeitarEmailInexistenteSemValidarSenha() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(clienteRepository.findByEmail("cliente@email.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.loginCliente(new ClienteLoginDTO("cliente@email.com", "123456"), request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Email ou senha invalidos", exception.getReason());
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void deveRejeitarSenhaInvalidaSemAutenticar() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        Cliente cliente = clienteComSenha();

        when(clienteRepository.findByEmail("cliente@email.com")).thenReturn(Optional.of(cliente));
        when(passwordEncoder.matches("errada", "senha-criptografada")).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.loginCliente(new ClienteLoginDTO("cliente@email.com", "errada"), request));

        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Email ou senha invalidos", exception.getReason());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    private Cliente clienteComSenha() {
        Cliente cliente = new Cliente();
        cliente.setId(7L);
        cliente.setNome("Cliente");
        cliente.setTelefone("11999999999");
        cliente.setEndereco("Rua A");
        cliente.setEmail("cliente@email.com");
        cliente.setSenha("senha-criptografada");
        return cliente;
    }
}
