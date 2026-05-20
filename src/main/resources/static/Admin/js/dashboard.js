var Dashboard = (function () {
    var U = AdminUtils;

    var obterElementos = function () {
        return {
            $estado: $("#dashboardEstado"),
            $metricas: $("#dashboardMetricas"),
            $conteudo: $("#dashboardConteudo"),
            $pedidosRecentesEstado: $("#pedidosRecentesEstado"),
            $pedidosRecentesTabela: $("#pedidosRecentesTabela"),
            $pedidosRecentesCorpo: $("#pedidosRecentesCorpo"),
            $statusResumo: $("#statusResumo")
        };
    };

    var carregarDashboard = async function () {
        var elementos = obterElementos();

        U.setEstado(elementos.$estado, "", "Carregando dashboard...");
        elementos.$metricas.prop("hidden", true);
        elementos.$conteudo.prop("hidden", true);

        try {
            var dados = await Promise.all([
                AdminApi.produtos.listar(),
                AdminApi.clientes.listar(),
                AdminApi.pedidos.listar()
            ]);
            var produtos = dados[0];
            var clientes = dados[1];
            var pedidos = dados[2];

            U.setEstado(elementos.$estado, "", "");
            renderMetricas(elementos.$metricas, produtos, clientes, pedidos);
            renderPedidosRecentes(pedidos, clientes);
            renderResumoStatus(pedidos);
            elementos.$metricas.prop("hidden", false);
            elementos.$conteudo.prop("hidden", false);
        } catch (error) {
            U.setEstado(elementos.$estado, "error", U.mensagemErro(error));
        }
    };

    var renderMetricas = function ($container, produtos, clientes, pedidos) {
        U.limpar($container);

        var produtosDisponiveis = produtos.filter(function (produto) {
            return produto.disponivel;
        }).length;
        var pedidosAtivos = pedidos.filter(function (pedido) {
            return pedido.status !== "CANCELADO";
        }).length;
        var valorTotal = pedidos
            .filter(function (pedido) {
                return pedido.status !== "CANCELADO";
            })
            .reduce(function (total, pedido) {
                return total + Number(pedido.valorTotal || 0);
            }, 0);

        [
            ["Produtos", produtos.length],
            ["Disponiveis", produtosDisponiveis],
            ["Clientes", clientes.length],
            ["Pedidos ativos", pedidosAtivos],
            ["Total vendido", U.formatarMoeda(valorTotal)]
        ].forEach(function (metrica) {
            var rotulo = metrica[0];
            var valor = metrica[1];

            $("<article>", { class: "metric-card" })
                .append(
                    $("<span>").text(rotulo),
                    $("<strong>").text(String(valor))
                )
                .appendTo($container);
        });
    };

    var renderPedidosRecentes = function (pedidos, clientes) {
        var elementos = obterElementos();
        var clientesPorId = new Map(clientes.map(function (cliente) {
            return [cliente.id, cliente];
        }));

        U.limpar(elementos.$pedidosRecentesCorpo);

        var recentes = pedidos.slice()
            .sort(function (a, b) {
                return compararPedidosRecentes(a, b);
            })
            .slice(0, 6);

        if (recentes.length === 0) {
            U.setEstado(elementos.$pedidosRecentesEstado, "", "Nenhum pedido cadastrado.");
            U.mostrarTabela(elementos.$pedidosRecentesTabela, false);
            return;
        }

        recentes.forEach(function (pedido) {
            var cliente = clientesPorId.get(pedido.clienteId);
            var $status = $("<td>").append(U.badge(U.texto(pedido.status), U.statusBadgeTipo(pedido.status)));

            $("<tr>")
                .append(
                    U.celula("#" + pedido.id),
                    U.celula(cliente ? cliente.nome : "Cliente " + pedido.clienteId),
                    $status,
                    U.celula(U.formatarMoeda(pedido.valorTotal)),
                    U.celula(U.formatarDataHora(pedido.dataHoraPedido))
                )
                .appendTo(elementos.$pedidosRecentesCorpo);
        });

        U.setEstado(elementos.$pedidosRecentesEstado, "", "");
        U.mostrarTabela(elementos.$pedidosRecentesTabela, true);
    };

    var renderResumoStatus = function (pedidos) {
        var elementos = obterElementos();
        var statusLista = ["ABERTO", "EM_PREPARO", "PRONTO", "ENTREGUE", "CANCELADO"];

        U.limpar(elementos.$statusResumo);

        statusLista.forEach(function (status) {
            var total = pedidos.filter(function (pedido) {
                return pedido.status === status;
            }).length;

            $("<div>", { class: "status-row" })
                .append(
                    U.badge(status, U.statusBadgeTipo(status)),
                    $("<strong>").text(String(total))
                )
                .appendTo(elementos.$statusResumo);
        });
    };

    var compararPedidosRecentes = function (a, b) {
        var dataA = a.dataHoraPedido ? new Date(a.dataHoraPedido).getTime() : 0;
        var dataB = b.dataHoraPedido ? new Date(b.dataHoraPedido).getTime() : 0;

        if (dataA !== dataB) {
            return dataB - dataA;
        }

        return Number(b.id || 0) - Number(a.id || 0);
    };

    var init = function () {
        carregarDashboard();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        carregarDashboard: carregarDashboard,
        renderMetricas: renderMetricas,
        renderPedidosRecentes: renderPedidosRecentes,
        renderResumoStatus: renderResumoStatus,
        compararPedidosRecentes: compararPedidosRecentes
    };
}());
