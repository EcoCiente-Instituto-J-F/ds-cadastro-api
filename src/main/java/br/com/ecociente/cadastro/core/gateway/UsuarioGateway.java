package br.com.ecociente.cadastro.core.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Usuario;

@Component 
public interface UsuarioGateway {
  Usuario salvar(Usuario usuario);
  boolean existePorEmail(String email);
  boolean existePorCpf(String cpf);
}
