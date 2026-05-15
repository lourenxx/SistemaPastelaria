(function () {
    document.addEventListener("DOMContentLoaded", () => {
        const clienteButton = document.getElementById("clienteButton");
        const clienteOptions = document.getElementById("clienteOptions");

        clienteButton.addEventListener("click", () => {
            clienteOptions.hidden = !clienteOptions.hidden;
        });
    });
})();
