package br.com.ecociente.cadastro.core.gateway;

import java.util.Optional;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.core.domain.Morador;

@Component 
public interface CondominioGateway {
  Optional<Condominio> buscarPorCodigoAcesso(String codigoAcesso);
  Morador vincularMorador(Morador morador);
}
