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

import br.edu.faculdade.sistemapastelaria.dto.ProdutoDTO;
import br.edu.faculdade.sistemapastelaria.service.ProdutoService;

@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    public ProdutoController(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    @GetMapping
    public List<ProdutoDTO> pesquisarTodos() {
        return produtoService.pesquisarProdutos();
    }

    @GetMapping("/cardapio")
    public List<ProdutoDTO> pesquisarCardapio() {
        return produtoService.pesquisarCardapio();
    }

    @GetMapping("/{id}")
    public ProdutoDTO pesquisarPorId(@PathVariable Long id) {
        return produtoService.pesquisarPorId(id);
    }

    @PostMapping
    public ProdutoDTO salvarProduto(@RequestBody ProdutoDTO produtoDto) {
        return produtoService.salvarProduto(produtoDto);
    }

    @PutMapping
    public ProdutoDTO atualizarProduto(@RequestBody ProdutoDTO produtoDto) {
        return produtoService.atualizarProduto(produtoDto);
    }

    @DeleteMapping("/{id}")
    public ProdutoDTO excluirProduto(@PathVariable Long id) {
        return produtoService.excluirProduto(id);
    }
}
