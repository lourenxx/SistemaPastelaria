(function () {
    const U = window.AdminUtils;
    const STATUS = ["ABERTO", "EM_PREPARO", "PRONTO", "ENTREGUE", "CANCELADO"];

    let pedidos = [];
    let clientes = [];
    let produtos = [];
    let itensEmEdicao = [];

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("novoPedido").addEventListener("click", abrirNovoPedido);
        document.getElementById("cancelarPedidoForm").addEventListener("click", fecharFormulario);
        document.getElementById("pedidoForm").addEventListener("submit", salvarPedido);
        document.getElementById("adicionarItem").addEventListener("click", adicionarItem);
        document.getElementById("buscaPedido").addEventListener("input", renderPedidos);
        carregarDados();
    });

    async function carregarDados() {
        const estado = document.getElementById("pedidosEstado");
        const tabela = document.getElementById("pedidosTabela");

        U.setEstado(estado, "", "Carregando pedidos...");
        U.mostrarTabela(tabela, false);

        try {
            [pedidos, clientes, produtos] = await Promise.all([
                AdminApi.pedidos.listar(),
                AdminApi.clientes.listar(),
                AdminApi.produtos.listar()
            ]);

            renderClientesOptions();
            renderProdutosOptions();
            renderPedidos();
        } catch (error) {
            U.setEstado(estado, "error", U.mensagemErro(error));
        }
    }

    function renderClientesOptions() {
        const select = document.getElementById("pedidoCliente");
        U.limpar(select);
        select.appendChild(new Option("Selecione", ""));

        clientes.forEach((cliente) => {
            select.appendChild(new Option(`${cliente.nome} (#${cliente.id})`, cliente.id));
        });
    }

    function renderProdutosOptions() {
        const select = document.getElementById("itemProduto");
        U.limpar(select);
        select.appendChild(new Option("Selecione", ""));

        produtos.forEach((produto) => {
            const option = new Option(`${produto.nome} - ${U.formatarMoeda(produto.preco)}`, produto.id);
            option.disabled = !produto.disponivel;
            select.appendChild(option);
        });
    }

    function renderPedidos() {
        const estado = document.getElementById("pedidosEstado");
        const tabela = document.getElementById("pedidosTabela");
        const corpo = document.getElementById("pedidosCorpo");
        const termo = U.normalizar(document.getElementById("buscaPedido").value);
        const filtrados = pedidos.filter((pedido) => filtrarPedido(pedido, termo));

        U.limpar(corpo);

        if (pedidos.length === 0) {
            U.setEstado(estado, "", "Nenhum pedido cadastrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        if (filtrados.length === 0) {
            U.setEstado(estado, "", "Nenhum pedido encontrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        filtrados
            .sort((a, b) => Number(b.id || 0) - Number(a.id || 0))
            .forEach((pedido) => corpo.appendChild(criarLinhaPedido(pedido)));

        U.setEstado(estado, "", "");
        U.mostrarTabela(tabela, true);
    }

    function criarLinhaPedido(pedido) {
        const tr = document.createElement("tr");
        const status = document.createElement("td");
        const acoes = document.createElement("td");
        const statusSelect = document.createElement("select");
        const actions = document.createElement("div");
        const editar = U.botao("Editar", "btn-secondary", () => abrirEdicao(pedido));
        const cancelar = U.botao("Cancelar", "btn-danger", () => cancelarPedido(pedido));

        statusSelect.className = "status-select";
        STATUS.forEach((item) => statusSelect.appendChild(new Option(item, item)));
        statusSelect.value = pedido.status || "ABERTO";
        statusSelect.disabled = pedido.status === "CANCELADO";
        statusSelect.addEventListener("change", () => atualizarStatus(pedido, statusSelect));
        status.appendChild(statusSelect);

        editar.disabled = pedido.status === "CANCELADO";
        cancelar.disabled = pedido.status === "CANCELADO";
        actions.className = "table-actions";
        actions.append(editar, cancelar);
        acoes.appendChild(actions);

        tr.append(
            U.celula(pedido.id),
            U.celula(nomeCliente(pedido.clienteId)),
            status,
            U.celula(U.formatarMoeda(pedido.valorTotal)),
            U.celula(U.formatarDataHora(pedido.dataHoraPedido)),
            acoes
        );

        return tr;
    }

    function filtrarPedido(pedido, termo) {
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
        ].some((valor) => U.normalizar(valor).includes(termo));
    }

    function abrirNovoPedido() {
        document.getElementById("pedidoFormTitulo").textContent = "Novo pedido";
        document.getElementById("pedidoForm").reset();
        document.getElementById("pedidoId").value = "";
        itensEmEdicao = [];
        renderItens();
        document.getElementById("pedidoFormPanel").hidden = false;
        document.getElementById("pedidoCliente").focus();
    }

    async function abrirEdicao(pedido) {
        try {
            const pedidoAtual = await AdminApi.pedidos.buscar(pedido.id);
            document.getElementById("pedidoFormTitulo").textContent = `Editar pedido #${pedidoAtual.id}`;
            document.getElementById("pedidoId").value = pedidoAtual.id;
            document.getElementById("pedidoCliente").value = pedidoAtual.clienteId || "";
            document.getElementById("pedidoPagamento").value = pedidoAtual.formaPagamento || "";
            document.getElementById("pedidoObservacao").value = pedidoAtual.observacao || "";
            itensEmEdicao = [...(pedidoAtual.itens || [])];
            atualizarPedidoLocal(pedidoAtual);
            renderItens();
            renderPedidos();
            document.getElementById("pedidoFormPanel").hidden = false;
            document.getElementById("pedidoCliente").focus();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }

    function fecharFormulario() {
        document.getElementById("pedidoFormPanel").hidden = true;
        document.getElementById("pedidoForm").reset();
        itensEmEdicao = [];
        renderItens();
    }

    async function salvarPedido(event) {
        event.preventDefault();

        const id = document.getElementById("pedidoId").value;
        const pedido = montarPedidoPayload(id);

        try {
            const salvo = pedido.id
                ? await AdminApi.pedidos.atualizar(pedido)
                : await AdminApi.pedidos.salvar(pedido);

            atualizarPedidoLocal(salvo);
            U.toast(pedido.id ? "Pedido atualizado." : "Pedido cadastrado.");
            fecharFormulario();
            await carregarDados();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }

    function montarPedidoPayload(id) {
        const formaPagamento = document.getElementById("pedidoPagamento").value;
        const observacao = document.getElementById("pedidoObservacao").value.trim();

        return {
            id: id ? Number(id) : null,
            dataHoraPedido: null,
            status: null,
            valorTotal: null,
            formaPagamento: formaPagamento || null,
            observacao: observacao || null,
            clienteId: Number(document.getElementById("pedidoCliente").value),
            itens: itensEmEdicao.map((item) => ({
                id: item.id || null,
                quantidade: Number(item.quantidade),
                precoUnitario: item.precoUnitario || null,
                subTotal: item.subTotal || null,
                produtoId: Number(item.produtoId)
            }))
        };
    }

    async function adicionarItem() {
        const pedidoId = document.getElementById("pedidoId").value;
        const produtoId = Number(document.getElementById("itemProduto").value);
        const quantidade = Number(document.getElementById("itemQuantidade").value);
        const produto = produtos.find((item) => item.id === produtoId);

        if (!produtoId || !produto) {
            U.toast("Informe um produto.", "error");
            return;
        }

        if (!quantidade || quantidade <= 0) {
            U.toast("Quantidade deve ser maior que zero.", "error");
            return;
        }

        const item = {
            id: null,
            quantidade,
            precoUnitario: null,
            subTotal: null,
            produtoId
        };

        try {
            if (pedidoId) {
                const pedidoAtualizado = await AdminApi.pedidos.adicionarItem(Number(pedidoId), item);
                itensEmEdicao = [...(pedidoAtualizado.itens || [])];
                atualizarPedidoLocal(pedidoAtualizado);
                U.toast("Item adicionado.");
                renderPedidos();
            } else {
                const preco = Number(produto.preco || 0);
                itensEmEdicao.push({
                    ...item,
                    precoUnitario: preco,
                    subTotal: preco * quantidade
                });
                U.toast("Item adicionado.");
            }

            document.getElementById("itemProduto").value = "";
            document.getElementById("itemQuantidade").value = 1;
            renderItens();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }

    async function removerItem(index) {
        const pedidoId = document.getElementById("pedidoId").value;
        const item = itensEmEdicao[index];

        if (!item) {
            return;
        }

        try {
            if (pedidoId && item.id) {
                const pedidoAtualizado = await AdminApi.pedidos.removerItem(Number(pedidoId), item.id);
                itensEmEdicao = [...(pedidoAtualizado.itens || [])];
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
    }

    function renderItens() {
        const estado = document.getElementById("itensEstado");
        const tabela = document.getElementById("itensTabela");
        const corpo = document.getElementById("itensCorpo");

        U.limpar(corpo);

        if (itensEmEdicao.length === 0) {
            U.setEstado(estado, "", "Nenhum item adicionado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        itensEmEdicao.forEach((item, index) => {
            const produto = produtos.find((produtoItem) => produtoItem.id === item.produtoId);
            const tr = document.createElement("tr");
            const acoes = document.createElement("td");
            const actions = document.createElement("div");

            actions.className = "table-actions";
            actions.appendChild(U.botao("Remover", "btn-danger", () => removerItem(index)));
            acoes.appendChild(actions);

            tr.append(
                U.celula(produto ? produto.nome : `Produto ${item.produtoId}`),
                U.celula(item.quantidade),
                U.celula(U.formatarMoeda(item.precoUnitario)),
                U.celula(U.formatarMoeda(item.subTotal)),
                acoes
            );
            corpo.appendChild(tr);
        });

        U.setEstado(estado, "", "");
        U.mostrarTabela(tabela, true);
    }

    async function atualizarStatus(pedido, select) {
        const statusAnterior = pedido.status;
        const statusNovo = select.value;

        select.disabled = true;

        try {
            const atualizado = await AdminApi.pedidos.atualizarStatus(pedido.id, statusNovo);
            atualizarPedidoLocal(atualizado);
            U.toast("Status atualizado.");
            renderPedidos();
        } catch (error) {
            select.value = statusAnterior;
            U.toast(U.mensagemErro(error), "error");
        } finally {
            select.disabled = select.value === "CANCELADO";
        }
    }

    async function cancelarPedido(pedido) {
        if (!window.confirm(`Cancelar pedido #${pedido.id}?`)) {
            return;
        }

        try {
            const atualizado = await AdminApi.pedidos.cancelar(pedido.id);
            atualizarPedidoLocal(atualizado);
            U.toast("Pedido cancelado.");
            renderPedidos();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }

    function atualizarPedidoLocal(pedidoAtualizado) {
        const index = pedidos.findIndex((pedido) => pedido.id === pedidoAtualizado.id);

        if (index >= 0) {
            pedidos[index] = pedidoAtualizado;
        } else {
            pedidos.push(pedidoAtualizado);
        }
    }

    function nomeCliente(clienteId) {
        const cliente = clientes.find((item) => item.id === clienteId);
        return cliente ? cliente.nome : `Cliente ${clienteId}`;
    }
})();
