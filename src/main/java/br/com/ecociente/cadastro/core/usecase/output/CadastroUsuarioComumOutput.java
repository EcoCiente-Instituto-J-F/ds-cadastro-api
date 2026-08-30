package br.com.ecociente.cadastro.core.usecase.output;

import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;

public record CadastroUsuarioComumOutput(
  Usuario usuario, Endereco endereco
) {
  
}
