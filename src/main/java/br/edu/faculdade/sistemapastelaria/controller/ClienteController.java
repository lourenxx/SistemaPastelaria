package br.edu.faculdade.sistemapastelaria.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.faculdade.sistemapastelaria.dto.ClienteCadastroDTO;
import br.edu.faculdade.sistemapastelaria.dto.ClienteDTO;
import br.edu.faculdade.sistemapastelaria.dto.ClienteLoginDTO;
import br.edu.faculdade.sistemapastelaria.dto.PedidoDTO;
import br.edu.faculdade.sistemapastelaria.dto.SessaoDTO;
import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.service.ClienteService;
import br.edu.faculdade.sistemapastelaria.service.PedidoService;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final PedidoService pedidoService;

    public ClienteController(ClienteService clienteService, PedidoService pedidoService) {
        this.clienteService = clienteService;
        this.pedidoService = pedidoService;
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

    @PostMapping("/cadastro")
    public ClienteDTO cadastrarCliente(@RequestBody ClienteCadastroDTO cadastroDto) {
        return clienteService.cadastrarClienteExterno(cadastroDto);
    }

    @PostMapping("/login")
    public SessaoDTO loginCliente(@RequestBody ClienteLoginDTO loginDto, HttpServletRequest request) {
        return clienteService.loginCliente(loginDto, request);
    }

    @PostMapping("/me/pedidos")
    public PedidoDTO salvarPedidoCliente(@RequestBody PedidoDTO pedidoDto, Authentication authentication) {
        Cliente cliente = clienteService.obterClienteAutenticado(authentication);
        return pedidoService.salvarPedidoCliente(cliente.getId(), pedidoDto);
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
