var LoginCliente = (function () {
    var obterElementos = function () {
        return {
            $form: $("#clienteLoginForm"),
            $mensagem: $("#mensagemErro"),
            $email: $("#email"),
            $senha: $("#senha")
        };
    };

    var bindEventos = function () {
        obterElementos().$form.on("submit", entrar);
    };

    var entrar = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();
        var $button = $(event.currentTarget).find("button[type='submit']");

        elementos.$mensagem
            .text("")
            .attr("class", "message error");
        $button
            .prop("disabled", true)
            .text("Entrando...");

        try {
            var sessao = await ClienteApi.login({
                email: (elementos.$email.val() || "").trim(),
                senha: elementos.$senha.val()
            });

            if (sessao.tipo !== "CLIENTE") {
                throw new Error("Perfil de acesso invalido.");
            }

            window.location.href = "/Cliente/html/pedidos.html";
        } catch (error) {
            elementos.$mensagem.text(error.message);
        } finally {
            $button
                .prop("disabled", false)
                .text("Entrar");
        }
    };

    var init = function () {
        bindEventos();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        entrar: entrar
    };
}());
