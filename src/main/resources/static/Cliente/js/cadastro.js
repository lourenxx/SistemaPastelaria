(function () {
    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("clienteCadastroForm").addEventListener("submit", cadastrar);
    });

    async function cadastrar(event) {
        event.preventDefault();

        const mensagem = document.getElementById("mensagemCadastro");
        const button = event.target.querySelector("button[type='submit']");

        mensagem.textContent = "";
        mensagem.className = "message";
        button.disabled = true;
        button.textContent = "Cadastrando...";

        try {
            await ClienteApi.cadastro({
                nome: document.getElementById("nome").value.trim(),
                telefone: document.getElementById("telefone").value.trim(),
                endereco: document.getElementById("endereco").value.trim(),
                email: document.getElementById("email").value.trim(),
                senha: document.getElementById("senha").value
            });

            mensagem.className = "message success";
            mensagem.textContent = "Cadastro realizado. Redirecionando para o login...";
            window.setTimeout(() => {
                window.location.href = "/Cliente/html/login.html";
            }, 900);
        } catch (error) {
            mensagem.className = "message error";
            mensagem.textContent = error.message;
        } finally {
            button.disabled = false;
            button.textContent = "Cadastrar cliente";
        }
    }
})();
