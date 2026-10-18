package br.com.ecociente.cadastro.core.domain;

import java.time.OffsetDateTime;

public record Coleta(
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