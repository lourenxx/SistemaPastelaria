(function () {
    const paginas = [
        { id: "dashboard", href: "/Admin/html/dashboard.html", rotulo: "Dashboard", icon: "Inicio" },
        { id: "produtos", href: "/Admin/html/produtos.html", rotulo: "Produtos", icon: "Prod" },
        { id: "clientes", href: "/Admin/html/clientes.html", rotulo: "Clientes", icon: "Cli" },
        { id: "pedidos", href: "/Admin/html/pedidos.html", rotulo: "Pedidos", icon: "Ped" }
    ];

    document.addEventListener("DOMContentLoaded", () => {
        const sidebar = document.getElementById("adminSidebar");
        const header = document.getElementById("adminHeader");

        if (!sidebar || !header) {
            return;
        }

        const paginaAtual = document.body.dataset.page;
        const titulo = document.body.dataset.title || "Administracao";
        const subtitulo = document.body.dataset.subtitle || "Sistema Pastelaria";

        sidebar.appendChild(criarMarca());
        sidebar.appendChild(criarNavegacao(paginaAtual));
        header.appendChild(criarBotaoMenu());
        header.appendChild(criarTitulo(titulo, subtitulo));
        header.appendChild(criarAcoesHeader());

        document.addEventListener("click", (event) => {
            if (window.innerWidth > 980 || !document.body.classList.contains("nav-open")) {
                return;
            }

            const clicouSidebar = sidebar.contains(event.target);
            const clicouMenu = event.target.closest("#menuButton");

            if (!clicouSidebar && !clicouMenu) {
                document.body.classList.remove("nav-open");
            }
        });
    });

    function criarMarca() {
        const brand = document.createElement("div");
        brand.className = "brand";

        const nome = document.createElement("strong");
        nome.textContent = "Sistema Pastelaria";

        const area = document.createElement("span");
        area.textContent = "Area administrativa";

        brand.append(nome, area);
        return brand;
    }

    function criarNavegacao(paginaAtual) {
        const nav = document.createElement("nav");
        nav.className = "admin-nav";
        nav.setAttribute("aria-label", "Navegacao principal");

        paginas.forEach((pagina) => {
            const link = document.createElement("a");
            link.href = pagina.href;
            link.className = pagina.id === paginaAtual ? "nav-link active" : "nav-link";

            const icon = document.createElement("span");
            icon.className = "nav-icon";
            icon.textContent = pagina.icon;

            const label = document.createElement("span");
            label.textContent = pagina.rotulo;

            link.append(icon, label);
            nav.appendChild(link);
        });

        return nav;
    }

    function criarBotaoMenu() {
        const button = document.createElement("button");
        button.type = "button";
        button.id = "menuButton";
        button.className = "menu-button";
        button.setAttribute("aria-label", "Abrir menu");
        button.textContent = "Menu";
        button.addEventListener("click", () => document.body.classList.toggle("nav-open"));
        return button;
    }

    function criarTitulo(titulo, subtitulo) {
        const wrapper = document.createElement("div");
        wrapper.className = "header-title";

        const h1 = document.createElement("h1");
        h1.textContent = titulo;

        const p = document.createElement("p");
        p.textContent = subtitulo;

        wrapper.append(h1, p);
        return wrapper;
    }

    function criarAcoesHeader() {
        const wrapper = document.createElement("div");
        const label = document.body.dataset.actionLabel;
        const target = document.body.dataset.actionTarget;

        wrapper.className = "header-actions";

        if (label && target) {
            const button = document.createElement("button");
            button.id = target;
            button.type = "button";
            button.className = "btn btn-primary";
            button.textContent = label;
            wrapper.appendChild(button);
        }

        return wrapper;
    }
})();
