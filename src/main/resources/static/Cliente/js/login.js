(function () {
    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("clienteLoginForm").addEventListener("submit", entrar);
    });

    async function entrar(event) {
        event.preventDefault();

        const mensagem = document.getElementById("mensagemErro");
        const button = event.target.querySelector("button[type='submit']");

        mensagem.textContent = "";
        mensagem.className = "message error";
        button.disabled = true;
        button.textContent = "Entrando...";

        try {
            const sessao = await ClienteApi.login({
                email: document.getElementById("email").value.trim(),
                senha: document.getElementById("senha").value
            });

            if (sessao.tipo !== "CLIENTE") {
                throw new Error("Perfil de acesso invalido.");
            }

            window.location.href = "/Cliente/html/pedidos.html";
        } catch (error) {
            mensagem.textContent = error.message;
        } finally {
            button.disabled = false;
            button.textContent = "Entrar";
        }
    }
})();
