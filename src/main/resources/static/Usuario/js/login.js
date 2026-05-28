var LoginUsuario = (function () {
    var etapa = "credenciais";

    var obterElementos = function () {
        return {
            $form: $("#formLogin"),
            $botaoEntrar: $("#botaoEntrar"),
            $mensagemErro: $("#mensagemErro"),
            $login: $("#login"),
            $senha: $("#senha"),
            $camposCredenciais: $("#camposCredenciais"),
            $campoCodigo: $("#campoCodigo"),
            $codigo: $("#codigo"),
            $emailDestino: $("#emailDestino")
        };
    };

    var bindEventos = function () {
        obterElementos().$form.on("submit", enviarFormulario);
    };

    var enviarFormulario = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();

        elementos.$mensagemErro.text("");
        elementos.$botaoEntrar
            .prop("disabled", true)
            .text(etapa === "credenciais" ? "Enviando codigo..." : "Verificando...");

        try {
            if (etapa === "credenciais") {
                await enviarCredenciais();
                return;
            }

            await verificarCodigo();
        } catch (error) {
            elementos.$mensagemErro.text(error.message);
        } finally {
            elementos.$botaoEntrar
                .prop("disabled", false)
                .text(etapa === "credenciais" ? "Entrar" : "Verificar codigo");
        }
    };

    var enviarCredenciais = async function () {
        var elementos = obterElementos();
        var dados = await UsuarioApi.login({
            login: elementos.$login.val(),
            senha: elementos.$senha.val()
        });

        etapa = "codigo";
        elementos.$camposCredenciais.prop("hidden", true);
        elementos.$campoCodigo.prop("hidden", false);
        elementos.$codigo
            .prop("required", true)
            .val("")
            .trigger("focus");
        elementos.$emailDestino.text(dados.emailMascarado
            ? "Codigo enviado para " + dados.emailMascarado + "."
            : "Codigo enviado para o email cadastrado.");
    };

    var verificarCodigo = async function () {
        var elementos = obterElementos();

        await UsuarioApi.verificarCodigo(elementos.$codigo.val());
        window.location.href = "/Admin/html/dashboard.html";
    };

    var init = function () {
        bindEventos();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        enviarFormulario: enviarFormulario,
        enviarCredenciais: enviarCredenciais,
        verificarCodigo: verificarCodigo
    };
}());
