package br.com.ecociente.cadastro.core.usecase.output;

import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;

public record CadastroCooperativaOutput(
  Usuario usuario,
  Cooperativa cooperativa,
  Endereco endereco
) {
  
}
