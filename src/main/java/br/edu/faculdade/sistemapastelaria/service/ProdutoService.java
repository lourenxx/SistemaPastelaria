package br.edu.faculdade.sistemapastelaria.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import br.edu.faculdade.sistemapastelaria.dto.ProdutoDTO;
import br.edu.faculdade.sistemapastelaria.model.Produto;
import br.edu.faculdade.sistemapastelaria.repository.ProdutoRepository;

@Service
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    public ProdutoService(ProdutoRepository produtoRepository) {
        this.produtoRepository = produtoRepository;
    }

    public ProdutoDTO salvarProduto(ProdutoDTO produtoDto) {
        Produto produto = new Produto();
        produto.setNome(produtoDto.getNome().toUpperCase());
        produto.setDescricao(produtoDto.getDescricao().toUpperCase());
        produto.setCategoria(produtoDto.getCategoria().toUpperCase());
        produto.setPreco(produtoDto.getPreco());
        produto.setDisponivel(true);
        produtoRepository.save(produto);

        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.isDisponivel());
    }

    public ProdutoDTO atualizarProduto(ProdutoDTO produtoDto) {
        Produto produto = produtoRepository.findById(produtoDto.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

        produto.setNome(produtoDto.getNome().toUpperCase());
        produto.setDescricao(produtoDto.getDescricao().toUpperCase());
        produto.setCategoria(produtoDto.getCategoria().toUpperCase());
        produto.setPreco(produtoDto.getPreco());
        produto.setDisponivel(produtoDto.isDisponivel());
        produtoRepository.save(produto);

        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.isDisponivel());
    }

    public List<ProdutoDTO> pesquisarProdutos() {
        List<Produto> produtos = produtoRepository.findAll();

        return produtos.stream()
                .map(produto -> new ProdutoDTO(
                        produto.getId(),
                        produto.getNome(),
                        produto.getDescricao(),
                        produto.getCategoria(),
                        produto.getPreco(),
                        produto.isDisponivel()))
                .toList();
    }

    public ProdutoDTO pesquisarPorId(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.isDisponivel());
    }

    public ProdutoDTO excluirProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Produto nao encontrado"));

        produto.setDisponivel(false);
        produtoRepository.save(produto);

        return new ProdutoDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getCategoria(),
                produto.getPreco(),
                produto.isDisponivel());
    }
}
