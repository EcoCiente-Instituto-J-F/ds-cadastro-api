package br.com.ecociente.cadastro.core.usecase;


import br.com.ecociente.cadastro.core.domain.Cooperativa;

public interface CadastrarCooperativaUseCase {
  Cooperativa executar(
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
