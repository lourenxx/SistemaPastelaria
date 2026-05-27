var AdminApi = (function () {
    var ApiError = class ApiError extends Error {
        constructor(message, status) {
            super(message);
            this.name = "ApiError";
            this.status = status;
        }
    };

    var request = async function (path, options = {}) {
        var headers = {
            Accept: "application/json",
            ...(options.headers || {})
        };

        var config = {
            method: options.method || "GET",
            credentials: "same-origin",
            headers: headers
        };

        if (options.body !== undefined) {
            config.headers["Content-Type"] = "application/json";
            config.body = JSON.stringify(options.body);
        }

        var response = await fetch(path, config);

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

        var text = await response.text();
        return text ? JSON.parse(text) : null;
    };

    var readError = async function (response) {
        var fallback = "Nao foi possivel concluir a operacao.";
        var text = await response.text();

        if (!text) {
            return fallback;
        }

        try {
            var data = JSON.parse(text);
            return data.detail || data.message || data.title || fallback;
        } catch (error) {
            return fallback;
        }
    };

    return {
        ApiError: ApiError,
        sessao: {
            logout: function () {
                return request("/sessao/logout", { method: "POST" });
            }
        },
        produtos: {
            listar: function () {
                return request("/produtos");
            },
            buscar: function (id) {
                return request("/produtos/" + id);
            },
            salvar: function (produto) {
                return request("/produtos", { method: "POST", body: produto });
            },
            atualizar: function (produto) {
                return request("/produtos", { method: "PUT", body: produto });
            },
            excluir: function (id) {
                return request("/produtos/" + id, { method: "DELETE" });
            }
        },
        clientes: {
            listar: function () {
                return request("/clientes");
            },
            buscar: function (id) {
                return request("/clientes/" + id);
            },
            salvar: function (cliente) {
                return request("/clientes", { method: "POST", body: cliente });
            },
            atualizar: function (cliente) {
                return request("/clientes", { method: "PUT", body: cliente });
            },
            excluir: function (id) {
                return request("/clientes/" + id, { method: "DELETE" });
            }
        },
        pedidos: {
            listar: function () {
                return request("/pedidos");
            },
            buscar: function (id) {
                return request("/pedidos/" + id);
            },
            salvar: function (pedido) {
                return request("/pedidos", { method: "POST", body: pedido });
            },
            atualizar: function (pedido) {
                return request("/pedidos", { method: "PUT", body: pedido });
            },
            excluir: function (id) {
                return request("/pedidos/" + id, { method: "DELETE" });
            },
            atualizarStatus: function (id, status) {
                return request("/pedidos/" + id + "/status", {
                    method: "PATCH",
                    body: {
                        id: id,
                        dataHoraPedido: null,
                        status: status,
                        valorTotal: null,
                        formaPagamento: null,
                        observacao: null,
                        clienteId: null,
                        itens: null
                    }
                });
            },
            cancelar: function (id) {
                return request("/pedidos/" + id + "/cancelar", { method: "PATCH" });
            },
            adicionarItem: function (id, item) {
                return request("/pedidos/" + id + "/itens", { method: "POST", body: item });
            },
            removerItem: function (id, itemId) {
                return request("/pedidos/" + id + "/itens/" + itemId, { method: "DELETE" });
            }
        }
    };
}());
