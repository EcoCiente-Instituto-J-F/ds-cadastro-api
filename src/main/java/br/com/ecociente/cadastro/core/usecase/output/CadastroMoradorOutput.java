package br.com.ecociente.cadastro.core.usecase.output;

import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;

public record CadastroMoradorOutput(
  Usuario usuario,
  String codigoCondominio,
  String nomeCondominio,
  Endereco enderecoCondominio
) {
  
}
