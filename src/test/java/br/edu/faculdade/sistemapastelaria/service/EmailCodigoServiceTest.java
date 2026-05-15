package br.edu.faculdade.sistemapastelaria.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.web.server.ResponseStatusException;

class EmailCodigoServiceTest {

    @Test
    void deveEnviarCodigoDeSeisDigitosEValidar() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MockHttpSession session = new MockHttpSession();
        EmailCodigoService service = service(mailSender, new MutableClock());

        service.iniciarCodigo(session, 10L, "usuario@email.com");

        String codigo = capturarCodigoEnviado(mailSender);
        assertEquals(10L, service.validarCodigo(session, codigo));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarCodigo(session, codigo));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    @Test
    void deveExpirarCodigoAposDezMinutos() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MockHttpSession session = new MockHttpSession();
        MutableClock clock = new MutableClock();
        EmailCodigoService service = service(mailSender, clock);

        service.iniciarCodigo(session, 20L, "usuario@email.com");
        String codigo = capturarCodigoEnviado(mailSender);
        clock.advance(Duration.ofMinutes(11));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarCodigo(session, codigo));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        assertEquals("Codigo expirado", exception.getReason());
    }

    @Test
    void deveLimparCodigoDepoisDeCincoTentativasInvalidas() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        MockHttpSession session = new MockHttpSession();
        EmailCodigoService service = service(mailSender, new MutableClock());

        service.iniciarCodigo(session, 30L, "usuario@email.com");
        String codigo = capturarCodigoEnviado(mailSender);

        for (int i = 0; i < 5; i++) {
            ResponseStatusException exception = assertThrows(
                    ResponseStatusException.class,
                    () -> service.validarCodigo(session, "000000"));
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
        }

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.validarCodigo(session, codigo));
        assertEquals("Nenhum codigo pendente", exception.getReason());
    }

    private EmailCodigoService service(JavaMailSender mailSender, Clock clock) {
        return new EmailCodigoService(
                Optional.of(mailSender),
                "no-reply@sistemapastelaria.local",
                clock,
                new SecureRandom());
    }

    private String capturarCodigoEnviado(JavaMailSender mailSender) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String texto = captor.getValue().getText();
        Matcher matcher = Pattern.compile("\\b\\d{6}\\b").matcher(texto);
        assertTrue(matcher.find());
        return matcher.group();
    }

    private static class MutableClock extends Clock {
        private Instant instant = Instant.parse("2026-05-12T12:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneId.of("UTC");
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        void advance(Duration duration) {
            instant = instant.plus(duration);
        }
    }
}
