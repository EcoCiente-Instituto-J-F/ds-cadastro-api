package br.com.ecociente.cadastro.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import br.com.ecociente.cadastro.core.Service.ColetaService;
import br.com.ecociente.cadastro.core.domain.Coleta;
import br.com.ecociente.cadastro.core.domain.NovaColeta;
import br.com.ecociente.cadastro.core.domain.StatusAgendamento;
import br.com.ecociente.cadastro.core.exception.AtualizacaoNaoPermitidaException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.exception.NotFoundException;
import br.com.ecociente.cadastro.core.gateway.ColetaGateway;

class ColetaServiceTest {

  private static final int SINDICO = 10;
  private static final int COOPERATIVA = 20;
  private static final int TERCEIRO = 30;

  private static final int CONDOMINIO_ID = 1;
  private static final int COOPERATIVA_ID = 2;
  private static final int COLETA_ID = 100;

  private ColetaGateway gateway;
  private ColetaService service;

  @BeforeEach
  void configurar() {
    gateway = mock(ColetaGateway.class);
    service = new ColetaService(gateway);
  }

  @Test
  void deveCriarSolicitacaoPeloSindico() {
    NovaColeta entrada = novaColeta();
    prepararCriacao(SINDICO, true, false);

    Coleta salva = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "SINDICO");

    when(gateway.criar(SINDICO, "SINDICO", entrada))
        .thenReturn(salva);

    Coleta resultado = service.criar(SINDICO, entrada);

    assertSame(salva, resultado);
    assertFalse(resultado.possuiRecorrencia());
    assertEquals(SINDICO, resultado.solicitadoPorUsuarioId());
    assertNull(resultado.respondidoPorUsuarioId());
    assertNull(resultado.respondidoEm());

    verify(gateway).criar(SINDICO, "SINDICO", entrada);
  }

  @Test
  void deveCriarSolicitacaoPelaCooperativa() {
    NovaColeta entrada = novaColeta();
    prepararCriacao(COOPERATIVA, false, true);

    Coleta salva = coleta(
        StatusAgendamento.AGENDADO,
        COOPERATIVA,
        "COOPERATIVA");

    when(gateway.criar(COOPERATIVA, "COOPERATIVA", entrada))
        .thenReturn(salva);

    assertSame(salva, service.criar(COOPERATIVA, entrada));

    verify(gateway).criar(
        COOPERATIVA, "COOPERATIVA", entrada);
  }

  @Test
  void naoDeveCriarSemRepresentarUmaDasPartes() {
    prepararCriacao(TERCEIRO, false, false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.criar(TERCEIRO, novaColeta()));

    verificarQueNaoCriou();
  }

  @Test
  void naoDeveCriarRepresentandoAsDuasPartes() {
    prepararCriacao(SINDICO, true, true);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.criar(SINDICO, novaColeta()));

    verificarQueNaoCriou();
  }

  @Test
  void naoDeveCriarComUsuarioInativo() {
    when(gateway.usuarioAtivo(SINDICO)).thenReturn(false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.criar(SINDICO, novaColeta()));

    verificarQueNaoCriou();
  }

  @Test
  void naoDeveCriarSemUsuario() {
    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.criar(null, novaColeta()));

    verifyNoInteractions(gateway);
  }

  @Test
  void naoDeveCriarComCondominioInativoOuInexistente() {
    when(gateway.usuarioAtivo(SINDICO)).thenReturn(true);
    when(gateway.condominioAtivo(CONDOMINIO_ID))
        .thenReturn(false);

    assertThrows(
        NotFoundException.class,
        () -> service.criar(SINDICO, novaColeta()));

    verificarQueNaoCriou();
  }

  @Test
  void naoDeveCriarComCooperativaInativaOuInexistente() {
    when(gateway.usuarioAtivo(SINDICO)).thenReturn(true);
    when(gateway.condominioAtivo(CONDOMINIO_ID))
        .thenReturn(true);
    when(gateway.cooperativaAtiva(COOPERATIVA_ID))
        .thenReturn(false);

    assertThrows(
        NotFoundException.class,
        () -> service.criar(SINDICO, novaColeta()));

    verificarQueNaoCriou();
  }

  @Test
  void naoDeveCriarComInicioNoPassado() {
    when(gateway.usuarioAtivo(SINDICO)).thenReturn(true);

    NovaColeta entrada = new NovaColeta(
        CONDOMINIO_ID,
        COOPERATIVA_ID,
        OffsetDateTime.now().minusDays(1),
        null);

    assertThrows(
        BusinessException.class,
        () -> service.criar(SINDICO, entrada));

    verificarQueNaoCriou();
  }

  @Test
  void naoDeveCriarComFimAnteriorAoInicio() {
    when(gateway.usuarioAtivo(SINDICO)).thenReturn(true);

    OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);

    NovaColeta entrada = new NovaColeta(
        CONDOMINIO_ID,
        COOPERATIVA_ID,
        inicio,
        inicio.minusHours(1));

    assertThrows(
        BusinessException.class,
        () -> service.criar(SINDICO, entrada));

    verificarQueNaoCriou();
  }

  @Test
  void devePermitirCriacaoSemDataFim() {
    prepararCriacao(SINDICO, true, false);

    NovaColeta entrada = new NovaColeta(
        CONDOMINIO_ID,
        COOPERATIVA_ID,
        OffsetDateTime.now().plusDays(2),
        null);

    service.criar(SINDICO, entrada);

    verify(gateway).criar(SINDICO, "SINDICO", entrada);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusAgendamento.class,
      names = {"CONFIRMADO", "RECUSADO", "CANCELADO"})
  void cooperativaDeveResponderSolicitacaoDoSindico(
      StatusAgendamento novoStatus) {

    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "SINDICO");

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    service.atualizarStatus(
        COOPERATIVA, COLETA_ID, novoStatus);

    verify(gateway).atualizarStatus(
        COLETA_ID, novoStatus, COOPERATIVA);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusAgendamento.class,
      names = {"CONFIRMADO", "RECUSADO", "CANCELADO"})
  void sindicoDeveResponderSolicitacaoDaCooperativa(
      StatusAgendamento novoStatus) {

    Coleta atual = coleta(
        StatusAgendamento.AGENDADO,
        COOPERATIVA,
        "COOPERATIVA");

    prepararAtualizacao(SINDICO, atual, true, false);

    service.atualizarStatus(SINDICO, COLETA_ID, novoStatus);

    verify(gateway).atualizarStatus(
        COLETA_ID, novoStatus, SINDICO);
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusAgendamento.class,
      names = {"CONFIRMADO", "RECUSADO", "CANCELADO"})
  void solicitanteNaoDeveResponderOuCancelar(
      StatusAgendamento novoStatus) {

    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "SINDICO");

    prepararAtualizacao(SINDICO, atual, true, false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.atualizarStatus(
            SINDICO, COLETA_ID, novoStatus));

    verificarQueNaoAtualizou();
  }

  @Test
  void outroSindicoNaoDeveResponderPeloLadoSolicitante() {
    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "SINDICO");

    prepararAtualizacao(TERCEIRO, atual, true, false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.atualizarStatus(
            TERCEIRO, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void usuarioSemVinculoNaoDeveAlterarStatus() {
    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "SINDICO");

    prepararAtualizacao(TERCEIRO, atual, false, false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.atualizarStatus(
            TERCEIRO, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void cooperativaDeveConcluirColetaConfirmada() {
    Coleta atual = confirmada(
        SINDICO, "SINDICO", COOPERATIVA);

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    service.atualizarStatus(
        COOPERATIVA, COLETA_ID, StatusAgendamento.REALIZADO);

    verify(gateway).atualizarStatus(
        COLETA_ID, StatusAgendamento.REALIZADO, COOPERATIVA);
  }

  @Test
  void cooperativaDeveConcluirMesmoSendoSolicitante() {
    Coleta atual = confirmada(
        COOPERATIVA, "COOPERATIVA", SINDICO);

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    service.atualizarStatus(
        COOPERATIVA, COLETA_ID, StatusAgendamento.REALIZADO);

    // Preserva a identidade de quem confirmou a solicitação.
    verify(gateway).atualizarStatus(
        COLETA_ID, StatusAgendamento.REALIZADO, SINDICO);
  }

  @Test
  void sindicoNaoDeveMarcarComoRealizado() {
    Coleta atual = confirmada(
        COOPERATIVA, "COOPERATIVA", SINDICO);

    prepararAtualizacao(SINDICO, atual, true, false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.atualizarStatus(
            SINDICO, COLETA_ID, StatusAgendamento.REALIZADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void cooperativaNaoDeveConcluirAntesDaConfirmacao() {
    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "SINDICO");

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.REALIZADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void destinatarioDeveCancelarColetaConfirmada() {
    Coleta atual = confirmada(
        SINDICO, "SINDICO", COOPERATIVA);

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    service.atualizarStatus(
        COOPERATIVA, COLETA_ID, StatusAgendamento.CANCELADO);

    verify(gateway).atualizarStatus(
        COLETA_ID, StatusAgendamento.CANCELADO, COOPERATIVA);
  }

  @Test
  void naoDeveRecusarColetaJaConfirmada() {
    Coleta atual = confirmada(
        SINDICO, "SINDICO", COOPERATIVA);

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.RECUSADO));

    verificarQueNaoAtualizou();
  }

  @ParameterizedTest
  @EnumSource(
      value = StatusAgendamento.class,
      names = {"RECUSADO", "REALIZADO", "CANCELADO"})
  void naoDeveReabrirColetaComStatusFinal(
      StatusAgendamento statusFinal) {

    Coleta atual = coleta(statusFinal, SINDICO, "SINDICO");

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void naoDeveVoltarParaAgendado() {
    Coleta atual = confirmada(
        SINDICO, "SINDICO", COOPERATIVA);

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.AGENDADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void naoDeveResponderColetaSemIdentificacaoDoSolicitante() {
    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, null, null);

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void naoDeveResponderColetaComOrigemDesconhecida() {
    Coleta atual = coleta(
        StatusAgendamento.AGENDADO, SINDICO, "DESCONHECIDA");

    prepararAtualizacao(COOPERATIVA, atual, false, true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void naoDeveAtualizarColetaInexistente() {
    when(gateway.usuarioAtivo(COOPERATIVA)).thenReturn(true);
    when(gateway.buscarComBloqueio(COLETA_ID))
        .thenReturn(Optional.empty());

    assertThrows(
        NotFoundException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verificarQueNaoAtualizou();
  }

  @Test
  void naoDeveAtualizarComUsuarioInativo() {
    when(gateway.usuarioAtivo(COOPERATIVA)).thenReturn(false);

    assertThrows(
        AtualizacaoNaoPermitidaException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, StatusAgendamento.CONFIRMADO));

    verify(gateway, never()).buscarComBloqueio(anyInt());
    verificarQueNaoAtualizou();
  }

  @Test
  void naoDeveAtualizarSemStatus() {
    when(gateway.usuarioAtivo(COOPERATIVA)).thenReturn(true);

    assertThrows(
        BusinessException.class,
        () -> service.atualizarStatus(
            COOPERATIVA, COLETA_ID, null));

    verificarQueNaoAtualizou();
  }

  private void prepararCriacao(
      int usuarioId,
      boolean sindico,
      boolean cooperativa) {

    when(gateway.usuarioAtivo(usuarioId)).thenReturn(true);
    when(gateway.condominioAtivo(CONDOMINIO_ID))
        .thenReturn(true);
    when(gateway.cooperativaAtiva(COOPERATIVA_ID))
        .thenReturn(true);

    when(gateway.sindicoResponsavel(usuarioId, CONDOMINIO_ID))
        .thenReturn(sindico);

    when(gateway.responsavelCooperativa(usuarioId, COOPERATIVA_ID))
        .thenReturn(cooperativa);
  }

  private void prepararAtualizacao(
      int usuarioId,
      Coleta coleta,
      boolean sindico,
      boolean cooperativa) {

    when(gateway.usuarioAtivo(usuarioId)).thenReturn(true);
    when(gateway.buscarComBloqueio(COLETA_ID))
        .thenReturn(Optional.of(coleta));

    when(gateway.sindicoResponsavel(usuarioId, CONDOMINIO_ID))
        .thenReturn(sindico);

    when(gateway.responsavelCooperativa(usuarioId, COOPERATIVA_ID))
        .thenReturn(cooperativa);
  }

  private NovaColeta novaColeta() {
    OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);

    return new NovaColeta(
        CONDOMINIO_ID,
        COOPERATIVA_ID,
        inicio,
        inicio.plusHours(1));
  }

  private Coleta coleta(
      StatusAgendamento status,
      Integer solicitante,
      String origem) {

    NovaColeta entrada = novaColeta();

    return new Coleta(
        COLETA_ID,
        CONDOMINIO_ID,
        COOPERATIVA_ID,
        status,
        entrada.dataInicio(),
        entrada.dataFim(),
        false,
        solicitante,
        origem,
        null,
        null);
  }

  private Coleta confirmada(
      Integer solicitante,
      String origem,
      Integer respondidoPor) {

    Coleta base = coleta(
        StatusAgendamento.CONFIRMADO, solicitante, origem);

    return new Coleta(
        base.id(),
        base.condominioId(),
        base.cooperativaId(),
        base.status(),
        base.dataInicio(),
        base.dataFim(),
        false,
        solicitante,
        origem,
        respondidoPor,
        OffsetDateTime.now().minusMinutes(5));
  }

  private void verificarQueNaoCriou() {
    verify(gateway, never()).criar(
        anyInt(), anyString(), any(NovaColeta.class));
  }

  private void verificarQueNaoAtualizou() {
    verify(gateway, never()).atualizarStatus(
        anyInt(), any(StatusAgendamento.class), any());
  }
}