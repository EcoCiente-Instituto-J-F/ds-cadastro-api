package br.com.ecociente.cadastro.entrypoint.dto.response;

import java.time.OffsetDateTime;

import br.com.ecociente.cadastro.core.domain.StatusAgendamento;

public record ColetaResponseDto(
    Integer id,
    Integer condominioId,
    Integer cooperativaId,
    StatusAgendamento status,
    OffsetDateTime dataInicio,
    OffsetDateTime dataFim,
    boolean possuiRecorrencia,
    Integer solicitadoPorUsuarioId,
    String origemSolicitacao,
    Integer respondidoPorUsuarioId,
    OffsetDateTime respondidoEm
) {
}