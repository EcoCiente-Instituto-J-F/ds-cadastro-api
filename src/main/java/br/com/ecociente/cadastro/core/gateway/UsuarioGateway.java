package br.com.ecociente.cadastro.core.gateway;

import br.com.ecociente.cadastro.core.domain.Usuario;

public interface UsuarioGateway {
  Usuario salvar(Usuario usuario);
  boolean existePorEmail(String email);
  boolean existePorCpf(String cpf);
  void salvarVinculoUsuarioComum(Integer usuarioId);  
}
