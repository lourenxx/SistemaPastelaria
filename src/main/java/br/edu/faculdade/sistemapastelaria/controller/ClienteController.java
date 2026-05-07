package br.edu.faculdade.sistemapastelaria.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.faculdade.sistemapastelaria.dto.ClienteDTO;
import br.edu.faculdade.sistemapastelaria.service.ClienteService;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public List<ClienteDTO> pesquisarTodos() {
        return clienteService.pesquisarClientes();
    }

    @GetMapping("/{id}")
    public ClienteDTO pesquisarPorId(@PathVariable Long id) {
        return clienteService.pesquisarPorId(id);
    }

    @PostMapping
    public ClienteDTO salvarCliente(@RequestBody ClienteDTO clienteDto) {
        return clienteService.salvarCliente(clienteDto);
    }

    @PutMapping
    public ClienteDTO atualizarCliente(@RequestBody ClienteDTO clienteDto) {
        return clienteService.atualizarCliente(clienteDto);
    }

    @DeleteMapping("/{id}")
    public ClienteDTO excluirCliente(@PathVariable Long id) {
        return clienteService.excluirCliente(id);
    }
}
