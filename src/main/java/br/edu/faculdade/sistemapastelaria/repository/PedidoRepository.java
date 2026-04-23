package br.edu.faculdade.sistemapastelaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.faculdade.sistemapastelaria.model.Pedido;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

}
