package br.com.ecociente.cadastro.core.gateway;

import br.com.ecociente.cadastro.core.domain.Endereco;

public interface CepGateway {
  Endereco buscarEnderecoPorCep(String cep);
}
