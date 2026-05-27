package br.edu.faculdade.sistemapastelaria;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import br.edu.faculdade.sistemapastelaria.dto.UsuarioDTO;
import br.edu.faculdade.sistemapastelaria.repository.UsuarioRepository;
import br.edu.faculdade.sistemapastelaria.service.UsuarioService;
import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioLogin2faIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CapturingMailSender mailSender;

    @BeforeEach
    void limparDados() {
        usuarioRepository.deleteAll();
        mailSender.clear();
    }

    @Test
    void deveExigirCodigoDeEmailAntesDeLiberarPaginaProtegida() throws Exception {
        mockMvc.perform(get("/vendor/jquery/jquery-4.0.0.slim.min.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("jQuery")));

        mockMvc.perform(get("/Admin/html/dashboard.html"))
                .andExpect(status().isUnauthorized());

        UsuarioDTO usuario = usuarioService.salvarUsuario(new UsuarioDTO(
                null,
                "Administrador",
                "admin",
                "ADMIN@EMAIL.COM",
                "123456"));
        assertEquals("admin@email.com", usuario.getEmail());

        MvcResult loginResult = mockMvc.perform(post("/usuarios/login")
                .contentType("application/json")
                .content("""
                        {
                          "login": "admin",
                          "senha": "123456"
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoEnviado").value(true))
                .andExpect(jsonPath("$.emailMascarado").value("a***@email.com"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) loginResult.getRequest().getSession(false);

        mockMvc.perform(get("/Admin/html/dashboard.html").session(session))
                .andExpect(status().isUnauthorized());

        MvcResult verificacaoResult = mockMvc.perform(post("/usuarios/login/verificar-codigo")
                .session(session)
                .contentType("application/json")
                .content("""
                        {
                          "codigo": "%s"
                        }
                        """.formatted(mailSender.ultimoCodigo())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("admin"))
                .andExpect(jsonPath("$.email").value("admin@email.com"))
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andReturn();

        MockHttpSession sessaoAutenticada = (MockHttpSession) verificacaoResult.getRequest().getSession(false);

        mockMvc.perform(get("/Admin/html/dashboard.html").session(sessaoAutenticada))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Dashboard - Sistema Pastelaria")));

        mockMvc.perform(get("/produtos").session(sessaoAutenticada))
                .andExpect(status().isOk());

        mockMvc.perform(get("/clientes").session(sessaoAutenticada))
                .andExpect(status().isOk());

        mockMvc.perform(get("/pedidos").session(sessaoAutenticada))
                .andExpect(status().isOk());

        mockMvc.perform(get("/Cliente/html/pedidos.html").session(sessaoAutenticada))
                .andExpect(status().isForbidden());
    }

    @TestConfiguration
    static class MailTestConfiguration {

        @Bean
        CapturingMailSender capturingMailSender() {
            return new CapturingMailSender();
        }
    }

    static class CapturingMailSender implements JavaMailSender {
        private final List<SimpleMailMessage> mensagens = new ArrayList<>();

        void clear() {
            mensagens.clear();
        }

        String ultimoCodigo() {
            SimpleMailMessage mensagem = mensagens.get(mensagens.size() - 1);
            Matcher matcher = Pattern.compile("\\b\\d{6}\\b").matcher(mensagem.getText());
            if (!matcher.find()) {
                throw new IllegalStateException("Codigo de email nao encontrado");
            }

            return matcher.group();
        }

        @Override
        public MimeMessage createMimeMessage() {
            throw new UnsupportedOperationException();
        }

        @Override
        public MimeMessage createMimeMessage(InputStream contentStream) throws MailException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(MimeMessage mimeMessage) throws MailException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(MimeMessage... mimeMessages) throws MailException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(MimeMessagePreparator mimeMessagePreparator) throws MailException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(MimeMessagePreparator... mimeMessagePreparators) throws MailException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void send(SimpleMailMessage simpleMessage) throws MailException {
            mensagens.add(new SimpleMailMessage(simpleMessage));
        }

        @Override
        public void send(SimpleMailMessage... simpleMessages) throws MailException {
            for (SimpleMailMessage simpleMessage : simpleMessages) {
                send(simpleMessage);
            }
        }
    }
}
