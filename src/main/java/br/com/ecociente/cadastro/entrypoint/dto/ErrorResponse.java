package br.com.ecociente.cadastro.entrypoint.dto;

import java.util.List;

import lombok.Builder;

@Builder 
public record ErrorResponse(
  int status, 
  String codigoError, 
  List<ValidationError> details) {
}
