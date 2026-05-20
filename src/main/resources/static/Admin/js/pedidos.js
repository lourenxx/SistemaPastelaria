var Pedido = (function () {
    var U = AdminUtils;
    var STATUS = ["ABERTO", "EM_PREPARO", "PRONTO", "ENTREGUE", "CANCELADO"];

    var pedidos = [];
    var clientes = [];
    var produtos = [];
    var itensEmEdicao = [];

    var obterElementos = function () {
        return {
            $estado: $("#pedidosEstado"),
            $tabela: $("#pedidosTabela"),
            $corpo: $("#pedidosCorpo"),
            $busca: $("#buscaPedido"),
            $form: $("#pedidoForm"),
            $formPanel: $("#pedidoFormPanel"),
            $formTitulo: $("#pedidoFormTitulo"),
            $id: $("#pedidoId"),
            $cliente: $("#pedidoCliente"),
            $pagamento: $("#pedidoPagamento"),
            $observacao: $("#pedidoObservacao"),
            $cancelar: $("#cancelarPedidoForm"),
            $adicionarItem: $("#adicionarItem"),
            $itemProduto: $("#itemProduto"),
            $itemQuantidade: $("#itemQuantidade")
        };
    };

    var obterElementosItens = function () {
        return {
            $estado: $("#itensEstado"),
            $tabela: $("#itensTabela"),
            $corpo: $("#itensCorpo")
        };
    };

    var bindEventos = function () {
        var elementos = obterElementos();

        $(document).on("click", "#novoPedido", abrirNovoPedido);
        elementos.$cancelar.on("click", fecharFormulario);
        elementos.$form.on("submit", salvarPedido);
        elementos.$adicionarItem.on("click", adicionarItem);
        elementos.$busca.on("input", renderPedidos);
    };

    var carregarDados = async function () {
        var elementos = obterElementos();

        U.setEstado(elementos.$estado, "", "Carregando pedidos...");
        U.mostrarTabela(elementos.$tabela, false);

        try {
            var dados = await Promise.all([
                AdminApi.pedidos.listar(),
                AdminApi.clientes.listar(),
                AdminApi.produtos.listar()
            ]);

            pedidos = dados[0];
            clientes = dados[1];
            produtos = dados[2];

            renderClientesOptions();
            renderProdutosOptions();
            renderPedidos();
        } catch (error) {
            U.setEstado(elementos.$estado, "error", U.mensagemErro(error));
        }
    };

    var renderClientesOptions = function () {
        var elementos = obterElementos();
        var $select = elementos.$cliente;
        U.limpar($select);
        $select.append(new Option("Selecione", ""));

        clientes.forEach(function (cliente) {
            $select.append(new Option(cliente.nome + " (#" + cliente.id + ")", cliente.id));
        });
    };

    var renderProdutosOptions = function () {
        var elementos = obterElementos();
        var $select = elementos.$itemProduto;
        U.limpar($select);
        $select.append(new Option("Selecione", ""));

        produtos.forEach(function (produto) {
            var option = new Option(produto.nome + " - " + U.formatarMoeda(produto.preco), produto.id);
            option.disabled = !produto.disponivel;
            $select.append(option);
        });
    };

    var renderPedidos = function () {
        var elementos = obterElementos();
        var termo = U.normalizar(elementos.$busca.val());
        var filtrados = pedidos.filter(function (pedido) {
            return filtrarPedido(pedido, termo);
        });

        U.limpar(elementos.$corpo);

        if (pedidos.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum pedido cadastrado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        if (filtrados.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum pedido encontrado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        filtrados
            .sort(function (a, b) {
                return Number(b.id || 0) - Number(a.id || 0);
            })
            .forEach(function (pedido) {
                elementos.$corpo.append(criarLinhaPedido(pedido));
            });

        U.setEstado(elementos.$estado, "", "");
        U.mostrarTabela(elementos.$tabela, true);
    };

    var criarLinhaPedido = function (pedido) {
        var $statusSelect = $("<select>", { class: "status-select" });
        STATUS.forEach(function (status) {
            $statusSelect.append(new Option(status, status));
        });
        $statusSelect
            .val(pedido.status || "ABERTO")
            .prop("disabled", pedido.status === "CANCELADO")
            .on("change", function () {
                atualizarStatus(pedido, $statusSelect);
            });

        var editar = U.botao("Editar", "btn-secondary", function () {
            abrirEdicao(pedido);
        });
        var cancelar = U.botao("Cancelar", "btn-danger", function () {
            cancelarPedido(pedido);
        });
        $(editar).prop("disabled", pedido.status === "CANCELADO");
        $(cancelar).prop("disabled", pedido.status === "CANCELADO");

        var $status = $("<td>").append($statusSelect);
        var $acoes = $("<td>").append(
            $("<div>", { class: "table-actions" }).append(editar, cancelar)
        );

        return $("<tr>").append(
            U.celula(pedido.id),
            U.celula(nomeCliente(pedido.clienteId)),
            $status,
            U.celula(U.formatarMoeda(pedido.valorTotal)),
            U.celula(U.formatarDataHora(pedido.dataHoraPedido)),
            $acoes
        );
    };

    var filtrarPedido = function (pedido, termo) {
        if (!termo || termo === "-") {
            return true;
        }

        return [
            pedido.id,
            nomeCliente(pedido.clienteId),
            pedido.status,
            pedido.valorTotal,
            pedido.formaPagamento,
            pedido.observacao,
            pedido.dataHoraPedido
        ].some(function (valor) {
            return U.normalizar(valor).includes(termo);
        });
    };

    var abrirNovoPedido = function () {
        var elementos = obterElementos();

        elementos.$formTitulo.text("Novo pedido");
        elementos.$form[0].reset();
        elementos.$id.val("");
        itensEmEdicao = [];
        renderItens();
        elementos.$formPanel.prop("hidden", false);
        elementos.$cliente.trigger("focus");
    };

    var abrirEdicao = async function (pedido) {
        try {
            var elementos = obterElementos();
            var pedidoAtual = await AdminApi.pedidos.buscar(pedido.id);
            elementos.$formTitulo.text("Editar pedido #" + pedidoAtual.id);
            elementos.$id.val(pedidoAtual.id);
            elementos.$cliente.val(pedidoAtual.clienteId || "");
            elementos.$pagamento.val(pedidoAtual.formaPagamento || "");
            elementos.$observacao.val(pedidoAtual.observacao || "");
            itensEmEdicao = (pedidoAtual.itens || []).slice();
            atualizarPedidoLocal(pedidoAtual);
            renderItens();
            renderPedidos();
            elementos.$formPanel.prop("hidden", false);
            elementos.$cliente.trigger("focus");
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var fecharFormulario = function () {
        var elementos = obterElementos();

        elementos.$formPanel.prop("hidden", true);
        elementos.$form[0].reset();
        itensEmEdicao = [];
        renderItens();
    };

    var salvarPedido = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();
        var id = elementos.$id.val();
        var pedido = montarPedidoPayload(id);

        try {
            var salvo = pedido.id
                ? await AdminApi.pedidos.atualizar(pedido)
                : await AdminApi.pedidos.salvar(pedido);

            atualizarPedidoLocal(salvo);
            U.toast(pedido.id ? "Pedido atualizado." : "Pedido cadastrado.");
            fecharFormulario();
            await carregarDados();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var montarPedidoPayload = function (id) {
        var elementos = obterElementos();
        var formaPagamento = elementos.$pagamento.val();
        var observacao = (elementos.$observacao.val() || "").trim();

        return {
            id: id ? Number(id) : null,
            dataHoraPedido: null,
            status: null,
            valorTotal: null,
            formaPagamento: formaPagamento || null,
            observacao: observacao || null,
            clienteId: Number(elementos.$cliente.val()),
            itens: itensEmEdicao.map(function (item) {
                return {
                    id: item.id || null,
                    quantidade: Number(item.quantidade),
                    precoUnitario: item.precoUnitario || null,
                    subTotal: item.subTotal || null,
                    produtoId: Number(item.produtoId)
                };
            })
        };
    };

    var adicionarItem = async function () {
        var elementos = obterElementos();
        var pedidoId = elementos.$id.val();
        var produtoId = Number(elementos.$itemProduto.val());
        var quantidade = Number(elementos.$itemQuantidade.val());
        var produto = produtos.find(function (item) {
            return item.id === produtoId;
        });

        if (!produtoId || !produto) {
            U.toast("Informe um produto.", "error");
            return;
        }

        if (!quantidade || quantidade <= 0) {
            U.toast("Quantidade deve ser maior que zero.", "error");
            return;
        }

        var item = {
            id: null,
            quantidade: quantidade,
            precoUnitario: null,
            subTotal: null,
            produtoId: produtoId
        };

        try {
            if (pedidoId) {
                var pedidoAtualizado = await AdminApi.pedidos.adicionarItem(Number(pedidoId), item);
                itensEmEdicao = (pedidoAtualizado.itens || []).slice();
                atualizarPedidoLocal(pedidoAtualizado);
                U.toast("Item adicionado.");
                renderPedidos();
            } else {
                var preco = Number(produto.preco || 0);
                itensEmEdicao.push({
                    id: item.id,
                    quantidade: item.quantidade,
                    precoUnitario: preco,
                    subTotal: preco * quantidade,
                    produtoId: item.produtoId
                });
                U.toast("Item adicionado.");
            }

            elementos.$itemProduto.val("");
            elementos.$itemQuantidade.val(1);
            renderItens();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var removerItem = async function (index) {
        var elementos = obterElementos();
        var pedidoId = elementos.$id.val();
        var item = itensEmEdicao[index];

        if (!item) {
            return;
        }

        try {
            if (pedidoId && item.id) {
                var pedidoAtualizado = await AdminApi.pedidos.removerItem(Number(pedidoId), item.id);
                itensEmEdicao = (pedidoAtualizado.itens || []).slice();
                atualizarPedidoLocal(pedidoAtualizado);
                U.toast("Item removido.");
                renderPedidos();
            } else {
                itensEmEdicao.splice(index, 1);
                U.toast("Item removido.");
            }

            renderItens();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var renderItens = function () {
        var elementos = obterElementosItens();

        U.limpar(elementos.$corpo);

        if (itensEmEdicao.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum item adicionado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        itensEmEdicao.forEach(function (item, index) {
            var produto = produtos.find(function (produtoItem) {
                return produtoItem.id === item.produtoId;
            });
            var $acoes = $("<td>").append(
                $("<div>", { class: "table-actions" }).append(
                    U.botao("Remover", "btn-danger", function () {
                        removerItem(index);
                    })
                )
            );

            $("<tr>")
                .append(
                    U.celula(produto ? produto.nome : "Produto " + item.produtoId),
                    U.celula(item.quantidade),
                    U.celula(U.formatarMoeda(item.precoUnitario)),
                    U.celula(U.formatarMoeda(item.subTotal)),
                    $acoes
                )
                .appendTo(elementos.$corpo);
        });

        U.setEstado(elementos.$estado, "", "");
        U.mostrarTabela(elementos.$tabela, true);
    };

    var atualizarStatus = async function (pedido, $select) {
        var statusAnterior = pedido.status;
        var statusNovo = $select.val();

        $select.prop("disabled", true);

        try {
            var atualizado = await AdminApi.pedidos.atualizarStatus(pedido.id, statusNovo);
            atualizarPedidoLocal(atualizado);
            U.toast("Status atualizado.");
            renderPedidos();
        } catch (error) {
            $select.val(statusAnterior);
            U.toast(U.mensagemErro(error), "error");
        } finally {
            $select.prop("disabled", $select.val() === "CANCELADO");
        }
    };

    var cancelarPedido = async function (pedido) {
        if (!window.confirm("Cancelar pedido #" + pedido.id + "?")) {
            return;
        }

        try {
            var atualizado = await AdminApi.pedidos.cancelar(pedido.id);
            atualizarPedidoLocal(atualizado);
            U.toast("Pedido cancelado.");
            renderPedidos();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var atualizarPedidoLocal = function (pedidoAtualizado) {
        var index = pedidos.findIndex(function (pedido) {
            return pedido.id === pedidoAtualizado.id;
        });

        if (index >= 0) {
            pedidos[index] = pedidoAtualizado;
        } else {
            pedidos.push(pedidoAtualizado);
        }
    };

    var nomeCliente = function (clienteId) {
        var cliente = clientes.find(function (item) {
            return item.id === clienteId;
        });
        return cliente ? cliente.nome : "Cliente " + clienteId;
    };

    var init = function () {
        bindEventos();
        carregarDados();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        obterElementosItens: obterElementosItens,
        bindEventos: bindEventos,
        carregarDados: carregarDados,
        renderClientesOptions: renderClientesOptions,
        renderProdutosOptions: renderProdutosOptions,
        renderPedidos: renderPedidos,
        criarLinhaPedido: criarLinhaPedido,
        filtrarPedido: filtrarPedido,
        abrirNovoPedido: abrirNovoPedido,
        abrirEdicao: abrirEdicao,
        fecharFormulario: fecharFormulario,
        salvarPedido: salvarPedido,
        montarPedidoPayload: montarPedidoPayload,
        adicionarItem: adicionarItem,
        removerItem: removerItem,
        renderItens: renderItens,
        atualizarStatus: atualizarStatus,
        cancelarPedido: cancelarPedido,
        atualizarPedidoLocal: atualizarPedidoLocal,
        nomeCliente: nomeCliente
    };
}());
