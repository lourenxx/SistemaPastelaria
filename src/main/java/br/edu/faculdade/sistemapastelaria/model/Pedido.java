package br.edu.faculdade.sistemapastelaria.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedido")
@Getter
@Setter
@NoArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dataHoraPedido;
    private String status;
    private BigDecimal valorTotal;
    private String formaPagamento;
    private String observacao;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    public Pedido(Long id, LocalDateTime dataHoraPedido, String status, BigDecimal valorTotal,
                  String formaPagamento, String observacao, Cliente cliente) {
        this.dataHoraPedido = dataHoraPedido;
        this.status = status;
        this.valorTotal = valorTotal;
        this.formaPagamento = formaPagamento;
        this.observacao = observacao;
        this.cliente = cliente;
    }
}
