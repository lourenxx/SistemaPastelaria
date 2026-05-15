package br.edu.faculdade.sistemapastelaria;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.edu.faculdade.sistemapastelaria.model.Cliente;
import br.edu.faculdade.sistemapastelaria.model.Produto;
import br.edu.faculdade.sistemapastelaria.repository.ClienteRepository;
import br.edu.faculdade.sistemapastelaria.repository.PedidoRepository;
import br.edu.faculdade.sistemapastelaria.repository.ProdutoRepository;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ClienteAccessIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparDados() {
        pedidoRepository.deleteAll();
        produtoRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void deveCadastrarClienteComSenhaCriptografada() throws Exception {
        mockMvc.perform(post("/clientes/cadastro")
                .contentType("application/json")
                .content("""
                        {
                          "nome": "Cliente",
                          "telefone": "11999999999",
                          "endereco": "Rua A",
                          "email": "CLIENTE@EMAIL.COM",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("cliente@email.com"));

        Cliente cliente = clienteRepository.findByEmail("cliente@email.com").orElseThrow();
        assertNotEquals("123456", cliente.getSenha());
        assertTrue(passwordEncoder.matches("123456", cliente.getSenha()));
    }

    @Test
    void deveAutenticarClienteRestringirAdminECriarPedidoParaProprioCliente() throws Exception {
        Produto produto = criarProdutoDisponivel();

        mockMvc.perform(post("/clientes/cadastro")
                .contentType("application/json")
                .content("""
                        {
                          "nome": "Cliente",
                          "telefone": "11999999999",
                          "endereco": "Rua A",
                          "email": "cliente@email.com",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk());

        Cliente cliente = clienteRepository.findByEmail("cliente@email.com").orElseThrow();
        Cliente outroCliente = criarOutroCliente();

        MvcResult loginResult = mockMvc.perform(post("/clientes/login")
                .contentType("application/json")
                .content("""
                        {
                          "email": "cliente@email.com",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("CLIENTE"))
                .andExpect(jsonPath("$.id").value(cliente.getId().intValue()))
                .andReturn();

        MockHttpSession sessaoCliente = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/Cliente/html/pedidos.html").session(sessaoCliente))
                .andExpect(status().isOk());

        mockMvc.perform(get("/produtos/cardapio").session(sessaoCliente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(produto.getId().intValue()));

        mockMvc.perform(post("/clientes/me/pedidos")
                .session(sessaoCliente)
                .contentType("application/json")
                .content("""
                        {
                          "clienteId": %d,
                          "formaPagamento": "PIX",
                          "observacao": "Sem cebola",
                          "itens": [
                            {
                              "quantidade": 2,
                              "produtoId": %d
                            }
                          ]
                        }
                        """.formatted(outroCliente.getId(), produto.getId())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clienteId").value(cliente.getId().intValue()))
                .andExpect(jsonPath("$.status").value("ABERTO"));

        mockMvc.perform(get("/Admin/html/dashboard.html").session(sessaoCliente))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/clientes").session(sessaoCliente))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/produtos").session(sessaoCliente))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/pedidos").session(sessaoCliente))
                .andExpect(status().isForbidden());
    }

    private Produto criarProdutoDisponivel() {
        Produto produto = new Produto();
        produto.setNome("PASTEL DE QUEIJO");
        produto.setDescricao("Pastel tradicional");
        produto.setCategoria("PASTEIS");
        produto.setPreco(new BigDecimal("12.50"));
        produto.setDisponivel(true);
        return produtoRepository.save(produto);
    }

    private Cliente criarOutroCliente() {
        Cliente cliente = new Cliente();
        cliente.setNome("OUTRO CLIENTE");
        cliente.setTelefone("11888888888");
        cliente.setEndereco("RUA B");
        cliente.setEmail("outro@email.com");
        return clienteRepository.save(cliente);
    }
}
