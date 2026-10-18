package br.com.ecociente.cadastro.entrypoint.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Coleta;
import br.com.ecociente.cadastro.core.domain.NovaColeta;
import br.com.ecociente.cadastro.entrypoint.dto.request.CriarColetaRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.response.ColetaResponseDto;

@Component
public class ColetaMapper {

  public NovaColeta toDomain(CriarColetaRequestDto request) {
    return new NovaColeta(
        request.condominioId(),
        request.cooperativaId(),
        request.dataInicio(),
        request.dataFim());
  }

  public ColetaResponseDto toResponse(Coleta coleta) {
    return new ColetaResponseDto(
        coleta.id(),
        coleta.condominioId(),
        coleta.cooperativaId(),
        coleta.status(),
        coleta.dataInicio(),
        coleta.dataFim(),
        coleta.possuiRecorrencia(),
        coleta.solicitadoPorUsuarioId(),
        coleta.origemSolicitacao(),
        coleta.respondidoPorUsuarioId(),
        coleta.respondidoEm());
  }
}