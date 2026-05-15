(function () {
    const moeda = new Intl.NumberFormat("pt-BR", { style: "currency", currency: "BRL" });
    let produtos = [];
    let carrinho = [];

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("pedidoForm").addEventListener("submit", enviarPedido);
        document.getElementById("sairButton").addEventListener("click", sair);
        iniciar();
    });

    async function iniciar() {
        try {
            const sessao = await ClienteApi.sessao();

            if (sessao.tipo !== "CLIENTE") {
                window.location.href = "/index.html";
                return;
            }

            document.getElementById("clienteNome").textContent = sessao.nome || sessao.email || "";
            await carregarCardapio();
        } catch (error) {
            document.getElementById("produtosEstado").textContent = error.message;
            document.getElementById("produtosEstado").className = "state error";
        }
    }

    async function carregarCardapio() {
        const estado = document.getElementById("produtosEstado");
        estado.textContent = "Carregando...";
        estado.className = "state";

        produtos = await ClienteApi.cardapio();
        renderProdutos();
    }

    function renderProdutos() {
        const lista = document.getElementById("produtosLista");
        const estado = document.getElementById("produtosEstado");

        lista.innerHTML = "";

        if (!produtos.length) {
            estado.textContent = "Nenhum produto disponivel.";
            return;
        }

        estado.textContent = "";

        produtos.forEach((produto) => {
            const card = document.createElement("article");
            const titulo = document.createElement("h3");
            const descricao = document.createElement("p");
            const meta = document.createElement("div");
            const categoria = document.createElement("span");
            const preco = document.createElement("strong");
            const actions = document.createElement("div");
            const quantidade = document.createElement("input");
            const adicionar = document.createElement("button");

            card.className = "product-card";
            titulo.textContent = produto.nome;
            descricao.textContent = produto.descricao || "Produto da casa";
            meta.className = "product-meta";
            categoria.textContent = produto.categoria || "Cardapio";
            preco.textContent = moeda.format(Number(produto.preco || 0));
            actions.className = "product-actions";
            quantidade.type = "number";
            quantidade.min = "1";
            quantidade.step = "1";
            quantidade.value = "1";
            adicionar.type = "button";
            adicionar.className = "primary-action";
            adicionar.textContent = "Adicionar";
            adicionar.addEventListener("click", () => adicionarAoCarrinho(produto, Number(quantidade.value)));

            meta.append(categoria, preco);
            actions.append(quantidade, adicionar);
            card.append(titulo, descricao, meta, actions);
            lista.appendChild(card);
        });
    }

    function adicionarAoCarrinho(produto, quantidade) {
        const mensagem = document.getElementById("pedidoMensagem");
        mensagem.textContent = "";
        mensagem.className = "message";

        if (!quantidade || quantidade <= 0) {
            mensagem.className = "message error";
            mensagem.textContent = "Quantidade deve ser maior que zero.";
            return;
        }

        const item = carrinho.find((atual) => atual.produto.id === produto.id);

        if (item) {
            item.quantidade += quantidade;
        } else {
            carrinho.push({ produto, quantidade });
        }

        renderCarrinho();
    }

    function renderCarrinho() {
        const lista = document.getElementById("carrinhoLista");
        const estado = document.getElementById("carrinhoEstado");
        const total = carrinho.reduce((soma, item) => soma + Number(item.produto.preco || 0) * item.quantidade, 0);

        lista.innerHTML = "";
        document.getElementById("pedidoTotal").textContent = moeda.format(total);

        if (!carrinho.length) {
            estado.hidden = false;
            estado.textContent = "Nenhum item adicionado.";
            return;
        }

        estado.hidden = true;

        carrinho.forEach((item, index) => {
            const row = document.createElement("div");
            const info = document.createElement("div");
            const nome = document.createElement("strong");
            const detalhes = document.createElement("span");
            const remover = document.createElement("button");
            const subtotal = Number(item.produto.preco || 0) * item.quantidade;

            row.className = "cart-item";
            nome.textContent = item.produto.nome;
            detalhes.textContent = `${item.quantidade} x ${moeda.format(Number(item.produto.preco || 0))} = ${moeda.format(subtotal)}`;
            remover.type = "button";
            remover.className = "cart-remove";
            remover.textContent = "Remover";
            remover.addEventListener("click", () => {
                carrinho.splice(index, 1);
                renderCarrinho();
            });

            info.append(nome, detalhes);
            row.append(info, remover);
            lista.appendChild(row);
        });
    }

    async function enviarPedido(event) {
        event.preventDefault();

        const mensagem = document.getElementById("pedidoMensagem");
        const button = event.target.querySelector("button[type='submit']");

        mensagem.textContent = "";
        mensagem.className = "message";

        if (!carrinho.length) {
            mensagem.className = "message error";
            mensagem.textContent = "Adicione ao menos um item.";
            return;
        }

        button.disabled = true;
        button.textContent = "Enviando...";

        try {
            const pedido = await ClienteApi.fazerPedido({
                formaPagamento: document.getElementById("formaPagamento").value || null,
                observacao: document.getElementById("observacao").value.trim() || null,
                itens: carrinho.map((item) => ({
                    quantidade: item.quantidade,
                    produtoId: item.produto.id
                }))
            });

            carrinho = [];
            renderCarrinho();
            document.getElementById("pedidoForm").reset();
            mensagem.className = "message success";
            mensagem.textContent = `Pedido #${pedido.id} enviado com sucesso.`;
        } catch (error) {
            mensagem.className = "message error";
            mensagem.textContent = error.message;
        } finally {
            button.disabled = false;
            button.textContent = "Fazer pedido";
        }
    }

    async function sair() {
        try {
            await ClienteApi.logout();
        } finally {
            window.location.href = "/index.html";
        }
    }
})();
