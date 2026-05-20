var Cliente = (function () {
    var U = AdminUtils;
    var clientes = [];

    var obterElementos = function () {
        return {
            $estado: $("#clientesEstado"),
            $tabela: $("#clientesTabela"),
            $corpo: $("#clientesCorpo"),
            $form: $("#clienteForm"),
            $formPanel: $("#clienteFormPanel"),
            $formTitulo: $("#clienteFormTitulo"),
            $id: $("#clienteId"),
            $nome: $("#clienteNome"),
            $telefone: $("#clienteTelefone"),
            $email: $("#clienteEmail"),
            $endereco: $("#clienteEndereco"),
            $busca: $("#buscaCliente"),
            $cancelar: $("#cancelarCliente")
        };
    };

    var bindEventos = function () {
        var elementos = obterElementos();

        $(document).on("click", "#novoCliente", abrirNovoCliente);
        elementos.$cancelar.on("click", fecharFormulario);
        elementos.$form.on("submit", salvarCliente);
        elementos.$busca.on("input", renderClientes);
    };

    var carregarClientes = async function () {
        var elementos = obterElementos();

        U.setEstado(elementos.$estado, "", "Carregando clientes...");
        U.mostrarTabela(elementos.$tabela, false);

        try {
            clientes = await AdminApi.clientes.listar();
            renderClientes();
        } catch (error) {
            U.setEstado(elementos.$estado, "error", U.mensagemErro(error));
        }
    };

    var renderClientes = function () {
        var elementos = obterElementos();
        var termo = U.normalizar(elementos.$busca.val());
        var filtrados = clientes.filter(function (cliente) {
            return filtrarCliente(cliente, termo);
        });

        U.limpar(elementos.$corpo);

        if (clientes.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum cliente cadastrado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        if (filtrados.length === 0) {
            U.setEstado(elementos.$estado, "", "Nenhum cliente encontrado.");
            U.mostrarTabela(elementos.$tabela, false);
            return;
        }

        filtrados.forEach(function (cliente) {
            var $acoes = $("<td>").append(
                $("<div>", { class: "table-actions" }).append(
                    U.botao("Editar", "btn-secondary", function () {
                        abrirEdicao(cliente);
                    }),
                    U.botao("Excluir", "btn-danger", function () {
                        excluirCliente(cliente);
                    })
                )
            );

            $("<tr>")
                .append(
                    U.celula(cliente.id),
                    U.celula(cliente.nome),
                    U.celula(cliente.telefone),
                    U.celula(cliente.email),
                    U.celula(cliente.endereco),
                    $acoes
                )
                .appendTo(elementos.$corpo);
        });

        U.setEstado(elementos.$estado, "", "");
        U.mostrarTabela(elementos.$tabela, true);
    };

    var filtrarCliente = function (cliente, termo) {
        if (!termo || termo === "-") {
            return true;
        }

        return [
            cliente.id,
            cliente.nome,
            cliente.telefone,
            cliente.email,
            cliente.endereco
        ].some(function (valor) {
            return U.normalizar(valor).includes(termo);
        });
    };

    var abrirNovoCliente = function () {
        var elementos = obterElementos();

        elementos.$formTitulo.text("Novo cliente");
        elementos.$form[0].reset();
        elementos.$id.val("");
        elementos.$formPanel.prop("hidden", false);
        elementos.$nome.trigger("focus");
    };

    var abrirEdicao = function (cliente) {
        var elementos = obterElementos();

        elementos.$formTitulo.text("Editar cliente #" + cliente.id);
        elementos.$id.val(cliente.id);
        elementos.$nome.val(cliente.nome || "");
        elementos.$telefone.val(cliente.telefone || "");
        elementos.$email.val(cliente.email || "");
        elementos.$endereco.val(cliente.endereco || "");
        elementos.$formPanel.prop("hidden", false);
        elementos.$nome.trigger("focus");
    };

    var fecharFormulario = function () {
        var elementos = obterElementos();

        elementos.$formPanel.prop("hidden", true);
        elementos.$form[0].reset();
    };

    var salvarCliente = async function (event) {
        event.preventDefault();

        var elementos = obterElementos();
        var id = elementos.$id.val();
        var email = (elementos.$email.val() || "").trim();
        var cliente = {
            id: id ? Number(id) : null,
            nome: (elementos.$nome.val() || "").trim(),
            telefone: (elementos.$telefone.val() || "").trim(),
            endereco: (elementos.$endereco.val() || "").trim(),
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
    };

    var excluirCliente = async function (cliente) {
        if (!window.confirm("Excluir cliente " + cliente.nome + "?")) {
            return;
        }

        try {
            await AdminApi.clientes.excluir(cliente.id);
            U.toast("Cliente excluido.");
            await carregarClientes();
        } catch (error) {
            U.toast(U.mensagemErro(error), "error");
        }
    };

    var init = function () {
        bindEventos();
        carregarClientes();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        bindEventos: bindEventos,
        carregarClientes: carregarClientes,
        renderClientes: renderClientes,
        filtrarCliente: filtrarCliente,
        abrirNovoCliente: abrirNovoCliente,
        abrirEdicao: abrirEdicao,
        fecharFormulario: fecharFormulario,
        salvarCliente: salvarCliente,
        excluirCliente: excluirCliente
    };
}());
