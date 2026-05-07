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
        Cliente cliente = new Cliente();
        cliente.setNome(clienteDto.getNome().toUpperCase());
        cliente.setTelefone(clienteDto.getTelefone());
        cliente.setEndereco(clienteDto.getEndereco().toUpperCase());
        clienteRepository.save(cliente);

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco());
    }

    public ClienteDTO atualizarCliente(ClienteDTO clienteDto) {
        Cliente cliente = clienteRepository.findById(clienteDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        cliente.setNome(clienteDto.getNome().toUpperCase());
        cliente.setTelefone(clienteDto.getTelefone());
        cliente.setEndereco(clienteDto.getEndereco().toUpperCase());
        clienteRepository.save(cliente);

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco());
    }

    public List<ClienteDTO> pesquisarClientes() {
        List<Cliente> clientes = clienteRepository.findAll();

        return clientes.stream()
                .map(cliente -> new ClienteDTO(
                        cliente.getId(),
                        cliente.getNome(),
                        cliente.getTelefone(),
                        cliente.getEndereco()))
                .toList();
    }

    public ClienteDTO pesquisarPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco());
    }

    public ClienteDTO excluirCliente(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        clienteRepository.delete(cliente);

        return new ClienteDTO(
                cliente.getId(),
                cliente.getNome(),
                cliente.getTelefone(),
                cliente.getEndereco());
    }
}
