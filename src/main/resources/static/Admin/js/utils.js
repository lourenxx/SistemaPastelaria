(function () {
    const moeda = new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    });

    function formatarMoeda(valor) {
        const numero = Number(valor || 0);
        return moeda.format(Number.isFinite(numero) ? numero : 0);
    }

    function formatarDataHora(valor) {
        if (!valor) {
            return "-";
        }

        const data = new Date(valor);
        if (Number.isNaN(data.getTime())) {
            return valor;
        }

        return data.toLocaleString("pt-BR", {
            day: "2-digit",
            month: "2-digit",
            year: "numeric",
            hour: "2-digit",
            minute: "2-digit"
        });
    }

    function texto(valor) {
        if (valor === null || valor === undefined || valor === "") {
            return "-";
        }

        return String(valor);
    }

    function normalizar(valor) {
        return texto(valor)
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "");
    }

    function setEstado(elemento, tipo, mensagem) {
        elemento.hidden = !mensagem;
        elemento.textContent = mensagem || "";
        elemento.className = tipo ? `state state-${tipo}` : "state";
    }

    function mostrarTabela(tabela, mostrar) {
        tabela.hidden = !mostrar;
    }

    function limpar(elemento) {
        while (elemento.firstChild) {
            elemento.removeChild(elemento.firstChild);
        }
    }

    function celula(valor) {
        const td = document.createElement("td");
        td.textContent = texto(valor);
        return td;
    }

    function botao(rotulo, classe, acao) {
        const button = document.createElement("button");
        button.type = "button";
        button.className = `btn ${classe}`;
        button.textContent = rotulo;
        button.addEventListener("click", acao);
        return button;
    }

    function badge(rotulo, tipo) {
        const span = document.createElement("span");
        span.className = `badge badge-${tipo}`;
        span.textContent = rotulo;
        return span;
    }

    function toast(mensagem, tipo = "success") {
        const container = document.getElementById("toastContainer");
        if (!container) {
            return;
        }

        const item = document.createElement("div");
        item.className = `toast toast-${tipo}`;
        item.textContent = mensagem;
        container.appendChild(item);

        window.setTimeout(() => item.remove(), 4200);
    }

    function mensagemErro(error) {
        return error && error.message ? error.message : "Nao foi possivel concluir a operacao.";
    }

    function statusBadgeTipo(status) {
        if (status === "CANCELADO") {
            return "danger";
        }

        if (status === "ENTREGUE" || status === "PRONTO") {
            return "success";
        }

        if (status === "EM_PREPARO") {
            return "warning";
        }

        return "muted";
    }

    window.AdminUtils = {
        formatarMoeda,
        formatarDataHora,
        texto,
        normalizar,
        setEstado,
        mostrarTabela,
        limpar,
        celula,
        botao,
        badge,
        toast,
        mensagemErro,
        statusBadgeTipo
    };
})();
