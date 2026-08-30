package br.com.ecociente.cadastro.core.usecase;

import java.time.LocalDate;

import br.com.ecociente.cadastro.core.domain.Usuario;

public interface CadastrarMoradorUseCase {
  Usuario executar(
    String nomeCompleto, 
    String email, 
    String senha, 
    LocalDate dataNascimento, 
    String cpf, 
    String codigoCondominio);
}
