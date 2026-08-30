package br.com.ecociente.cadastro.core.exception;

public class ApiException extends RuntimeException{
  private final String codigoErro;

  public ApiException(String codigoErro, String message){
    super(message);
    this.codigoErro=codigoErro;
  }
  public String getCodigoErro(){
    return codigoErro;
  }
  
}
