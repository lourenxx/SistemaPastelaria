package br.edu.faculdade.sistemapastelaria.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.SessaoDTO;
import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.model.Usuario;
import br.edu.faculdade.sistemapastelaria.repository.ClienteRepository;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/sessao")
public class SessaoController {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;

    public SessaoController(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
    }

    @GetMapping
    public SessaoDTO sessaoAtual(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao nao autenticada");
        }

        if (temAutoridade(authentication, "ROLE_CLIENTE")) {
            Cliente cliente = clienteRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente nao encontrado"));

            return new SessaoDTO("CLIENTE", cliente.getId(), cliente.getNome(), cliente.getEmail());
        }

        if (temAutoridade(authentication, "ROLE_INTERNO")) {
            Usuario usuario = usuarioRepository.findByLogin(authentication.getName())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));

            return new SessaoDTO("INTERNO", usuario.getId(), usuario.getNome(), usuario.getEmail());
        }

        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Perfil sem permissao");
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
    }

    private boolean temAutoridade(Authentication authentication, String autoridade) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> autoridade.equals(authority.getAuthority()));
    }
}
