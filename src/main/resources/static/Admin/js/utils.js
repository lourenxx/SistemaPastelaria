var AdminUtils = (function () {
    var moeda = new Intl.NumberFormat("pt-BR", {
        style: "currency",
        currency: "BRL"
    });

    var formatarMoeda = function (valor) {
        var numero = Number(valor || 0);
        return moeda.format(Number.isFinite(numero) ? numero : 0);
    };

    var formatarDataHora = function (valor) {
        if (!valor) {
            return "-";
        }

        var data = new Date(valor);
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
    };

    var texto = function (valor) {
        if (valor === null || valor === undefined || valor === "") {
            return "-";
        }

        return String(valor);
    };

    var normalizar = function (valor) {
        return texto(valor)
            .toLowerCase()
            .normalize("NFD")
            .replace(/[\u0300-\u036f]/g, "");
    };

    var setEstado = function (elemento, tipo, mensagem) {
        $(elemento)
            .prop("hidden", !mensagem)
            .text(mensagem || "")
            .attr("class", tipo ? "state state-" + tipo : "state");
    };

    var mostrarTabela = function (tabela, mostrar) {
        $(tabela).prop("hidden", !mostrar);
    };

    var limpar = function (elemento) {
        $(elemento).empty();
    };

    var celula = function (valor) {
        return $("<td>").text(texto(valor))[0];
    };

    var botao = function (rotulo, classe, acao) {
        return $("<button>", {
            type: "button",
            class: "btn " + classe
        })
            .text(rotulo)
            .on("click", acao)[0];
    };

    var badge = function (rotulo, tipo) {
        return $("<span>", {
            class: "badge badge-" + tipo
        })
            .text(rotulo)[0];
    };

    var toast = function (mensagem, tipo = "success") {
        var $container = $("#toastContainer");
        if (!$container.length) {
            return;
        }

        var $item = $("<div>", {
            class: "toast toast-" + tipo
        })
            .text(mensagem)
            .appendTo($container);

        window.setTimeout(function () {
            $item.remove();
        }, 4200);
    };

    var mensagemErro = function (error) {
        return error && error.message ? error.message : "Nao foi possivel concluir a operacao.";
    };

    var statusBadgeTipo = function (status) {
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
    };

    return {
        formatarMoeda: formatarMoeda,
        formatarDataHora: formatarDataHora,
        texto: texto,
        normalizar: normalizar,
        setEstado: setEstado,
        mostrarTabela: mostrarTabela,
        limpar: limpar,
        celula: celula,
        botao: botao,
        badge: badge,
        toast: toast,
        mensagemErro: mensagemErro,
        statusBadgeTipo: statusBadgeTipo
    };
}());
