package br.com.ecociente.cadastro.core.usecase.input;

import java.time.LocalDate;

import br.com.ecociente.cadastro.core.usecase.output.CadastroMoradorOutput;

public interface CadastrarMoradorUseCase {
  CadastroMoradorOutput executar(
    String nomeCompleto, 
    String email, 
    String senha, 
    LocalDate dataNascimento, 
    String cpf, 
    String codigoCondominio);
}
