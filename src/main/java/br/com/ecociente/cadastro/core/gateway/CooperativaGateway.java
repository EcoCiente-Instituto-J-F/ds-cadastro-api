package br.com.ecociente.cadastro.core.gateway;

import br.com.ecociente.cadastro.core.domain.Cooperativa;

public interface CooperativaGateway {
  Cooperativa salvar(Cooperativa cooperativa);
  boolean existePorCnpj(String cnpj);
  
}
