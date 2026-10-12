package br.com.ecociente.cadastro.dataprovaider.gateway;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ecociente.cadastro.core.exception.ConsultaDocumentoIndisponivelException;
import br.com.ecociente.cadastro.dataprovaider.client.BrasilApiClient;

@ExtendWith(MockitoExtension.class)
class ConsultaDocumentoGatewayImplTest {

    private static final String CNPJ = "34238864000168";

    @Mock
    private BrasilApiClient client;

    @InjectMocks
    private ConsultaDocumentoGatewayImpl gateway;

    @Test
    void deveAceitarDocumentoCorrespondente() {
        when(client.consultarCnpj(CNPJ)).thenReturn(Map.of(
            "cnpj", CNPJ,
            "razao_social", "Empresa de teste"
        ));

        assertDoesNotThrow(() -> gateway.validarCnpj(CNPJ));

        verify(client).consultarCnpj(CNPJ);
    }

    @Test
    void deveAceitarDocumentoRetornadoComPontuacao() {
        when(client.consultarCnpj(CNPJ)).thenReturn(Map.of(
            "cnpj", "34.238.864/0001-68",
            "razao_social", "Empresa de teste"
        ));

        assertDoesNotThrow(() -> gateway.validarCnpj(CNPJ));
    }

    @ParameterizedTest
    @MethodSource("respostasInconsistentes")
    void deveRejeitarRespostaInconsistente(Map<String, Object> dados) {
        when(client.consultarCnpj(CNPJ)).thenReturn(dados);

        assertThrows(
            ConsultaDocumentoIndisponivelException.class,
            () -> gateway.validarCnpj(CNPJ)
        );
    }

    static Stream<Map<String, Object>> respostasInconsistentes() {
        return Stream.of(
            Map.<String, Object>of(),
            Map.<String, Object>of("cnpj", CNPJ),
            Map.<String, Object>of(
                "razao_social", "Empresa de teste"
            ),
            Map.<String, Object>of(
                "cnpj", "54934758000126",
                "razao_social", "Outra empresa"
            ),
            Map.<String, Object>of(
                "cnpj", CNPJ,
                "razao_social", " "
            ),
            Map.<String, Object>of(
                "cnpj", CNPJ,
                "razao_social", 123
            )
        );
    }
}