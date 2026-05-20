var Produto = (function () {
    var U = AdminUtils;
    var produtos = [];

    var obterElementos = function () {
        return {
            $estado: $("#produtosEstado"),
            $tabela: $("#produtosTabela"),
            $corpo: $("#produtosCorpo"),
            $form: $("#produtoForm"),
            $formPanel: $("#produtoFormPanel"),
            $formTitulo: $("#produtoFormTitulo"),
            $id: $("#produtoId"),
            $nome: $("#produtoNome"),
            $categoria: $("#produtoCategoria"),
            $preco: $("#produtoPreco"),
            $disponivel: $("#produtoDisponivel"),
            $descricao: $("#produtoDescricao"),
            $busca: $("#buscaProduto"),
            $cancelar: $("#cancelarProduto")
        };
    };

    var bindEventos = function () {
        var elementos = obterElementos();

        $(document).on("click", "#novoProduto", abrirNovoProduto);
        elementos.$cancelar.on("click", fecharFormulario);
        elementos.$form.on("submit", salvarProduto);
        elementos.$busca.on("input", renderProdutos);
    };

    var carregarProdutos = async function () {
        var elementos = obterElementos();

        U.setEstado(elementos.$estado, "", "Carregando produtos...");
        U.mostrarTabela(elementos.$tabela, false);

        try {
            produtos = await AdminApi.produtos.listar();
            renderProdutos();
        } catch (error) {
            U.setEstado(elementos.$estado, "error", U.mensagemErro(error));
        }
    };

    var renderProdutos = function () {
        var elementos = obterElementos();
        var termo = U.normalizar(elementos.$busca.val());
        var filtrados = produtos.filter(function (produto) {
            return filtrarProduto(produto, termo);
        });

        U.limpar(elementos.$corpo);

        if (produtos.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum produto cadastrado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        if (filtrados.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum produto encontrado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        filtrados.forEach(function (produto) {
            var $status = $("<td>").append(U.badge(
                produto.disponivel ? "Disponivel" : "Indisponivel",
                produto.disponivel ? "success" : "danger"
            ));
            var $acoes = $("<td>").append(
                $("<div>", { class: "table-actions" }).append(
                    U.botao("Editar", "btn-secondary", function () {
                        abrirEdicao(produto);
                    }),
                    U.botao("Excluir", "btn-danger", function () {
                        excluirProduto(produto);
                    })
                )
            );

            $("<tr>")
                .append(
                    U.celula(produto.id),
                    U.celula(produto.nome),
                    U.celula(produto.categoria),
                    U.celula(U.formatarMoeda(produto.preco)),
                    $status,
                    $acoes
                )
                .appendTo(elementos.$corpo);
        });

        U.setEstado(elementos.$estado, "", "");
        U.mostrarTabela(elementos.$tabela, true);
    };

    var filtrarProduto = function (produto, termo) {
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
        ].some(function (valor) {
            return U.normalizar(valor).includes(termo);
        });
    };

    var abrirNovoProduto = function () {
        var elementos = obterElementos();

        elementos.$formTitulo.text("Novo produto");
        elementos.$form[0].reset();
        elementos.$id.val("");
        elementos.$disponivel.prop("checked", true);
        elementos.$formPanel.prop("hidden", false);
        elementos.$nome.trigger("focus");
    };

    var abrirEdicao = function (produto) {
        var elementos = obterElementos();

        elementos.$formTitulo.text("Editar produto #" + produto.id);
        elementos.$id.val(produto.id);
        elementos.$nome.val(U.texto(produto.nome) === "-" ? "" : produto.nome);
        elementos.$categoria.val(U.texto(produto.categoria) === "-" ? "" : produto.categoria);
        elementos.$preco.val(produto.preco || 0);
        elementos.$disponivel.prop("checked", Boolean(produto.disponivel));
        elementos.$descricao.val(U.texto(produto.descricao) === "-" ? "" : produto.descricao);
        elementos.$formPanel.prop("hidden", false);
        elementos.$nome.trigger("focus");
    };

    var fecharFormulario = function () {
        var elementos = obterElementos();

        elementos.$formPanel.prop("hidden", true);
        elementos.$form[0].reset();
    };

    var salvarProduto = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();
        var id = elementos.$id.val();
        var produto = {
            id: id ? Number(id) : null,
            nome: (elementos.$nome.val() || "").trim(),
            descricao: (elementos.$descricao.val() || "").trim(),
            categoria: (elementos.$categoria.val() || "").trim(),
            preco: Number(elementos.$preco.val()),
            disponivel: elementos.$disponivel.prop("checked")
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
    };

    var excluirProduto = async function (produto) {
        if (!window.confirm("Excluir produto " + produto.nome + "?")) {
            return;
        }

        try {
            await AdminApi.produtos.excluir(produto.id);
            U.toast("Produto excluido.");
            await carregarProdutos();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var init = function () {
        bindEventos();
        carregarProdutos();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        carregarProdutos: carregarProdutos,
        renderProdutos: renderProdutos,
        filtrarProduto: filtrarProduto,
        abrirNovoProduto: abrirNovoProduto,
        abrirEdicao: abrirEdicao,
        fecharFormulario: fecharFormulario,
        salvarProduto: salvarProduto,
        excluirProduto: excluirProduto
    };
}());
