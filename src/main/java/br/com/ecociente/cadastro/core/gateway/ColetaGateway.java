package br.com.ecociente.cadastro.core.gateway;

import java.util.Optional;

import br.com.ecociente.cadastro.core.domain.Coleta;
import br.com.ecociente.cadastro.core.domain.NovaColeta;
import br.com.ecociente.cadastro.core.domain.StatusAgendamento;

public interface ColetaGateway {

  boolean usuarioAtivo(Integer usuarioId);

  boolean condominioAtivo(Integer condominioId);

  boolean cooperativaAtiva(Integer cooperativaId);

  boolean sindicoResponsavel(
      Integer usuarioId,
      Integer condominioId);

  boolean responsavelCooperativa(
      Integer usuarioId,
      Integer cooperativaId);

  Coleta criar(
      Integer usuarioId,
      String origem,
      NovaColeta entrada);

  Optional<Coleta> buscarComBloqueio(Integer coletaId);

  Coleta atualizarStatus(
      Integer coletaId,
      StatusAgendamento status,
      Integer respondidoPorUsuarioId);
}