package br.edu.faculdade.sistemapastelaria.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.CodigoEmailDTO;
import br.edu.faculdade.sistemapastelaria.dto.LoginDTO;
import br.edu.faculdade.sistemapastelaria.dto.LoginCodigoRespostaDTO;
import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;
import br.edu.faculdade.sistemapastelaria.model.Usuario;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Service
public class UsuarioService {

    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;
    private final EmailCodigoService emailCodigoService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
            EmailCodigoService emailCodigoService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailCodigoService = emailCodigoService;
    }

    public synchronized UsuarioDTO salvarUsuario(UsuarioDTO usuarioDto) {
        if (usuarioDto == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario deve ser informado");
        }

        validarCriacaoUsuarioPermitida();
        String nome = normalizarNomeObrigatorio(usuarioDto.getNome());
        String login = normalizarLoginCadastro(usuarioDto.getLogin());
        String email = normalizarEmailObrigatorio(usuarioDto.getEmail());
        String senha = normalizarSenhaObrigatoria(usuarioDto.getSenha());
        validarLoginUnico(login, null);
        validarEmailUnico(email, null);

        Usuario usuario = new Usuario();
        usuario.setNome(nome);
        usuario.setLogin(login);
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        return toDto(usuario);
    }

    public LoginCodigoRespostaDTO loginUsuario(LoginDTO loginDto, HttpSession session) {
        String login = normalizarLogin(loginDto.getLogin());

        if (loginDto.getSenha() == null || loginDto.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos");
        }

        Usuario usuario = usuarioRepository.findByLogin(login)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos"));

        if (!usuario.isAtivo() || !passwordEncoder.matches(loginDto.getSenha(), usuario.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos");
        }

        emailCodigoService.iniciarCodigo(session, usuario.getId(), usuario.getEmail());
        return new LoginCodigoRespostaDTO(true, emailCodigoService.mascararEmail(usuario.getEmail()));
    }

    public UsuarioDTO verificarCodigo(CodigoEmailDTO codigoDto, HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Long usuarioId = emailCodigoService.validarCodigo(session, codigoDto.getCodigo());

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario nao encontrado"));

        if (!usuario.isAtivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Usuario inativo");
        }

        autenticarUsuario(request, usuario);

        return toDto(usuario);
    }

    public UsuarioDTO atualizarUsuario(UsuarioDTO usuarioDto) {
        Usuario usuario = usuarioRepository.findById(usuarioDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));
        String email = normalizarEmailObrigatorio(usuarioDto.getEmail());
        validarEmailUnico(email, usuario.getId());

        usuario.setNome(usuarioDto.getNome().toUpperCase());
        usuario.setLogin(usuarioDto.getLogin().toLowerCase());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(usuarioDto.getSenha()));
        usuarioRepository.save(usuario);

        return toDto(usuario);
    }

    public List<UsuarioDTO> pesquisarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream()
                .map(this::toDto)
                .toList();
    }

    public UsuarioDTO pesquisarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        return toDto(usuario);
    }

    public UsuarioDTO excluirUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        usuario.setAtivo(false);
        usuarioRepository.delete(usuario);

        return toDto(usuario);
    }

    private UsuarioDTO toDto(Usuario usuario) {
        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getEmail(),
                null);
    }

    private String normalizarLogin(String login) {
        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login ou senha invalidos");
        }

        return login.trim().toLowerCase();
    }

    private String normalizarNomeObrigatorio(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nome deve ser informado");
        }

        return nome.trim().toUpperCase();
    }

    private String normalizarLoginCadastro(String login) {
        if (login == null || login.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login deve ser informado");
        }

        return login.trim().toLowerCase();
    }

    private String normalizarSenhaObrigatoria(String senha) {
        if (senha == null || senha.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Senha deve ser informada");
        }

        return senha.trim();
    }

    private String normalizarEmailObrigatorio(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email deve ser informado");
        }

        return email.trim().toLowerCase();
    }

    private void validarEmailUnico(String email, Long idAtual) {
        usuarioRepository.findByEmail(email)
                .filter(usuario -> idAtual == null || usuario.getId() != idAtual)
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ja cadastrado para outro usuario");
                });
    }

    private void validarLoginUnico(String login, Long idAtual) {
        usuarioRepository.findByLogin(login)
                .filter(usuario -> idAtual == null || usuario.getId() != idAtual)
                .ifPresent(usuario -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login ja cadastrado para outro usuario");
                });
    }

    private void validarCriacaoUsuarioPermitida() {
        if (usuarioRepository.count() == 0) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getAuthorities().stream()
                        .anyMatch(authority -> "ROLE_INTERNO".equals(authority.getAuthority()))) {
            return;
        }

        throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Apenas usuarios internos autenticados podem criar novos usuarios");
    }

    private void autenticarUsuario(HttpServletRequest request, Usuario usuario) {
        request.changeSessionId();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                usuario.getLogin(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_INTERNO"));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);
    }
}
