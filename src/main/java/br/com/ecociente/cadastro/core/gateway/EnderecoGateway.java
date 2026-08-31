package br.com.ecociente.cadastro.core.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Endereco;

@Component 
public interface EnderecoGateway {
  Endereco salvar(Endereco endereco);
  Endereco buscarPorId(Integer idEndereco);  
}
