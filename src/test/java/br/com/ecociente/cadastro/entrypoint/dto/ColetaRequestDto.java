package br.com.ecociente.cadastro.entrypoint.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import br.com.ecociente.cadastro.core.domain.StatusAgendamento;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarStatusColetaRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.request.CriarColetaRequestDto;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ColetaRequestDtoTest {

  private static ValidatorFactory factory;
  private static Validator validator;

  @BeforeAll
  static void configurar() {
    factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
  }

  @AfterAll
  static void encerrar() {
    factory.close();
  }

  @Test
  void deveAceitarCriacaoValida() {
    var inicio = OffsetDateTime.now().plusDays(2);

    var request = new CriarColetaRequestDto(
        1, 2, inicio, inicio.plusHours(1));

    assertTrue(validator.validate(request).isEmpty());
  }

  @Test
  void deveAceitarFimOmitido() {
    var request = new CriarColetaRequestDto(
        1, 2, OffsetDateTime.now().plusDays(2), null);

    assertTrue(validator.validate(request).isEmpty());
  }

  @Test
  void deveAceitarFimIgualAoInicio() {
    var inicio = OffsetDateTime.now().plusDays(2);
    var request = new CriarColetaRequestDto(1, 2, inicio, inicio);

    assertTrue(validator.validate(request).isEmpty());
  }

  @Test
  void deveRejeitarCamposObrigatoriosAusentes() {
    var request = new CriarColetaRequestDto(
        null, null, null, null);

    var campos = validator.validate(request).stream()
        .map(v -> v.getPropertyPath().toString())
        .toList();

    assertTrue(campos.contains("condominioId"));
    assertTrue(campos.contains("cooperativaId"));
    assertTrue(campos.contains("dataInicio"));
  }

  @Test
  void deveRejeitarIdsNaoPositivos() {
    var request = new CriarColetaRequestDto(
        0, -1, OffsetDateTime.now().plusDays(2), null);

    var campos = validator.validate(request).stream()
        .map(v -> v.getPropertyPath().toString())
        .toList();

    assertTrue(campos.contains("condominioId"));
    assertTrue(campos.contains("cooperativaId"));
  }

  @Test
  void deveRejeitarInicioNoPassado() {
    var request = new CriarColetaRequestDto(
        1, 2, OffsetDateTime.now().minusDays(1), null);

    assertFalse(validator.validate(request).isEmpty());
  }

  @Test
  void deveRejeitarFimAnteriorAoInicio() {
    var inicio = OffsetDateTime.now().plusDays(2);

    var request = new CriarColetaRequestDto(
        1, 2, inicio, inicio.minusHours(1));

    assertFalse(validator.validate(request).isEmpty());
  }

  @Test
  void deveExigirStatus() {
    var request = new AtualizarStatusColetaRequestDto(null);

    assertFalse(validator.validate(request).isEmpty());
  }

  @Test
  void deveAceitarStatusInformado() {
    var request = new AtualizarStatusColetaRequestDto(
        StatusAgendamento.CONFIRMADO);

    assertTrue(validator.validate(request).isEmpty());
  }
}