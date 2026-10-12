package br.com.ecociente.cadastro.dataprovaider.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.net.SocketTimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.exception.ConsultaDocumentoIndisponivelException;

class BrasilApiClientTest {

    private static final String CNPJ = "34238864000168";

    private static final String URL =
        "https://brasilapi.com.br/api/cnpj/v1/" + CNPJ;

    private BrasilApiClient client;
    private MockRestServiceServer servidor;

    @BeforeEach
    void configurar() {
        client = new BrasilApiClient();

        RestTemplate restTemplate = (RestTemplate)
            ReflectionTestUtils.getField(client, "restTemplate");

        servidor = MockRestServiceServer
            .bindTo(restTemplate)
            .build();
    }

    @AfterEach
    void verificarRequisicoes() {
        servidor.verify();
    }

    @Test
    void deveRetornarDadosQuandoConsultaTemSucesso() {
        servidor.expect(requestTo(URL))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                """
                {
                    "cnpj": "34238864000168",
                    "razao_social": "Empresa de teste"
                }
                """,
                MediaType.APPLICATION_JSON
            ));

        var resultado = client.consultarCnpj(CNPJ);

        assertEquals(CNPJ, resultado.get("cnpj"));
        assertEquals(
            "Empresa de teste",
            resultado.get("razao_social")
        );
    }

    @ParameterizedTest
    @CsvSource({
        "400, CNPJ_INVALID",
        "404, CNPJ_NOT_FOUND"
    })
    void deveTratarErroDoDocumento(
        int status,
        String codigoEsperado
    ) {
        servidor.expect(requestTo(URL))
            .andRespond(withStatus(HttpStatus.valueOf(status)));

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> client.consultarCnpj(CNPJ)
        );

        assertEquals(codigoEsperado, erro.getCodigoErro());
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 429, 500, 503})
    void deveTratarFalhaDoFornecedorComoIndisponibilidade(int status) {
        servidor.expect(requestTo(URL))
            .andRespond(withStatus(HttpStatus.valueOf(status)));

        assertThrows(
            ConsultaDocumentoIndisponivelException.class,
            () -> client.consultarCnpj(CNPJ)
        );
    }

    @Test
    void deveTratarTimeoutComoIndisponibilidade() {
        servidor.expect(requestTo(URL))
            .andRespond(requisicao -> {
                throw new SocketTimeoutException(
                    "Tempo de resposta esgotado"
                );
            });

        assertThrows(
            ConsultaDocumentoIndisponivelException.class,
            () -> client.consultarCnpj(CNPJ)
        );
    }

    @Test
    void deveRejeitarRespostaSemCorpo() {
        servidor.expect(requestTo(URL))
            .andRespond(withStatus(HttpStatus.OK));

        assertThrows(
            ConsultaDocumentoIndisponivelException.class,
            () -> client.consultarCnpj(CNPJ)
        );
    }

    @Test
    void deveRejeitarJsonMalformado() {
        servidor.expect(requestTo(URL))
            .andRespond(withSuccess(
                "{resposta-invalida",
                MediaType.APPLICATION_JSON
            ));

        assertThrows(
            ConsultaDocumentoIndisponivelException.class,
            () -> client.consultarCnpj(CNPJ)
        );
    }
}