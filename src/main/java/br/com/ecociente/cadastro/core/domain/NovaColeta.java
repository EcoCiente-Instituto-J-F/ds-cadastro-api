package br.com.ecociente.cadastro.core.domain;

import java.time.OffsetDateTime;

public record NovaColeta(
    Integer condominioId,
    Integer cooperativaId,
    OffsetDateTime dataInicio,
    OffsetDateTime dataFim
) {
}