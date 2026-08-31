package br.com.ecociente.cadastro.core.exception;

public class BusinessException extends DomainException {
  public BusinessException(String codigoErro, String message){
    super(codigoErro,message);
  }
  
}
