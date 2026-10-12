package br.com.ecociente.cadastro.config.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain chain;

    private JwtAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();

        filter = new JwtAuthenticationFilter(jwtService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void deveAutenticarComIdDoTokenEContinuarCadeia() throws Exception {
        request.addHeader("Authorization", "Bearer token-valido");

        when(jwtService.extrairUsuarioId("token-valido")).thenReturn(10);

        filter.doFilter(request, response, chain);

        var authentication = SecurityContextHolder.getContext()
            .getAuthentication();

        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        assertEquals(10, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertTrue(authentication.getAuthorities().isEmpty());

        verify(chain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticarQuandoHeaderEstaAusente()
        throws Exception {

        filter.doFilter(request, response, chain);

        assertNull(
            SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService);
        verify(chain).doFilter(request, response);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Basic abc", "token", "Bearer", "bearer token"
    })
    void deveIgnorarHeaderSemPrefixoEsperado(String header)
        throws Exception {

        request.addHeader("Authorization", header);

        filter.doFilter(request, response, chain);

        assertNull(
            SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService);
        verify(chain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticarQuandoJwtEstaMalformado()
        throws Exception {

        request.addHeader("Authorization", "Bearer invalido");

        when(jwtService.extrairUsuarioId("invalido"))
            .thenThrow(new MalformedJwtException("Token inválido"));

        filter.doFilter(request, response, chain);

        assertNull(
            SecurityContextHolder.getContext().getAuthentication()
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    void deveContinuarSemAutenticarQuandoClaimEstaInvalida()
        throws Exception {

        request.addHeader("Authorization", "Bearer invalido");

        when(jwtService.extrairUsuarioId("invalido"))
            .thenThrow(new IllegalArgumentException("Usuário inválido"));

        filter.doFilter(request, response, chain);

        assertNull(
            SecurityContextHolder.getContext().getAuthentication()
        );

        verify(chain).doFilter(request, response);
    }

    @Test
    void devePreservarAutenticacaoJaExistente() throws Exception {
        var anterior = new UsernamePasswordAuthenticationToken(
            20, null, List.of()
        );

        SecurityContextHolder.getContext()
            .setAuthentication(anterior);

        request.addHeader("Authorization", "Bearer outro-token");

        filter.doFilter(request, response, chain);

        assertSame(
            anterior,
            SecurityContextHolder.getContext().getAuthentication()
        );

        verifyNoInteractions(jwtService);
        verify(chain).doFilter(request, response);
    }
}