var AcessoInicial = (function () {
    var obterElementos = function () {
        return {
            $clienteButton: $("#clienteButton"),
            $clienteOptions: $("#clienteOptions")
        };
    };

    var bindEventos = function () {
        obterElementos().$clienteButton.on("click", alternarOpcoesCliente);
    };

    var alternarOpcoesCliente = function () {
        var elementos = obterElementos();

        elementos.$clienteOptions.prop("hidden", !elementos.$clienteOptions.prop("hidden"));
    };

    var init = function () {
        bindEventos();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        alternarOpcoesCliente: alternarOpcoesCliente
    };
}());
