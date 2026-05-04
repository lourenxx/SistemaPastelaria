const formLogin = document.getElementById("formLogin");
const botaoEntrar = document.getElementById("botaoEntrar");
const mensagemErro = document.getElementById("mensagemErro");

formLogin.addEventListener("submit", async (event) => {
    event.preventDefault();

    mensagemErro.textContent = "";
    botaoEntrar.disabled = true;
    botaoEntrar.textContent = "Entrando...";

    try {
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
            throw new Error("Login ou senha invalidos.");
        }

        window.location.href = "sucesso.html";
    } catch (error) {
        mensagemErro.textContent = error.message;
    } finally {
        botaoEntrar.disabled = false;
        botaoEntrar.textContent = "Entrar";
    }
});
