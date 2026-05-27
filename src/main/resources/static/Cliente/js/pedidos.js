var PedidoCliente = (function () {
    var moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
    var produtos = [];
    var carrinho = [];

    var obterElementos = function () {
        return {
            $form: $("#pedidoForm"),
            $sairButton: $("#sairButton"),
            $clienteNome: $("#clienteNome"),
            $produtosEstado: $("#produtosEstado"),
            $produtosLista: $("#produtosLista"),
            $pedidoMensagem: $("#pedidoMensagem"),
            $carrinhoLista: $("#carrinhoLista"),
            $carrinhoEstado: $("#carrinhoEstado"),
            $pedidoTotal: $("#pedidoTotal"),
            $formaPagamento: $("#formaPagamento"),
            $observacao: $("#observacao")
        };
    };

    var bindEventos = function () {
        var elementos = obterElementos();

        elementos.$form.on("submit", enviarPedido);
        elementos.$sairButton.on("click", sair);
    };

    var iniciar = async function () {
        var elementos = obterElementos();

        try {
            var sessao = await ClienteApi.sessao();

            if (sessao.tipo !== "CLIENTE") {
                window.location.href = "/index.html";
                return;
            }

            elementos.$clienteNome.text(sessao.nome || sessao.email || "");
            await carregarCardapio();
        } catch (error) {
            elementos.$produtosEstado
                .text(error.message)
                .attr("class", "state error");
        }
    };

    var carregarCardapio = async function () {
        var elementos = obterElementos();

        elementos.$produtosEstado
            .text("Carregando...")
            .attr("class", "state");

        produtos = await ClienteApi.cardapio();
        renderProdutos();
    };

    var renderProdutos = function () {
        var elementos = obterElementos();

        elementos.$produtosLista.empty();

        if (!produtos.length) {
            elementos.$produtosEstado.text("Nenhum produto disponivel.");
            return;
        }

        elementos.$produtosEstado.text("");

        produtos.forEach(function (produto) {
            var $quantidade = $("<input>", {
                type: "number",
                min: "1",
                step: "1",
                value: "1"
            });

            $("<article>", { class: "product-card" })
                .append(
                    $("<h3>").text(produto.nome),
                    $("<p>").text(produto.descricao || "Produto da casa"),
                    $("<div>", { class: "product-meta" }).append(
                        $("<span>").text(produto.categoria || "Cardapio"),
                        $("<strong>").text(moeda.format(Number(produto.preco || 0)))
                    ),
                    $("<div>", { class: "product-actions" }).append(
                        $quantidade,
                        $("<button>", {
                            type: "button",
                            class: "primary-action"
                        })
                            .text("Adicionar")
                            .on("click", function () {
                                adicionarAoCarrinho(produto, Number($quantidade.val()));
                            })
                    )
                )
                .appendTo(elementos.$produtosLista);
        });
    };

    var adicionarAoCarrinho = function (produto, quantidade) {
        var elementos = obterElementos();

        elementos.$pedidoMensagem
            .text("")
            .attr("class", "message");

        if (!quantidade || quantidade <= 0) {
            elementos.$pedidoMensagem
                .attr("class", "message error")
                .text("Quantidade deve ser maior que zero.");
            return;
        }

        var item = carrinho.find(function (atual) {
            return atual.produto.id === produto.id;
        });

        if (item) {
            item.quantidade += quantidade;
        } else {
            carrinho.push({ produto: produto, quantidade: quantidade });
        }

        renderCarrinho();
    };

    var renderCarrinho = function () {
        var elementos = obterElementos();
        var total = carrinho.reduce(function (soma, item) {
            return soma + Number(item.produto.preco || 0) * item.quantidade;
        }, 0);

        elementos.$carrinhoLista.empty();
        elementos.$pedidoTotal.text(moeda.format(total));

        if (!carrinho.length) {
            elementos.$carrinhoEstado
                .prop("hidden", false)
                .text("Nenhum item adicionado.");
            return;
        }

        elementos.$carrinhoEstado.prop("hidden", true);

        carrinho.forEach(function (item, index) {
            var subtotal = Number(item.produto.preco || 0) * item.quantidade;

            $("<div>", { class: "cart-item" })
                .append(
                    $("<div>").append(
                        $("<strong>").text(item.produto.nome),
                        $("<span>").text(item.quantidade + " x " + moeda.format(Number(item.produto.preco || 0)) + " = " + moeda.format(subtotal))
                    ),
                    $("<button>", {
                        type: "button",
                        class: "cart-remove"
                    })
                        .text("Remover")
                        .on("click", function () {
                            carrinho.splice(index, 1);
                            renderCarrinho();
                        })
                )
                .appendTo(elementos.$carrinhoLista);
        });
    };

    var enviarPedido = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();
        var $button = $(event.currentTarget).find("button[type='submit']");

        elementos.$pedidoMensagem
            .text("")
            .attr("class", "message");

        if (!carrinho.length) {
            elementos.$pedidoMensagem
                .attr("class", "message error")
                .text("Adicione ao menos um item.");
            return;
        }

        $button
            .prop("disabled", true)
            .text("Enviando...");

        try {
            var pedido = await ClienteApi.fazerPedido({
                formaPagamento: elementos.$formaPagamento.val() || null,
                observacao: (elementos.$observacao.val() || "").trim() || null,
                itens: carrinho.map(function (item) {
                    return {
                        quantidade: item.quantidade,
                        produtoId: item.produto.id
                    };
                })
            });

            carrinho = [];
            renderCarrinho();
            elementos.$form[0].reset();
            elementos.$pedidoMensagem
                .attr("class", "message success")
                .text("Pedido #" + pedido.id + " enviado com sucesso.");
        } catch (error) {
            elementos.$pedidoMensagem
                .attr("class", "message error")
                .text(error.message);
        } finally {
            $button
                .prop("disabled", false)
                .text("Fazer pedido");
        }
    };

    var sair = async function () {
        try {
            await ClienteApi.logout();
        } finally {
            window.location.href = "/index.html";
        }
    };

    var init = function () {
        bindEventos();
        iniciar();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        iniciar: iniciar,
        carregarCardapio: carregarCardapio,
        renderProdutos: renderProdutos,
        adicionarAoCarrinho: adicionarAoCarrinho,
        renderCarrinho: renderCarrinho,
        enviarPedido: enviarPedido,
        sair: sair
    };
}());
