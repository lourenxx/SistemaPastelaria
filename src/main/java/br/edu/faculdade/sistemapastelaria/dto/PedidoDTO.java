package br.edu.faculdade.sistemapastelaria.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    private Long id;
    private LocalDateTime dataHoraPedido;
    private String status;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacao;
    private Long clienteId;
    private List<ItemPedidoDTO> itens;
}
