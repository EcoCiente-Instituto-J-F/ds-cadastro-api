package br.com.ecociente.cadastro.entrypoint.dto;

import lombok.Builder;

/**
 * ValidationError
 */
@Builder
public record ValidationError(
  String field, 
  String message
) {

}
