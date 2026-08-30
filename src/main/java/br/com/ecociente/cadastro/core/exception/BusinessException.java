package br.com.ecociente.cadastro.core.exception;

public class BusinessException extends RuntimeException {
  private final String codigoErro;

  public BusinessException(String codigoErro, String message){
    super(message);
    this.codigoErro=codigoErro;
  }
  public String getCodigoErro(){
    return codigoErro;
  }
  
}
