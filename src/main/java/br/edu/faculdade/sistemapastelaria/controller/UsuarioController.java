package br.edu.faculdade.sistemapastelaria.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;
import br.edu.faculdade.sistemapastelaria.service.UsuarioService;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public List<UsuarioDTO> pesquisarTodos() {
        return usuarioService.pesquisarUsuarios();
    }

    @GetMapping("/{id}")
    public UsuarioDTO pesquisarPorId(@PathVariable Long id) {
        return usuarioService.pesquisarPorId(id);
    }

    @PostMapping
    public UsuarioDTO salvarUsuario(@RequestBody UsuarioDTO usuarioDto) {
        return usuarioService.salvarUsuario(usuarioDto);
    }

    @PutMapping
    public UsuarioDTO atualizarUsuario(@RequestBody UsuarioDTO usuarioDto) {
        return usuarioService.atualizarUsuario(usuarioDto);
    }

    @DeleteMapping("/{id}")
    public UsuarioDTO excluirUsuario(@PathVariable Long id) {
        return usuarioService.excluirUsuario(id);
    }
}
