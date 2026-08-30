package br.com.ecociente.cadastro.core.gateway;

import br.com.ecociente.cadastro.core.domain.Endereco;

public interface EnderecoGateway {
  Endereco salvar(Endereco endereco);
  Endereco buscarPorId(Integer idEndereco);  
}
