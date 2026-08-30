package br.com.ecociente.cadastro.entrypoint.dto.response;

import lombok.Builder;

@Builder 
public record CepResponseDto(
  String cep,
  String estado,
  String cidade,
  String logradouro
) {
  
}
