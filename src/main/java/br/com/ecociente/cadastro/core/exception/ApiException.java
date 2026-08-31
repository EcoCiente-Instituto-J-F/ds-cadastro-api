package br.com.ecociente.cadastro.core.exception;

public class ApiException extends DomainException{

  public ApiException(String codigoErro, String message){
    super(codigoErro,message);
  }
  
}
