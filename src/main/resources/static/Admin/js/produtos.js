(function () {
    const U = window.AdminUtils;
    let produtos = [];

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("novoProduto").addEventListener("click", abrirNovoProduto);
        document.getElementById("cancelarProduto").addEventListener("click", fecharFormulario);
        document.getElementById("produtoForm").addEventListener("submit", salvarProduto);
        document.getElementById("buscaProduto").addEventListener("input", renderProdutos);
        carregarProdutos();
    });

    async function carregarProdutos() {
        const estado = document.getElementById("produtosEstado");
        const tabela = document.getElementById("produtosTabela");

        U.setEstado(estado, "", "Carregando produtos...");
        U.mostrarTabela(tabela, false);

        try {
            produtos = await AdminApi.produtos.listar();
            renderProdutos();
        } catch (error) {
            U.setEstado(estado, "error", U.mensagemErro(error));
        }
    }

    function renderProdutos() {
        const estado = document.getElementById("produtosEstado");
        const tabela = document.getElementById("produtosTabela");
        const corpo = document.getElementById("produtosCorpo");
        const termo = U.normalizar(document.getElementById("buscaProduto").value);
        const filtrados = produtos.filter((produto) => filtrarProduto(produto, termo));

        U.limpar(corpo);

        if (produtos.length === 0) {
            U.setEstado(estado, "", "Nenhum produto cadastrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        if (filtrados.length === 0) {
            U.setEstado(estado, "", "Nenhum produto encontrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        filtrados.forEach((produto) => {
            const tr = document.createElement("tr");
            const status = document.createElement("td");
            const acoes = document.createElement("td");
            const actions = document.createElement("div");

            status.appendChild(U.badge(
                produto.disponivel ? "Disponivel" : "Indisponivel",
                produto.disponivel ? "success" : "danger"
            ));

            actions.className = "table-actions";
            actions.append(
                U.botao("Editar", "btn-secondary", () => abrirEdicao(produto)),
                U.botao("Excluir", "btn-danger", () => excluirProduto(produto))
            );
            acoes.appendChild(actions);

            tr.append(
                U.celula(produto.id),
                U.celula(produto.nome),
                U.celula(produto.categoria),
                U.celula(U.formatarMoeda(produto.preco)),
                status,
                acoes
            );
            corpo.appendChild(tr);
        });

        U.setEstado(estado, "", "");
        U.mostrarTabela(tabela, true);
    }

    function filtrarProduto(produto, termo) {
        if (!termo || termo === "-") {
            return true;
        }

        return [
            produto.id,
            produto.nome,
            produto.descricao,
            produto.categoria,
            produto.preco,
            produto.disponivel ? "disponivel" : "indisponivel"
        ].some((valor) => U.normalizar(valor).includes(termo));
    }

    function abrirNovoProduto() {
        document.getElementById("produtoFormTitulo").textContent = "Novo produto";
        document.getElementById("produtoForm").reset();
        document.getElementById("produtoId").value = "";
        document.getElementById("produtoDisponivel").checked = true;
        document.getElementById("produtoFormPanel").hidden = false;
        document.getElementById("produtoNome").focus();
    }

    function abrirEdicao(produto) {
        document.getElementById("produtoFormTitulo").textContent = `Editar produto #${produto.id}`;
        document.getElementById("produtoId").value = produto.id;
        document.getElementById("produtoNome").value = U.texto(produto.nome) === "-" ? "" : produto.nome;
        document.getElementById("produtoCategoria").value = U.texto(produto.categoria) === "-" ? "" : produto.categoria;
        document.getElementById("produtoPreco").value = produto.preco || 0;
        document.getElementById("produtoDisponivel").checked = Boolean(produto.disponivel);
        document.getElementById("produtoDescricao").value = U.texto(produto.descricao) === "-" ? "" : produto.descricao;
        document.getElementById("produtoFormPanel").hidden = false;
        document.getElementById("produtoNome").focus();
    }

    function fecharFormulario() {
        document.getElementById("produtoFormPanel").hidden = true;
        document.getElementById("produtoForm").reset();
    }

    async function salvarProduto(event) {
        event.preventDefault();

        const id = document.getElementById("produtoId").value;
        const produto = {
            id: id ? Number(id) : null,
            nome: document.getElementById("produtoNome").value.trim(),
            descricao: document.getElementById("produtoDescricao").value.trim(),
            categoria: document.getElementById("produtoCategoria").value.trim(),
            preco: Number(document.getElementById("produtoPreco").value),
            disponivel: document.getElementById("produtoDisponivel").checked
        };

        try {
            if (produto.id) {
                await AdminApi.produtos.atualizar(produto);
                U.toast("Produto atualizado.");
            } else {
                await AdminApi.produtos.salvar(produto);
                U.toast("Produto cadastrado.");
            }

            fecharFormulario();
            await carregarProdutos();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }

    async function excluirProduto(produto) {
        if (!window.confirm(`Excluir produto ${produto.nome}?`)) {
            return;
        }

        try {
            await AdminApi.produtos.excluir(produto.id);
            U.toast("Produto excluido.");
            await carregarProdutos();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }
})();
