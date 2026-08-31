package br.com.ecociente.cadastro.core.usecase.input;

import java.time.LocalDate;

import br.com.ecociente.cadastro.core.usecase.output.CadastroUsuarioComumOutput;

public interface CadastroUsuarioComumUseCase {
  CadastroUsuarioComumOutput executar (
    String nomeCompleto, 
    String email, 
    String senha, 
    LocalDate dataNascimento, 
    String cpf, 
    String cep, 
    String numero, 
    String complemento);
}
