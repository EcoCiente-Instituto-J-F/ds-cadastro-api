package br.com.ecociente.cadastro.core.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Cooperativa;

@Component 
public interface CooperativaGateway {
  Cooperativa salvar(Cooperativa cooperativa);
  boolean existePorCnpj(String cnpj);
  
}
