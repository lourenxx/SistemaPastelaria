(function () {
    class ApiError extends Error {
        constructor(message, status) {
            super(message);
            this.name = "ApiError";
            this.status = status;
        }
    }

    async function request(path, options = {}) {
        const headers = {
            Accept: "application/json",
            ...(options.headers || {})
        };

        const config = {
            method: options.method || "GET",
            credentials: "same-origin",
            headers
        };

        if (options.body !== undefined) {
            config.headers["Content-Type"] = "application/json";
            config.body = JSON.stringify(options.body);
        }

        const response = await fetch(path, config);

        if (response.status === 401 && !options.noRedirect) {
            window.location.href = "/Cliente/html/login.html";
            throw new ApiError("Sessao expirada. Faca login novamente.", 401);
        }

        if (!response.ok) {
            throw new ApiError(await readError(response), response.status);
        }

        const text = await response.text();
        return text ? JSON.parse(text) : null;
    }

    async function readError(response) {
        const fallback = "Nao foi possivel concluir a operacao.";
        const text = await response.text();

        if (!text) {
            return fallback;
        }

        try {
            const data = JSON.parse(text);
            return data.detail || data.message || data.title || fallback;
        } catch (error) {
            return fallback;
        }
    }

    window.ClienteApi = {
        ApiError,
        sessao: () => request("/sessao"),
        logout: () => request("/sessao/logout", { method: "POST" }),
        login: (credenciais) => request("/clientes/login", { method: "POST", body: credenciais, noRedirect: true }),
        cadastro: (cliente) => request("/clientes/cadastro", { method: "POST", body: cliente, noRedirect: true }),
        cardapio: () => request("/produtos/cardapio"),
        fazerPedido: (pedido) => request("/clientes/me/pedidos", { method: "POST", body: pedido })
    };
})();
