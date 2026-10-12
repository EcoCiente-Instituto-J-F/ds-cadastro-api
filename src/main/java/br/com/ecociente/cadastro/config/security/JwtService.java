package br.com.ecociente.cadastro.config.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(
            @Value("${app.security.jwt.secret}") String secret) {
        this.secretKey = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8));
    }

    public Integer extrairUsuarioId(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration() == null
                || !claims.getExpiration().after(new Date())) {
            throw new IllegalArgumentException("Token expirado");
        }

        Object valor = claims.get("usuarioId");

        if (!(valor instanceof Number numero)) {
            throw new IllegalArgumentException("Token sem usuário");
        }

        long id;

        try {
            id = new java.math.BigDecimal(numero.toString())
                    .longValueExact();
        } catch (ArithmeticException | NumberFormatException ex) {
            throw new IllegalArgumentException("Usuário inválido", ex);
        }

        if (id <= 0 || id > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Usuário inválido");
        }

        return Math.toIntExact(id);
    }
}