var UsuarioApi = (function () {
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

        if (!response.ok) {
            throw new ApiError(await readError(response, options.fallback), response.status);
        }

        if (response.status === 204) {
            return null;
        }

        var text = await response.text();
        return text ? JSON.parse(text) : null;
    };

    var readError = async function (response, fallback = "Nao foi possivel concluir a operacao.") {
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
        login: function (credenciais) {
            return request("/usuarios/login", {
                method: "POST",
                body: credenciais,
                fallback: "Login ou senha invalidos."
            });
        },
        verificarCodigo: function (codigo) {
            return request("/usuarios/login/verificar-codigo", {
                method: "POST",
                body: { codigo: codigo },
                fallback: "Codigo invalido."
            });
        }
    };
}());
