package br.com.ecociente.cadastro.config.security;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

class JwtServiceTest {

    private static final String SECRET =
        "segredo-exclusivo-dos-testes-com-mais-de-32-caracteres";

    private final SecretKey key = Keys.hmacShaKeyFor(
        SECRET.getBytes(StandardCharsets.UTF_8)
    );

    private final JwtService service = new JwtService(SECRET);

    private String token(Object usuarioId, Instant expiracao) {
        var builder = Jwts.builder().subject("usuario@teste.com");

        if (usuarioId != null) {
            builder.claim("usuarioId", usuarioId);
        }

        if (expiracao != null) {
            builder.expiration(Date.from(expiracao));
        }

        return builder.signWith(key).compact();
    }

    @Test
    void deveExtrairIdDeTokenValido() {
        String token = token(10, Instant.now().plusSeconds(300));

        assertEquals(10, service.extrairUsuarioId(token));
    }

    @Test
    void deveAceitarMaiorIdInteiroPermitido() {
        String token = token(
            Integer.MAX_VALUE,
            Instant.now().plusSeconds(300)
        );

        assertEquals(
            Integer.MAX_VALUE,
            service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarTokenExpirado() {
        String token = token(10, Instant.now().minusSeconds(60));

        assertThrows(
            JwtException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarTokenSemExpiracao() {
        String token = token(10, null);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarTokenSemUsuarioId() {
        String token = token(null, Instant.now().plusSeconds(300));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarUsuarioIdComoTexto() {
        String token = token("10", Instant.now().plusSeconds(300));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @ParameterizedTest
    @ValueSource(longs = {-1, 0, 2147483648L})
    void deveRejeitarIdForaDaFaixa(long usuarioId) {
        String token = token(usuarioId, Instant.now().plusSeconds(300));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @ParameterizedTest
    @ValueSource(doubles = {1.5, 10.9})
    void deveRejeitarIdFracionario(double usuarioId) {
        String token = token(usuarioId, Instant.now().plusSeconds(300));

        assertThrows(
            IllegalArgumentException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarAssinaturaDeOutroSegredo() {
        SecretKey outraChave = Keys.hmacShaKeyFor(
            "outro-segredo-de-teste-com-mais-de-32-caracteres"
                .getBytes(StandardCharsets.UTF_8)
        );

        String token = Jwts.builder()
            .claim("usuarioId", 10)
            .expiration(Date.from(Instant.now().plusSeconds(300)))
            .signWith(outraChave)
            .compact();

        assertThrows(
            JwtException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarTokenSemAssinatura() {
        String token = Jwts.builder()
            .claim("usuarioId", 10)
            .expiration(Date.from(Instant.now().plusSeconds(300)))
            .compact();

        assertThrows(
            JwtException.class,
            () -> service.extrairUsuarioId(token)
        );
    }

    @Test
    void deveRejeitarTokenMalformado() {
        assertThrows(
            JwtException.class,
            () -> service.extrairUsuarioId("isso-nao-e-jwt")
        );
    }
}