package br.edu.faculdade.sistemapastelaria;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import br.edu.faculdade.sistemapastelaria.model.Usuario;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioSwaggerCadastroIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void limparDados() {
        usuarioRepository.deleteAll();
    }

    @Test
    void devePermitirCriarPrimeiroUsuarioSemAutenticacao() throws Exception {
        mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("""
                        {
                          "nome": "Administrador",
                          "login": "ADMIN",
                          "email": "ADMIN@EMAIL.COM",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        Usuario usuario = usuarioRepository.findByLogin("admin").orElseThrow();
        assertTrue(usuario.isAtivo());
        assertTrue(passwordEncoder.matches("123456", usuario.getSenha()));
    }

    @Test
    void deveBloquearCriacaoAnonimaDepoisDoPrimeiroUsuario() throws Exception {
        criarPrimeiroUsuario();

        mockMvc.perform(post("/usuarios")
                .contentType("application/json")
                .content("""
                        {
                          "nome": "Outro",
                          "login": "outro",
                          "email": "outro@email.com",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isForbidden());
    }

    @Test
    void devePermitirCriacaoAutenticadaDepoisDoPrimeiroUsuario() throws Exception {
        criarPrimeiroUsuario();

        mockMvc.perform(post("/usuarios")
                .with(user("admin").roles("INTERNO"))
                .contentType("application/json")
                .content("""
                        {
                          "nome": "Outro",
                          "login": "outro",
                          "email": "outro@email.com",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("outro"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    private void criarPrimeiroUsuario() {
        Usuario usuario = new Usuario();
        usuario.setNome("ADMINISTRADOR");
        usuario.setLogin("admin");
        usuario.setEmail("admin@email.com");
        usuario.setSenha(passwordEncoder.encode("123456"));
        usuario.setAtivo(true);
        usuarioRepository.save(usuario);
    }
}
