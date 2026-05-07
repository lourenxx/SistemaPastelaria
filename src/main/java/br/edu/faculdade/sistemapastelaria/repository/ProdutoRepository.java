package br.edu.faculdade.sistemapastelaria.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.faculdade.sistemapastelaria.model.Produto;
import org.springframework.stereotype.Repository;


@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long> {

}
