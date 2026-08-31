package br.com.ecociente.cadastro.core.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Endereco;

@Component 
public interface CepGateway {
  Endereco buscarEnderecoPorCep(String cep);
}
