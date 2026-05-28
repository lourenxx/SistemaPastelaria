var CadastroCliente = (function () {
    var obterElementos = function () {
        return {
            $form: $("#clienteCadastroForm"),
            $mensagem: $("#mensagemCadastro"),
            $nome: $("#nome"),
            $telefone: $("#telefone"),
            $endereco: $("#endereco"),
            $email: $("#email"),
            $senha: $("#senha")
        };
    };

    var bindEventos = function () {
        obterElementos().$form.on("submit", cadastrar);
    };

    var cadastrar = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();
        var $button = $(event.currentTarget).find("button[type='submit']");

        elementos.$mensagem
            .text("")
            .attr("class", "message");
        $button
            .prop("disabled", true)
            .text("Cadastrando...");

        try {
            await ClienteApi.cadastro({
                nome: (elementos.$nome.val() || "").trim(),
                telefone: (elementos.$telefone.val() || "").trim(),
                endereco: (elementos.$endereco.val() || "").trim(),
                email: (elementos.$email.val() || "").trim(),
                senha: elementos.$senha.val()
            });

            elementos.$mensagem
                .attr("class", "message success")
                .text("Cadastro realizado. Redirecionando para o login...");
            window.setTimeout(function () {
                window.location.href = "/Cliente/html/login.html";
            }, 900);
        } catch (error) {
            elementos.$mensagem
                .attr("class", "message error")
                .text(error.message);
        } finally {
            $button
                .prop("disabled", false)
                .text("Cadastrar cliente");
        }
    };

    var init = function () {
        bindEventos();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        cadastrar: cadastrar
    };
}());
