package br.com.ecociente.cadastro.core.Service;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ecociente.cadastro.core.domain.Coleta;
import br.com.ecociente.cadastro.core.domain.NovaColeta;
import br.com.ecociente.cadastro.core.domain.StatusAgendamento;
import br.com.ecociente.cadastro.core.exception.AtualizacaoNaoPermitidaException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.exception.NotFoundException;
import br.com.ecociente.cadastro.core.gateway.ColetaGateway;

@Service
public class ColetaService {

  private final ColetaGateway gateway;

  public ColetaService(ColetaGateway gateway) {
    this.gateway = gateway;
  }

  @Transactional
  public Coleta criar(
      Integer usuarioId,
      NovaColeta entrada) {

    validarUsuario(usuarioId);
    validarDatas(entrada);

    if (!gateway.condominioAtivo(entrada.condominioId())) {
      throw new NotFoundException(
          "CONDOMINIO_NAO_ENCONTRADO",
          "Condomínio não encontrado ou inativo");
    }

    if (!gateway.cooperativaAtiva(entrada.cooperativaId())) {
      throw new NotFoundException(
          "COOPERATIVA_NAO_ENCONTRADA",
          "Cooperativa não encontrada ou sem usuário ativo");
    }

    boolean sindico = gateway.sindicoResponsavel(
        usuarioId, entrada.condominioId());

    boolean cooperativa = gateway.responsavelCooperativa(
        usuarioId, entrada.cooperativaId());

    if (!sindico && !cooperativa) {
      throw proibido(
          "Você não representa o condomínio nem a cooperativa informados");
    }

    if (sindico && cooperativa) {
      throw proibido(
          "O mesmo usuário não pode representar as duas partes da solicitação");
    }

    return gateway.criar(
        usuarioId,
        sindico ? "SINDICO" : "COOPERATIVA",
        entrada);
  }

  @Transactional
  public Coleta atualizarStatus(
      Integer usuarioId,
      Integer coletaId,
      StatusAgendamento novoStatus) {

    validarUsuario(usuarioId);

    if (novoStatus == null) {
      throw new BusinessException(
          "STATUS_OBRIGATORIO",
          "Informe o novo status");
    }

    Coleta coleta = gateway.buscarComBloqueio(coletaId)
        .orElseThrow(() -> new NotFoundException(
            "COLETA_NAO_ENCONTRADA",
            "Coleta não encontrada"));

    boolean sindico = gateway.sindicoResponsavel(
        usuarioId, coleta.condominioId());

    boolean cooperativa = gateway.responsavelCooperativa(
        usuarioId, coleta.cooperativaId());

    if (!sindico && !cooperativa) {
      throw proibido("Você não participa desta coleta");
    }

    if (novoStatus == StatusAgendamento.REALIZADO) {
      if (!cooperativa) {
        throw proibido(
            "Somente a cooperativa envolvida pode marcar como realizado");
      }
    } else {
      validarDestinatario(
          usuarioId, coleta, sindico, cooperativa);
    }

    validarTransicao(coleta.status(), novoStatus);

    /*
     * Os campos de resposta representam a resposta à solicitação.
     * A conclusão preserva quem confirmou e quando confirmou.
     */
    Integer respondidoPor = novoStatus == StatusAgendamento.REALIZADO
        ? coleta.respondidoPorUsuarioId()
        : usuarioId;

    return gateway.atualizarStatus(
        coletaId, novoStatus, respondidoPor);
  }

  private void validarDestinatario(
      Integer usuarioId,
      Coleta coleta,
      boolean sindico,
      boolean cooperativa) {

    if (coleta.solicitadoPorUsuarioId() == null
        || coleta.origemSolicitacao() == null) {
      throw new BusinessException(
          "ORIGEM_SOLICITACAO_AUSENTE",
          "A coleta não possui identificação completa do solicitante");
    }

    if (usuarioId.equals(coleta.solicitadoPorUsuarioId())) {
      throw proibido(
          "O solicitante não pode responder ou cancelar a própria solicitação");
    }

    boolean destinatario = switch (coleta.origemSolicitacao()) {
      case "SINDICO" -> cooperativa;
      case "COOPERATIVA" -> sindico;
      default -> throw new BusinessException(
          "ORIGEM_SOLICITACAO_INVALIDA",
          "Origem da solicitação não reconhecida");
    };

    if (!destinatario) {
      throw proibido(
          "Somente a parte destinatária pode responder ou cancelar");
    }
  }

  private void validarTransicao(
      StatusAgendamento atual,
      StatusAgendamento novo) {

    boolean permitida = switch (atual) {
      case AGENDADO ->
        novo == StatusAgendamento.CONFIRMADO
            || novo == StatusAgendamento.RECUSADO
            || novo == StatusAgendamento.CANCELADO;

      case CONFIRMADO ->
        novo == StatusAgendamento.REALIZADO
            || novo == StatusAgendamento.CANCELADO;

      case RECUSADO, REALIZADO, CANCELADO -> false;
    };

    if (!permitida) {
      throw new BusinessException(
          "TRANSICAO_STATUS_INVALIDA",
          "Não é permitido alterar de " + atual + " para " + novo);
    }
  }

  private void validarUsuario(Integer usuarioId) {
    if (usuarioId == null || !gateway.usuarioAtivo(usuarioId)) {
      throw proibido("Usuário não encontrado ou inativo");
    }
  }

  private void validarDatas(NovaColeta entrada) {
    if (entrada == null
        || entrada.condominioId() == null
        || entrada.cooperativaId() == null
        || entrada.condominioId() <= 0
        || entrada.cooperativaId() <= 0
        || entrada.dataInicio() == null) {
      throw new BusinessException(
          "DADOS_COLETA_INVALIDOS",
          "Informe condomínio, cooperativa e data de início válidos");
    }

    if (!entrada.dataInicio().isAfter(OffsetDateTime.now())) {
      throw new BusinessException(
          "DATA_INICIO_INVALIDA",
          "Data de início deve estar no futuro");
    }

    if (entrada.dataFim() != null
        && entrada.dataFim().isBefore(entrada.dataInicio())) {
      throw new BusinessException(
          "PERIODO_INVALIDO",
          "Data de fim deve ser igual ou posterior ao início");
    }
  }

  private AtualizacaoNaoPermitidaException proibido(
      String mensagem) {
    return new AtualizacaoNaoPermitidaException(mensagem);
  }
}