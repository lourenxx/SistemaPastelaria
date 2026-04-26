package br.edu.faculdade.sistemapastelaria.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;
import br.edu.faculdade.sistemapastelaria.model.Usuario;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioDTO salvarUsuario(UsuarioDTO usuarioDto) {
        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome().toUpperCase());
        usuario.setLogin(usuarioDto.getLogin().toLowerCase());
        usuario.setSenha(usuarioDto.getSenha().toLowerCase());
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getSenha());
    }

    public UsuarioDTO atualizarUsuario(UsuarioDTO usuarioDto) {
        Usuario usuario = usuarioRepository.findById(usuarioDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        usuario.setNome(usuarioDto.getNome().toUpperCase());
        usuario.setLogin(usuarioDto.getLogin().toLowerCase());
        usuario.setSenha(usuarioDto.getSenha().toLowerCase());
        usuarioRepository.save(usuario);

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getSenha());
    }

    public List<UsuarioDTO> pesquisarUsuarios() {
        List<Usuario> usuarios = usuarioRepository.findAll();

        return usuarios.stream()
                .map(usuario -> new UsuarioDTO(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getLogin(),
                        usuario.getSenha()))
                .toList();
    }

    public UsuarioDTO pesquisarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getSenha());
    }

    public UsuarioDTO excluirUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario nao encontrado"));

        usuario.setAtivo(false);
        usuarioRepository.delete(usuario);

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getSenha());
    }
}
