(function () {
    const U = window.AdminUtils;

    document.addEventListener("DOMContentLoaded", carregarDashboard);

    async function carregarDashboard() {
        const estado = document.getElementById("dashboardEstado");
        const metricas = document.getElementById("dashboardMetricas");
        const conteudo = document.getElementById("dashboardConteudo");

        U.setEstado(estado, "", "Carregando dashboard...");
        metricas.hidden = true;
        conteudo.hidden = true;

        try {
            const [produtos, clientes, pedidos] = await Promise.all([
                AdminApi.produtos.listar(),
                AdminApi.clientes.listar(),
                AdminApi.pedidos.listar()
            ]);

            U.setEstado(estado, "", "");
            renderMetricas(metricas, produtos, clientes, pedidos);
            renderPedidosRecentes(pedidos, clientes);
            renderResumoStatus(pedidos);
            metricas.hidden = false;
            conteudo.hidden = false;
        } catch (error) {
            U.setEstado(estado, "error", U.mensagemErro(error));
        }
    }

    function renderMetricas(container, produtos, clientes, pedidos) {
        U.limpar(container);

        const produtosDisponiveis = produtos.filter((produto) => produto.disponivel).length;
        const pedidosAtivos = pedidos.filter((pedido) => pedido.status !== "CANCELADO").length;
        const valorTotal = pedidos
            .filter((pedido) => pedido.status !== "CANCELADO")
            .reduce((total, pedido) => total + Number(pedido.valorTotal || 0), 0);

        [
            ["Produtos", produtos.length],
            ["Disponiveis", produtosDisponiveis],
            ["Clientes", clientes.length],
            ["Pedidos ativos", pedidosAtivos],
            ["Total vendido", U.formatarMoeda(valorTotal)]
        ].forEach(([rotulo, valor]) => {
            const card = document.createElement("article");
            card.className = "metric-card";

            const span = document.createElement("span");
            span.textContent = rotulo;

            const strong = document.createElement("strong");
            strong.textContent = String(valor);

            card.append(span, strong);
            container.appendChild(card);
        });
    }

    function renderPedidosRecentes(pedidos, clientes) {
        const estado = document.getElementById("pedidosRecentesEstado");
        const tabela = document.getElementById("pedidosRecentesTabela");
        const corpo = document.getElementById("pedidosRecentesCorpo");
        const clientesPorId = new Map(clientes.map((cliente) => [cliente.id, cliente]));

        U.limpar(corpo);

        const recentes = [...pedidos]
            .sort((a, b) => compararPedidosRecentes(a, b))
            .slice(0, 6);

        if (recentes.length === 0) {
            U.setEstado(estado, "", "Nenhum pedido cadastrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        recentes.forEach((pedido) => {
            const tr = document.createElement("tr");
            const cliente = clientesPorId.get(pedido.clienteId);
            const status = document.createElement("td");

            status.appendChild(U.badge(U.texto(pedido.status), U.statusBadgeTipo(pedido.status)));
            tr.append(
                U.celula(`#${pedido.id}`),
                U.celula(cliente ? cliente.nome : `Cliente ${pedido.clienteId}`),
                status,
                U.celula(U.formatarMoeda(pedido.valorTotal)),
                U.celula(U.formatarDataHora(pedido.dataHoraPedido))
            );
            corpo.appendChild(tr);
        });

        U.setEstado(estado, "", "");
        U.mostrarTabela(tabela, true);
    }

    function renderResumoStatus(pedidos) {
        const container = document.getElementById("statusResumo");
        const statusLista = ["ABERTO", "EM_PREPARO", "PRONTO", "ENTREGUE", "CANCELADO"];

        U.limpar(container);

        statusLista.forEach((status) => {
            const total = pedidos.filter((pedido) => pedido.status === status).length;
            const row = document.createElement("div");
            row.className = "status-row";

            const label = U.badge(status, U.statusBadgeTipo(status));
            const count = document.createElement("strong");
            count.textContent = String(total);

            row.append(label, count);
            container.appendChild(row);
        });
    }

    function compararPedidosRecentes(a, b) {
        const dataA = a.dataHoraPedido ? new Date(a.dataHoraPedido).getTime() : 0;
        const dataB = b.dataHoraPedido ? new Date(b.dataHoraPedido).getTime() : 0;

        if (dataA !== dataB) {
            return dataB - dataA;
        }

        return Number(b.id || 0) - Number(a.id || 0);
    }
})();
