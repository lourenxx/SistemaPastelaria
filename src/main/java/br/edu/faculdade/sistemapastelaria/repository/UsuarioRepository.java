package br.edu.faculdade.sistemapastelaria.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import br.edu.faculdade.sistemapastelaria.model.Usuario;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByLogin(String login);

}
