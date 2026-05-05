package br.edu.faculdade.sistemapastelaria.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.faculdade.sistemapastelaria.dto.ItemPedidoDTO;
import br.edu.faculdade.sistemapastelaria.dto.PedidoDTO;
import br.edu.faculdade.sistemapastelaria.service.PedidoService;

@RestController
@RequestMapping("/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;

    public PedidoController(PedidoService pedidoService) {
        this.pedidoService = pedidoService;
    }

    @GetMapping
    public List<PedidoDTO> pesquisarTodos() {
        return pedidoService.pesquisarPedidos();
    }

    @GetMapping("/{id}")
    public PedidoDTO pesquisarPorId(@PathVariable Long id) {
        return pedidoService.pesquisarPorId(id);
    }

    @PostMapping
    public PedidoDTO salvarPedido(@RequestBody PedidoDTO pedidoDto) {
        return pedidoService.salvarPedido(pedidoDto);
    }

    @PutMapping
    public PedidoDTO atualizarPedido(@RequestBody PedidoDTO pedidoDto) {
        return pedidoService.atualizarPedido(pedidoDto);
    }

    @DeleteMapping("/{id}")
    public PedidoDTO excluirPedido(@PathVariable Long id) {
        return pedidoService.excluirPedido(id);
    }

    @PatchMapping("/{id}/status")
    public PedidoDTO atualizarStatusPedido(@PathVariable Long id, @RequestBody PedidoDTO pedidoDto) {
        return pedidoService.atualizarStatusPedido(id, pedidoDto);
    }

    @PatchMapping("/{id}/cancelar")
    public PedidoDTO cancelarPedido(@PathVariable Long id) {
        return pedidoService.cancelarPedido(id);
    }

    @PostMapping("/{id}/itens")
    public PedidoDTO adicionarItemPedido(@PathVariable Long id, @RequestBody ItemPedidoDTO itemPedidoDto) {
        return pedidoService.adicionarItemPedido(id, itemPedidoDto);
    }

    @DeleteMapping("/{id}/itens/{itemId}")
    public PedidoDTO removerItemPedido(@PathVariable Long id, @PathVariable Long itemId) {
        return pedidoService.removerItemPedido(id, itemId);
    }
}
