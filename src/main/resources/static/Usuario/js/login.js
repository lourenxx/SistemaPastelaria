const formLogin = document.getElementById("formLogin");
const botaoEntrar = document.getElementById("botaoEntrar");
const mensagemErro = document.getElementById("mensagemErro");
const camposCredenciais = document.getElementById("camposCredenciais");
const campoCodigo = document.getElementById("campoCodigo");
const emailDestino = document.getElementById("emailDestino");
const codigo = document.getElementById("codigo");

let etapa = "credenciais";

formLogin.addEventListener("submit", async (event) => {
    event.preventDefault();

    mensagemErro.textContent = "";
    botaoEntrar.disabled = true;
    botaoEntrar.textContent = etapa === "credenciais" ? "Enviando codigo..." : "Verificando...";

    try {
        if (etapa === "credenciais") {
            await enviarCredenciais();
            return;
        }

        await verificarCodigo();
    } catch (error) {
        mensagemErro.textContent = error.message;
    } finally {
        botaoEntrar.disabled = false;
        botaoEntrar.textContent = etapa === "credenciais" ? "Entrar" : "Verificar codigo";
    }
});

async function enviarCredenciais() {
    const resposta = await fetch("/usuarios/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            login: document.getElementById("login").value,
            senha: document.getElementById("senha").value
        })
    });

    if (!resposta.ok) {
        throw new Error(await lerMensagemErro(resposta, "Login ou senha invalidos."));
    }

    const dados = await resposta.json();
    etapa = "codigo";
    camposCredenciais.hidden = true;
    campoCodigo.hidden = false;
    codigo.required = true;
    codigo.value = "";
    emailDestino.textContent = dados.emailMascarado
        ? `Codigo enviado para ${dados.emailMascarado}.`
        : "Codigo enviado para o email cadastrado.";
    codigo.focus();
}

async function verificarCodigo() {
    const resposta = await fetch("/usuarios/login/verificar-codigo", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            codigo: codigo.value
        })
    });

    if (!resposta.ok) {
        throw new Error(await lerMensagemErro(resposta, "Codigo invalido."));
    }

    window.location.href = "/Admin/html/dashboard.html";
}

async function lerMensagemErro(resposta, mensagemPadrao) {
    const texto = await resposta.text();
    if (!texto) {
        return mensagemPadrao;
    }

    try {
        const dados = JSON.parse(texto);
        return dados.detail || dados.message || mensagemPadrao;
    } catch (error) {
        return mensagemPadrao;
    }
}
