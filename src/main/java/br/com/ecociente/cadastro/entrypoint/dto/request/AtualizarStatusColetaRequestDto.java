package br.com.ecociente.cadastro.entrypoint.dto.request;

import br.com.ecociente.cadastro.core.domain.StatusAgendamento;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusColetaRequestDto(

    @NotNull(message = "Status é obrigatório")
    StatusAgendamento status

) {
}