package br.edu.faculdade.sistemapastelaria.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;
import br.edu.faculdade.sistemapastelaria.model.Usuario;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;

@Service
public class UsuarioService {


    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public UsuarioDTO salvar(UsuarioDTO usuarioDto){
        usuarioDto.getNome().toUpperCase();
        usuarioDto.getLogin().toLowerCase();
        usuarioDto.getSenha().toLowerCase();

        Usuario usuario = new Usuario();
        usuario.setNome(usuarioDto.getNome());
        usuario.setLogin(usuarioDto.getLogin());
        usuario.setSenha(usuarioDto.getSenha());
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);

        return new UsuarioDTO(
                usuario.getId(),
                usuario.getNome(),
                usuario.getLogin(),
                usuario.getSenha()
        );
    }

    public List<UsuarioDTO> listarUsuarios(){
        List<Usuario> usuarios = usuarioRepository.findAll();

        List<UsuarioDTO> listaUsuarioDto = usuarios.stream()
                .map(usuario -> new UsuarioDTO(
                        usuario.getId(),
                        usuario.getNome(),
                        usuario.getLogin(),
                        usuario.getSenha()
                ))
                .toList();

        return listaUsuarioDto;
    }


}
