package br.edu.faculdade.sistemapastelaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.ClienteDTO;
import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;

@SpringBootTest
@Transactional
class EmailValidationServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private ClienteService clienteService;

    @Test
    void deveNormalizarEmailDeUsuarioEBarrarDuplicado() {
        UsuarioDTO usuario = usuarioService.salvarUsuario(new UsuarioDTO(
                null,
                "Admin",
                "admin",
                " ADMIN@EMAIL.COM ",
                "123456"));

        assertEquals("admin@email.com", usuario.getEmail());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> usuarioService.salvarUsuario(new UsuarioDTO(
                        null,
                        "Outro",
                        "outro",
                        "admin@email.com",
                        "123456")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deveNormalizarEmailDeClienteEBarrarDuplicado() {
        ClienteDTO cliente = clienteService.salvarCliente(new ClienteDTO(
                null,
                "Cliente",
                "11999999999",
                "Rua A",
                " CLIENTE@EMAIL.COM "));

        assertEquals("cliente@email.com", cliente.getEmail());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> clienteService.salvarCliente(new ClienteDTO(
                        null,
                        "Outro Cliente",
                        "11888888888",
                        "Rua B",
                        "cliente@email.com")));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
