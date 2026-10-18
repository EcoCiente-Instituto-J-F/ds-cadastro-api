package br.com.ecociente.cadastro.entrypoint.mapper;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;

import br.com.ecociente.cadastro.core.domain.Coleta;
import br.com.ecociente.cadastro.core.domain.StatusAgendamento;
import br.com.ecociente.cadastro.entrypoint.dto.request.CriarColetaRequestDto;

class ColetaMapperTest {

  private final ColetaMapper mapper = new ColetaMapper();

  @Test
  void deveConverterRequestPreservandoPeriodo() {
    var inicio = OffsetDateTime.now().plusDays(2);
    var fim = inicio.plusHours(1);

    var entrada = mapper.toDomain(
        new CriarColetaRequestDto(1, 2, inicio, fim));

    assertEquals(1, entrada.condominioId());
    assertEquals(2, entrada.cooperativaId());
    assertEquals(inicio, entrada.dataInicio());
    assertEquals(fim, entrada.dataFim());
  }

  @Test
  void deveConverterTodosOsCamposDaResposta() {
    var inicio = OffsetDateTime.now().plusDays(2);
    var fim = inicio.plusHours(1);
    var respondidoEm = OffsetDateTime.now();

    var coleta = new Coleta(
        100, 1, 2,
        StatusAgendamento.CONFIRMADO,
        inicio, fim, false,
        10, "SINDICO", 20, respondidoEm);

    var resposta = mapper.toResponse(coleta);

    assertEquals(100, resposta.id());
    assertEquals(1, resposta.condominioId());
    assertEquals(2, resposta.cooperativaId());
    assertEquals(StatusAgendamento.CONFIRMADO, resposta.status());
    assertEquals(inicio, resposta.dataInicio());
    assertEquals(fim, resposta.dataFim());
    assertFalse(resposta.possuiRecorrencia());
    assertEquals(10, resposta.solicitadoPorUsuarioId());
    assertEquals("SINDICO", resposta.origemSolicitacao());
    assertEquals(20, resposta.respondidoPorUsuarioId());
    assertEquals(respondidoEm, resposta.respondidoEm());
  }

  @Test
  void devePreservarCamposOpcionaisNulos() {
    var coleta = new Coleta(
        100, 1, 2,
        StatusAgendamento.AGENDADO,
        OffsetDateTime.now().plusDays(2),
        null, false,
        10, "SINDICO", null, null);

    var resposta = mapper.toResponse(coleta);

    assertNull(resposta.dataFim());
    assertNull(resposta.respondidoPorUsuarioId());
    assertNull(resposta.respondidoEm());
  }
}