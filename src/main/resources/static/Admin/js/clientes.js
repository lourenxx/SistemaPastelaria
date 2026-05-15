(function () {
    const U = window.AdminUtils;
    let clientes = [];

    document.addEventListener("DOMContentLoaded", () => {
        document.getElementById("novoCliente").addEventListener("click", abrirNovoCliente);
        document.getElementById("cancelarCliente").addEventListener("click", fecharFormulario);
        document.getElementById("clienteForm").addEventListener("submit", salvarCliente);
        document.getElementById("buscaCliente").addEventListener("input", renderClientes);
        carregarClientes();
    });

    async function carregarClientes() {
        const estado = document.getElementById("clientesEstado");
        const tabela = document.getElementById("clientesTabela");

        U.setEstado(estado, "", "Carregando clientes...");
        U.mostrarTabela(tabela, false);

        try {
            clientes = await AdminApi.clientes.listar();
            renderClientes();
        } catch (error) {
            U.setEstado(estado, "error", U.mensagemErro(error));
        }
    }

    function renderClientes() {
        const estado = document.getElementById("clientesEstado");
        const tabela = document.getElementById("clientesTabela");
        const corpo = document.getElementById("clientesCorpo");
        const termo = U.normalizar(document.getElementById("buscaCliente").value);
        const filtrados = clientes.filter((cliente) => filtrarCliente(cliente, termo));

        U.limpar(corpo);

        if (clientes.length === 0) {
            U.setEstado(estado, "", "Nenhum cliente cadastrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        if (filtrados.length === 0) {
            U.setEstado(estado, "", "Nenhum cliente encontrado.");
            U.mostrarTabela(tabela, false);
            return;
        }

        filtrados.forEach((cliente) => {
            const tr = document.createElement("tr");
            const acoes = document.createElement("td");
            const actions = document.createElement("div");

            actions.className = "table-actions";
            actions.append(
                U.botao("Editar", "btn-secondary", () => abrirEdicao(cliente)),
                U.botao("Excluir", "btn-danger", () => excluirCliente(cliente))
            );
            acoes.appendChild(actions);

            tr.append(
                U.celula(cliente.id),
                U.celula(cliente.nome),
                U.celula(cliente.telefone),
                U.celula(cliente.email),
                U.celula(cliente.endereco),
                acoes
            );
            corpo.appendChild(tr);
        });

        U.setEstado(estado, "", "");
        U.mostrarTabela(tabela, true);
    }

    function filtrarCliente(cliente, termo) {
        if (!termo || termo === "-") {
            return true;
        }

        return [
            cliente.id,
            cliente.nome,
            cliente.telefone,
            cliente.email,
            cliente.endereco
        ].some((valor) => U.normalizar(valor).includes(termo));
    }

    function abrirNovoCliente() {
        document.getElementById("clienteFormTitulo").textContent = "Novo cliente";
        document.getElementById("clienteForm").reset();
        document.getElementById("clienteId").value = "";
        document.getElementById("clienteFormPanel").hidden = false;
        document.getElementById("clienteNome").focus();
    }

    function abrirEdicao(cliente) {
        document.getElementById("clienteFormTitulo").textContent = `Editar cliente #${cliente.id}`;
        document.getElementById("clienteId").value = cliente.id;
        document.getElementById("clienteNome").value = cliente.nome || "";
        document.getElementById("clienteTelefone").value = cliente.telefone || "";
        document.getElementById("clienteEmail").value = cliente.email || "";
        document.getElementById("clienteEndereco").value = cliente.endereco || "";
        document.getElementById("clienteFormPanel").hidden = false;
        document.getElementById("clienteNome").focus();
    }

    function fecharFormulario() {
        document.getElementById("clienteFormPanel").hidden = true;
        document.getElementById("clienteForm").reset();
    }

    async function salvarCliente(event) {
        event.preventDefault();

        const id = document.getElementById("clienteId").value;
        const email = document.getElementById("clienteEmail").value.trim();
        const cliente = {
            id: id ? Number(id) : null,
            nome: document.getElementById("clienteNome").value.trim(),
            telefone: document.getElementById("clienteTelefone").value.trim(),
            endereco: document.getElementById("clienteEndereco").value.trim(),
            email: email || null
        };

        try {
            if (cliente.id) {
                await AdminApi.clientes.atualizar(cliente);
                U.toast("Cliente atualizado.");
            } else {
                await AdminApi.clientes.salvar(cliente);
                U.toast("Cliente cadastrado.");
            }

            fecharFormulario();
            await carregarClientes();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }

    async function excluirCliente(cliente) {
        if (!window.confirm(`Excluir cliente ${cliente.nome}?`)) {
            return;
        }

        try {
            await AdminApi.clientes.excluir(cliente.id);
            U.toast("Cliente excluido.");
            await carregarClientes();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    }
})();
