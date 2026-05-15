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

        if (response.status === 401) {
            window.location.href = "/Usuario/html/login.html";
            throw new ApiError("Sessao expirada. Faca login novamente.", 401);
        }

        if (response.status === 403) {
            throw new ApiError("Acesso negado para este perfil.", 403);
        }

        if (!response.ok) {
            throw new ApiError(await readError(response), response.status);
        }

        if (response.status === 204) {
            return null;
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

    window.AdminApi = {
        ApiError,
        produtos: {
            listar: () => request("/produtos"),
            buscar: (id) => request(`/produtos/${id}`),
            salvar: (produto) => request("/produtos", { method: "POST", body: produto }),
            atualizar: (produto) => request("/produtos", { method: "PUT", body: produto }),
            excluir: (id) => request(`/produtos/${id}`, { method: "DELETE" })
        },
        clientes: {
            listar: () => request("/clientes"),
            buscar: (id) => request(`/clientes/${id}`),
            salvar: (cliente) => request("/clientes", { method: "POST", body: cliente }),
            atualizar: (cliente) => request("/clientes", { method: "PUT", body: cliente }),
            excluir: (id) => request(`/clientes/${id}`, { method: "DELETE" })
        },
        pedidos: {
            listar: () => request("/pedidos"),
            buscar: (id) => request(`/pedidos/${id}`),
            salvar: (pedido) => request("/pedidos", { method: "POST", body: pedido }),
            atualizar: (pedido) => request("/pedidos", { method: "PUT", body: pedido }),
            excluir: (id) => request(`/pedidos/${id}`, { method: "DELETE" }),
            atualizarStatus: (id, status) => request(`/pedidos/${id}/status`, {
                method: "PATCH",
                body: {
                    id,
                    dataHoraPedido: null,
                    status,
                    valorTotal: null,
                    formaPagamento: null,
                    observacao: null,
                    clienteId: null,
                    itens: null
                }
            }),
            cancelar: (id) => request(`/pedidos/${id}/cancelar`, { method: "PATCH" }),
            adicionarItem: (id, item) => request(`/pedidos/${id}/itens`, { method: "POST", body: item }),
            removerItem: (id, itemId) => request(`/pedidos/${id}/itens/${itemId}`, { method: "DELETE" })
        }
    };
})();
