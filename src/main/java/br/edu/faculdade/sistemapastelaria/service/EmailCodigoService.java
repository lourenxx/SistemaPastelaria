package br.edu.faculdade.sistemapastelaria.service;

import java.io.Serializable;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import jakarta.servlet.http.HttpSession;

@Service
public class EmailCodigoService {

    private static final String CODIGO_SESSION_KEY = "LOGIN_EMAIL_CODIGO";
    private static final int MAX_TENTATIVAS = 5;
    private static final Duration VALIDADE_CODIGO = Duration.ofMinutes(10);

    private final Optional<JavaMailSender> mailSender;
    private final String remetente;
    private final SecureRandom secureRandom;
    private final Clock clock;

    @Autowired
    public EmailCodigoService(
            Optional<JavaMailSender> mailSender,
            @Value("${app.mail.from:no-reply@sistemapastelaria.local}") String remetente) {
        this(mailSender, remetente, Clock.systemUTC(), new SecureRandom());
    }

    EmailCodigoService(Optional<JavaMailSender> mailSender, String remetente, Clock clock, SecureRandom secureRandom) {
        this.mailSender = mailSender;
        this.remetente = remetente;
        this.clock = clock;
        this.secureRandom = secureRandom;
    }

    public void iniciarCodigo(HttpSession session, Long usuarioId, String email) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sessao invalida");
        }

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuario sem email cadastrado");
        }

        String codigo = gerarCodigo();
        enviarCodigo(email, codigo);

        CodigoPendente codigoPendente = new CodigoPendente(
                usuarioId,
                codigo,
                Instant.now(clock).plus(VALIDADE_CODIGO),
                0);

        session.setAttribute(CODIGO_SESSION_KEY, codigoPendente);
    }

    public Long validarCodigo(HttpSession session, String codigoInformado) {
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nenhum codigo pendente");
        }

        Object atributo = session.getAttribute(CODIGO_SESSION_KEY);
        if (!(atributo instanceof CodigoPendente codigoPendente)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nenhum codigo pendente");
        }

        if (Instant.now(clock).isAfter(codigoPendente.expiraEm())) {
            session.removeAttribute(CODIGO_SESSION_KEY);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Codigo expirado");
        }

        if (codigoInformado == null || !codigoPendente.codigo().equals(codigoInformado.trim())) {
            int tentativas = codigoPendente.tentativas() + 1;
            if (tentativas >= MAX_TENTATIVAS) {
                session.removeAttribute(CODIGO_SESSION_KEY);
            } else {
                session.setAttribute(CODIGO_SESSION_KEY, new CodigoPendente(
                        codigoPendente.usuarioId(),
                        codigoPendente.codigo(),
                        codigoPendente.expiraEm(),
                        tentativas));
            }

            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Codigo invalido");
        }

        session.removeAttribute(CODIGO_SESSION_KEY);
        return codigoPendente.usuarioId();
    }

    public String mascararEmail(String email) {
        if (email == null || email.isBlank()) {
            return "";
        }

        String[] partes = email.split("@", 2);
        if (partes.length != 2) {
            return email;
        }

        String usuario = partes[0];
        String dominio = partes[1];
        String primeiroCaractere = usuario.isEmpty() ? "*" : usuario.substring(0, 1);

        return primeiroCaractere + "***@" + dominio;
    }

    private String gerarCodigo() {
        return "%06d".formatted(secureRandom.nextInt(1_000_000));
    }

    private void enviarCodigo(String email, String codigo) {
        JavaMailSender sender = mailSender.orElseThrow(() -> new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Envio de email nao configurado"));

        SimpleMailMessage mensagem = new SimpleMailMessage();
        mensagem.setFrom(remetente);
        mensagem.setTo(email);
        mensagem.setSubject("Codigo de acesso - Sistema Pastelaria");
        mensagem.setText("Seu codigo de acesso e: " + codigo + "\nEle expira em 10 minutos.");

        sender.send(mensagem);
    }

    private record CodigoPendente(Long usuarioId, String codigo, Instant expiraEm, int tentativas)
            implements Serializable {
    }
}
