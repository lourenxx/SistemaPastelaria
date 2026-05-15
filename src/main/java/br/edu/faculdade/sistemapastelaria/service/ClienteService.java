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

import br.edu.faculdade.sistemapastelaria.dto.ClienteCadastroDTO;
import br.edu.faculdade.sistemapastelaria.dto.ClienteDTO;
import br.edu.faculdade.sistemapastelaria.dto.ClienteLoginDTO;
import br.edu.faculdade.sistemapastelaria.dto.SessaoDTO;
import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.repository.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ClienteDTO salvarCliente(ClienteDTO clienteDto) {
        String email = normalizarEmail(clienteDto.getEmail());
        validarEmailUnico(email, null);

        Cliente cliente = new Cliente();
        cliente.setNome(clienteDto.getNome().toUpperCase());
        cliente.setTelefone(clienteDto.getTelefone());
        cliente.setEndereco(clienteDto.getEndereco().toUpperCase());
        cliente.setEmail(email);
        clienteRepository.save(cliente);

        return toDto(cliente);
    }

    public ClienteDTO cadastrarClienteExterno(ClienteCadastroDTO cadastroDto) {
        String nome = normalizarTextoObrigatorio(cadastroDto.getNome(), "Nome deve ser informado").toUpperCase();
        String telefone = normalizarTextoObrigatorio(cadastroDto.getTelefone(), "Telefone deve ser informado");
        String endereco = normalizarTextoObrigatorio(cadastroDto.getEndereco(), "Endereco deve ser informado").toUpperCase();
        String email = normalizarEmailObrigatorio(cadastroDto.getEmail(), HttpStatus.BAD_REQUEST);
        String senha = normalizarSenhaObrigatoria(cadastroDto.getSenha(), HttpStatus.BAD_REQUEST);

        Cliente cliente = clienteRepository.findByEmail(email).orElse(null);

        if (cliente != null && cliente.getSenha() != null && !cliente.getSenha().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ja cadastrado para outro cliente");
        }

        if (cliente != null) {
            if (!normalizarTelefone(cliente.getTelefone()).equals(normalizarTelefone(telefone))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Telefone nao confere com o cadastro existente");
            }

            cliente.setNome(nome);
            cliente.setTelefone(telefone);
            cliente.setEndereco(endereco);
            cliente.setSenha(passwordEncoder.encode(senha));
            clienteRepository.save(cliente);
            return toDto(cliente);
        }

        cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setTelefone(telefone);
        cliente.setEndereco(endereco);
        cliente.setEmail(email);
        cliente.setSenha(passwordEncoder.encode(senha));
        clienteRepository.save(cliente);

        return toDto(cliente);
    }

    public SessaoDTO loginCliente(ClienteLoginDTO loginDto, HttpServletRequest request) {
        String email = normalizarEmailObrigatorio(loginDto.getEmail(), HttpStatus.UNAUTHORIZED);
        String senha = normalizarSenhaObrigatoria(loginDto.getSenha(), HttpStatus.UNAUTHORIZED);

        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos"));

        if (cliente.getSenha() == null || cliente.getSenha().isBlank()
                || !passwordEncoder.matches(senha, cliente.getSenha())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email ou senha invalidos");
        }

        autenticarCliente(request, cliente);
        return toSessao(cliente);
    }

    public Cliente obterClienteAutenticado(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getAuthorities().stream()
                        .noneMatch(authority -> "ROLE_CLIENTE".equals(authority.getAuthority()))) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente nao autenticado");
        }

        return clienteRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Cliente nao encontrado"));
    }

    public ClienteDTO atualizarCliente(ClienteDTO clienteDto) {
        Cliente cliente = clienteRepository.findById(clienteDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));
        String email = normalizarEmail(clienteDto.getEmail());
        validarEmailUnico(email, cliente.getId());

        cliente.setNome(clienteDto.getNome().toUpperCase());
        cliente.setTelefone(clienteDto.getTelefone());
        cliente.setEndereco(clienteDto.getEndereco().toUpperCase());
        cliente.setEmail(email);
        clienteRepository.save(cliente);

        return toDto(cliente);
    }

    public List<ClienteDTO> pesquisarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();

        return clientes.stream()
                .map(this::toDto)
                .toList();
    }

    public ClienteDTO pesquisarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        return toDto(cliente);
    }

    public ClienteDTO excluirCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        clienteRepository.delete(cliente);

        return toDto(cliente);
    }

    public SessaoDTO toSessao(Cliente cliente) {
        return new SessaoDTO("CLIENTE", cliente.getId(), cliente.getNome(), cliente.getEmail());
    }

    private ClienteDTO toDto(Cliente cliente) {
        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getEmail());
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }

        return email.trim().toLowerCase();
    }

    private String normalizarEmailObrigatorio(String email, HttpStatus status) {
        String normalizado = normalizarEmail(email);

        if (normalizado == null) {
            throw new ResponseStatusException(status, status == HttpStatus.UNAUTHORIZED
                    ? "Email ou senha invalidos"
                    : "Email deve ser informado");
        }

        return normalizado;
    }

    private String normalizarSenhaObrigatoria(String senha, HttpStatus status) {
        if (senha == null || senha.isBlank()) {
            throw new ResponseStatusException(status, status == HttpStatus.UNAUTHORIZED
                    ? "Email ou senha invalidos"
                    : "Senha deve ser informada");
        }

        return senha;
    }

    private String normalizarTextoObrigatorio(String valor, String mensagem) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
        }

        return valor.trim();
    }

    private String normalizarTelefone(String telefone) {
        if (telefone == null) {
            return "";
        }

        String digitos = telefone.replaceAll("\\D", "");
        return digitos.isBlank() ? telefone.trim().toLowerCase() : digitos;
    }

    private void validarEmailUnico(String email, Long idAtual) {
        if (email == null) {
            return;
        }

        clienteRepository.findByEmail(email)
                .filter(cliente -> idAtual == null || !cliente.getId().equals(idAtual))
                .ifPresent(cliente -> {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ja cadastrado para outro cliente");
                });
    }

    private void autenticarCliente(HttpServletRequest request, Cliente cliente) {
        request.getSession();
        request.changeSessionId();

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                cliente.getEmail(),
                null,
                AuthorityUtils.createAuthorityList("ROLE_CLIENTE"));

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        request.getSession().setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                securityContext);
    }
}
