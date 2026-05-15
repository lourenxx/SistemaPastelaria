package br.edu.faculdade.sistemapastelaria.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemPedidoDTO {
    private Long id;
    private int quantidade;
    private BigDecimal precoUnitario;
    private BigDecimal subTotal;
    private Long produtoId;
}
