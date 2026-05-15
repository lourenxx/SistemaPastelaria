package br.edu.faculdade.sistemapastelaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.faculdade.sistemapastelaria.model.ItemPedido;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemPedidoRepository extends JpaRepository<ItemPedido, Long> {

    Optional<ItemPedido> findByIdAndPedido_Id(Long id, Long pedidoId);

}
