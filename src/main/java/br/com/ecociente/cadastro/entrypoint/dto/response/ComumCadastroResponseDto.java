package br.com.ecociente.cadastro.entrypoint.dto.response;

import lombok.Builder;

@Builder
public record ComumCadastroResponseDto(
  Integer id,
  String tipo,
  String nomeCompleto,
  String email,
  String cpf,
  String dataNascimento,
  EnderecoDto endereco,
  String registroEm
) {
  @Builder 
  public record EnderecoDto(
    String cep,
    String estado,
    String cidade,
    String logradouro,
    String numero,
    String complemento
) {
  
}

}
