package br.edu.faculdade.sistemapastelaria.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.ItemPedidoDTO;
import br.edu.faculdade.sistemapastelaria.dto.PedidoDTO;
import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.model.ItemPedido;
import br.edu.faculdade.sistemapastelaria.model.Pedido;
import br.edu.faculdade.sistemapastelaria.model.Produto;
import br.edu.faculdade.sistemapastelaria.repository.ClienteRepository;
import br.edu.faculdade.sistemapastelaria.repository.ItemPedidoRepository;
import br.edu.faculdade.sistemapastelaria.repository.PedidoRepository;
import br.edu.faculdade.sistemapastelaria.repository.ProdutoRepository;

@Service
public class PedidoService {

    private static final String STATUS_ABERTO = "ABERTO";
    private static final String STATUS_CANCELADO = "CANCELADO";

    private final PedidoRepository pedidoRepository;
    private final ClienteRepository clienteRepository;
    private final ProdutoRepository produtoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ClienteRepository clienteRepository,
            ProdutoRepository produtoRepository, ItemPedidoRepository itemPedidoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.clienteRepository = clienteRepository;
        this.produtoRepository = produtoRepository;
        this.itemPedidoRepository = itemPedidoRepository;
    }

    @Transactional
    public PedidoDTO salvarPedido(PedidoDTO pedidoDto) {
        if (pedidoDto.getClienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente deve ser informado");
        }

        Cliente cliente = clienteRepository.findById(pedidoDto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        Pedido pedido = new Pedido();
        pedido.setDataHoraPedido(LocalDateTime.now());
        pedido.setStatus(STATUS_ABERTO);
        pedido.setFormaPagamento(pedidoDto.getFormaPagamento() == null ? null
                : pedidoDto.getFormaPagamento().toUpperCase());
        pedido.setObservacao(pedidoDto.getObservacao() == null ? null : pedidoDto.getObservacao().toUpperCase());
        pedido.setCliente(cliente);

        if (pedidoDto.getItens() != null) {
            for (ItemPedidoDTO itemPedidoDto : pedidoDto.getItens()) {
                if (itemPedidoDto.getQuantidade() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade deve ser maior que zero");
                }

                if (itemPedidoDto.getProdutoId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto deve ser informado");
                }

                Produto produto = produtoRepository.findById(itemPedidoDto.getProdutoId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

                if (!produto.isDisponivel()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponivel");
                }

                if (produto.getPreco() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem preco cadastrado");
                }

                BigDecimal precoUnitario = produto.getPreco();
                BigDecimal subTotal = precoUnitario.multiply(BigDecimal.valueOf(itemPedidoDto.getQuantidade()));

                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setQuantidade(itemPedidoDto.getQuantidade());
                itemPedido.setPrecoUnitario(precoUnitario);
                itemPedido.setSubTotal(subTotal);
                itemPedido.setPedido(pedido);
                itemPedido.setProduto(produto);
                pedido.getItens().add(itemPedido);
            }
        }

        BigDecimal valorTotal = pedido.getItens().stream()
                .map(ItemPedido::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(valorTotal);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(itemPedido -> new ItemPedidoDTO(
                                itemPedido.getId(),
                                itemPedido.getQuantidade(),
                                itemPedido.getPrecoUnitario(),
                                itemPedido.getSubTotal(),
                                itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                        .toList());
    }

    @Transactional
    public PedidoDTO salvarPedidoCliente(Long clienteId, PedidoDTO pedidoDto) {
        if (pedidoDto == null) {
            pedidoDto = new PedidoDTO();
        }

        pedidoDto.setId(null);
        pedidoDto.setClienteId(clienteId);
        return salvarPedido(pedidoDto);
    }

    @Transactional
    public PedidoDTO atualizarPedido(PedidoDTO pedidoDto) {
        Pedido pedido = pedidoRepository.findById(pedidoDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        if (STATUS_CANCELADO.equals(pedido.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido cancelado nao pode ser alterado");
        }

        if (pedidoDto.getClienteId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente deve ser informado");
        }

        Cliente cliente = clienteRepository.findById(pedidoDto.getClienteId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente nao encontrado"));

        pedido.setCliente(cliente);
        pedido.setFormaPagamento(pedidoDto.getFormaPagamento() == null ? null
                : pedidoDto.getFormaPagamento().toUpperCase());
        pedido.setObservacao(pedidoDto.getObservacao() == null ? null : pedidoDto.getObservacao().toUpperCase());

        if (pedidoDto.getItens() != null) {
            pedido.getItens().clear();

            for (ItemPedidoDTO itemPedidoDto : pedidoDto.getItens()) {
                if (itemPedidoDto.getQuantidade() <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade deve ser maior que zero");
                }

                if (itemPedidoDto.getProdutoId() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto deve ser informado");
                }

                Produto produto = produtoRepository.findById(itemPedidoDto.getProdutoId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

                if (!produto.isDisponivel()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponivel");
                }

                if (produto.getPreco() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem preco cadastrado");
                }

                BigDecimal precoUnitario = produto.getPreco();
                BigDecimal subTotal = precoUnitario.multiply(BigDecimal.valueOf(itemPedidoDto.getQuantidade()));

                ItemPedido itemPedido = new ItemPedido();
                itemPedido.setQuantidade(itemPedidoDto.getQuantidade());
                itemPedido.setPrecoUnitario(precoUnitario);
                itemPedido.setSubTotal(subTotal);
                itemPedido.setPedido(pedido);
                itemPedido.setProduto(produto);
                pedido.getItens().add(itemPedido);
            }
        }

        BigDecimal valorTotal = pedido.getItens().stream()
                .map(ItemPedido::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(valorTotal);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(itemPedido -> new ItemPedidoDTO(
                                itemPedido.getId(),
                                itemPedido.getQuantidade(),
                                itemPedido.getPrecoUnitario(),
                                itemPedido.getSubTotal(),
                                itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                        .toList());
    }

    @Transactional(readOnly = true)
    public List<PedidoDTO> pesquisarPedidos() {
        List<Pedido> pedidos = pedidoRepository.findAll();

        return pedidos.stream()
                .map(pedido -> new PedidoDTO(
                        pedido.getId(),
                        pedido.getDataHoraPedido(),
                        pedido.getStatus(),
                        pedido.getValorTotal(),
                        pedido.getFormaPagamento(),
                        pedido.getObservacao(),
                        pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                        pedido.getItens().stream()
                                .map(itemPedido -> new ItemPedidoDTO(
                                        itemPedido.getId(),
                                        itemPedido.getQuantidade(),
                                        itemPedido.getPrecoUnitario(),
                                        itemPedido.getSubTotal(),
                                        itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                                .toList()))
                .toList();
    }

    @Transactional(readOnly = true)
    public PedidoDTO pesquisarPorId(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(itemPedido -> new ItemPedidoDTO(
                                itemPedido.getId(),
                                itemPedido.getQuantidade(),
                                itemPedido.getPrecoUnitario(),
                                itemPedido.getSubTotal(),
                                itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                        .toList());
    }

    @Transactional
    public PedidoDTO excluirPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        pedido.setStatus(STATUS_CANCELADO);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(itemPedido -> new ItemPedidoDTO(
                                itemPedido.getId(),
                                itemPedido.getQuantidade(),
                                itemPedido.getPrecoUnitario(),
                                itemPedido.getSubTotal(),
                                itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                        .toList());
    }

    @Transactional
    public PedidoDTO atualizarStatusPedido(Long id, PedidoDTO pedidoDto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        if (pedidoDto.getStatus() == null || pedidoDto.getStatus().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status deve ser informado");
        }

        String status = pedidoDto.getStatus().toUpperCase();

        if (STATUS_CANCELADO.equals(pedido.getStatus()) && !STATUS_CANCELADO.equals(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido cancelado nao pode alterar status");
        }

        pedido.setStatus(status);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(itemPedido -> new ItemPedidoDTO(
                                itemPedido.getId(),
                                itemPedido.getQuantidade(),
                                itemPedido.getPrecoUnitario(),
                                itemPedido.getSubTotal(),
                                itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                        .toList());
    }

    @Transactional
    public PedidoDTO cancelarPedido(Long id) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        pedido.setStatus(STATUS_CANCELADO);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(itemPedido -> new ItemPedidoDTO(
                                itemPedido.getId(),
                                itemPedido.getQuantidade(),
                                itemPedido.getPrecoUnitario(),
                                itemPedido.getSubTotal(),
                                itemPedido.getProduto() != null ? itemPedido.getProduto().getId() : null))
                        .toList());
    }

    @Transactional
    public PedidoDTO adicionarItemPedido(Long id, ItemPedidoDTO itemPedidoDto) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        if (STATUS_CANCELADO.equals(pedido.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido cancelado nao pode ser alterado");
        }

        if (itemPedidoDto.getQuantidade() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantidade deve ser maior que zero");
        }

        if (itemPedidoDto.getProdutoId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto deve ser informado");
        }

        Produto produto = produtoRepository.findById(itemPedidoDto.getProdutoId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

        if (!produto.isDisponivel()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto indisponivel");
        }

        if (produto.getPreco() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Produto sem preco cadastrado");
        }

        BigDecimal precoUnitario = produto.getPreco();
        BigDecimal subTotal = precoUnitario.multiply(BigDecimal.valueOf(itemPedidoDto.getQuantidade()));

        ItemPedido itemPedido = new ItemPedido();
        itemPedido.setQuantidade(itemPedidoDto.getQuantidade());
        itemPedido.setPrecoUnitario(precoUnitario);
        itemPedido.setSubTotal(subTotal);
        itemPedido.setPedido(pedido);
        itemPedido.setProduto(produto);
        pedido.getItens().add(itemPedido);

        BigDecimal valorTotal = pedido.getItens().stream()
                .map(ItemPedido::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(valorTotal);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(item -> new ItemPedidoDTO(
                                item.getId(),
                                item.getQuantidade(),
                                item.getPrecoUnitario(),
                                item.getSubTotal(),
                                item.getProduto() != null ? item.getProduto().getId() : null))
                        .toList());
    }

    @Transactional
    public PedidoDTO removerItemPedido(Long id, Long itemId) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido nao encontrado"));

        if (STATUS_CANCELADO.equals(pedido.getStatus())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pedido cancelado nao pode ser alterado");
        }

        ItemPedido itemPedido = itemPedidoRepository.findByIdAndPedido_Id(itemId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item do pedido nao encontrado"));

        pedido.getItens().removeIf(item -> item.getId().equals(itemPedido.getId()));

        BigDecimal valorTotal = pedido.getItens().stream()
                .map(ItemPedido::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        pedido.setValorTotal(valorTotal);
        pedidoRepository.save(pedido);

        return new PedidoDTO(
                pedido.getId(),
                pedido.getDataHoraPedido(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                pedido.getFormaPagamento(),
                pedido.getObservacao(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getItens().stream()
                        .map(item -> new ItemPedidoDTO(
                                item.getId(),
                                item.getQuantidade(),
                                item.getPrecoUnitario(),
                                item.getSubTotal(),
                                item.getProduto() != null ? item.getProduto().getId() : null))
                        .toList());
    }
}
