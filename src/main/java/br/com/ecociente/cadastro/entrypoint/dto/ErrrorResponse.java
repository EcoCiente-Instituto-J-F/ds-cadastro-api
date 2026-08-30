package br.com.ecociente.cadastro.entrypoint.dto;

import java.util.List;

import lombok.Builder;

@Builder 
public record ErrrorResponse(
  int status, 
  String codigoError, 
  List<ValidationError> details) {
}
