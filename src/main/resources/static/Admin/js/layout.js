var AdminLayout = (function () {
    var paginas = [
        { id: "dashboard", href: "/Admin/html/dashboard.html", rotulo: "Dashboard", icon: "IN" },
        { id: "produtos", href: "/Admin/html/produtos.html", rotulo: "Produtos", icon: "PR" },
        { id: "clientes", href: "/Admin/html/clientes.html", rotulo: "Clientes", icon: "CL" },
        { id: "pedidos", href: "/Admin/html/pedidos.html", rotulo: "Pedidos", icon: "PE" }
    ];

    var obterElementos = function () {
        return {
            $sidebar: $("#adminSidebar"),
            $header: $("#adminHeader"),
            $body: $("body")
        };
    };

    var montarLayout = function () {
        var elementos = obterElementos();

        if (!elementos.$sidebar.length || !elementos.$header.length) {
            return;
        }

        var paginaAtual = elementos.$body.data("page");
        var titulo = elementos.$body.data("title") || "Administracao";
        var subtitulo = elementos.$body.data("subtitle") || "Sistema Pastelaria";

        elementos.$sidebar.append(criarMarca(), criarNavegacao(paginaAtual));
        elementos.$header.append(criarBotaoMenu(), criarTitulo(titulo, subtitulo), criarAcoesHeader());

        bindCliqueForaMenu(elementos.$sidebar, elementos.$body);
    };

    var bindCliqueForaMenu = function ($sidebar, $body) {
        $(document).on("click", function (event) {
            if (window.innerWidth > 980 || !$body.hasClass("nav-open")) {
                return;
            }

            var clicouSidebar = $sidebar[0].contains(event.target);
            var clicouMenu = $(event.target).closest("#menuButton").length > 0;

            if (!clicouSidebar && !clicouMenu) {
                $body.removeClass("nav-open");
            }
        });
    };

    var criarMarca = function () {
        return $("<div>", { class: "brand" }).append(
            $("<strong>").text("Sistema Pastelaria"),
            $("<span>").text("Area administrativa")
        );
    };

    var criarNavegacao = function (paginaAtual) {
        var $nav = $("<nav>", {
            class: "admin-nav",
            "aria-label": "Navegacao principal"
        });

        paginas.forEach(function (pagina) {
            $("<a>", {
                href: pagina.href,
                class: pagina.id === paginaAtual ? "nav-link active" : "nav-link"
            })
                .append(
                    $("<span>", {
                        class: "nav-icon",
                        "aria-hidden": "true"
                    }).text(pagina.icon),
                    $("<span>").text(pagina.rotulo)
                )
                .appendTo($nav);
        });

        return $nav;
    };

    var criarBotaoMenu = function () {
        return $("<button>", {
            type: "button",
            id: "menuButton",
            class: "menu-button",
            "aria-label": "Abrir menu"
        })
            .text("Menu")
            .on("click", function () {
                obterElementos().$body.toggleClass("nav-open");
            });
    };

    var criarTitulo = function (titulo, subtitulo) {
        return $("<div>", { class: "header-title" }).append(
            $("<h1>").text(titulo),
            $("<p>").text(subtitulo)
        );
    };

    var criarAcoesHeader = function () {
        var elementos = obterElementos();
        var label = elementos.$body.data("actionLabel");
        var target = elementos.$body.data("actionTarget");
        var $wrapper = $("<div>", { class: "header-actions" });

        if (label && target) {
            $("<button>", {
                id: target,
                type: "button",
                class: "btn btn-primary"
            })
                .text(label)
                .appendTo($wrapper);
        }

        $("<button>", {
            type: "button",
            class: "btn btn-secondary"
        })
            .text("Sair")
            .on("click", sairDoSistema)
            .appendTo($wrapper);

        return $wrapper;
    };

    var sairDoSistema = async function () {
        try {
            await AdminApi.sessao.logout();
        } finally {
            window.location.href = "/index.html";
        }
    };

    var init = function () {
        montarLayout();
    };

    return {
        init: init,
        obterElementos: obterElementos,
        montarLayout: montarLayout,
        bindCliqueForaMenu: bindCliqueForaMenu,
        criarMarca: criarMarca,
        criarNavegacao: criarNavegacao,
        criarBotaoMenu: criarBotaoMenu,
        criarTitulo: criarTitulo,
        criarAcoesHeader: criarAcoesHeader,
        sairDoSistema: sairDoSistema
    };
}());
