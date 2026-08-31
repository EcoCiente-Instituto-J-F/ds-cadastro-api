package br.com.ecociente.cadastro.core.exception;

public class CepException extends DomainException{
  public CepException(String codigoErro,String message){
    super(codigoErro, message);
  }
  
}
