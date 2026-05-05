package br.edu.faculdade.sistemapastelaria.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

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
