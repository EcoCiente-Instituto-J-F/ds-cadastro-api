package br.com.ecociente.cadastro.core.exception;

public class NotFoundException extends DomainException {

  public NotFoundException(String codigoErro, String message){
    super(codigoErro,message);

  }
  
}
