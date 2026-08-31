package br.com.ecociente.cadastro.entrypoint.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;

@Builder 
public record CepResponseDto(
  String cep,
  @JsonProperty("uf")
  String estado,
  @JsonProperty("localidade")
  String cidade,
  String logradouro
) {
  
}
