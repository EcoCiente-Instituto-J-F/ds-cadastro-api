package br.com.ecociente.cadastro.core.gateway;

import java.util.Optional;

import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.core.domain.Morador;

public interface CondominioGateway {
  Optional<Condominio> buscarPorCodigoAcesso(String codigoAcesso);
  Morador vincularMorador(Morador morador);
}
