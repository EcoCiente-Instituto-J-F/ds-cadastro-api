package br.com.ecociente.cadastro.core.usecase.input;


import br.com.ecociente.cadastro.core.usecase.output.CadastroCooperativaOutput;

public interface CadastrarCooperativaUseCase {
  CadastroCooperativaOutput executar(
    String nomeResponsavel, 
    String email, 
    String senha, 
    String nomeCooperativa,
    String cnpj,
    String emailCooperativa,
    String telefone,
    String cep, 
    String numero, 
    String complemento);
}
