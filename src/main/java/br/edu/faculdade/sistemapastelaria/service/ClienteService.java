package br.edu.faculdade.sistemapastelaria.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.ClienteDTO;
import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.repository.ClienteRepository;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
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

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getEmail());
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

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getEmail());
    }

    public List<ClienteDTO> pesquisarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();

        return clientes.stream()
                .map(cliente -> new ClienteDTO(
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getTelefone(),
                        cliente.getEndereco(),
                        cliente.getEmail()))
                .toList();
    }

    public ClienteDTO pesquisarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco(),
                cliente.getEmail());
    }

    public ClienteDTO excluirCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        clienteRepository.delete(cliente);

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
}
