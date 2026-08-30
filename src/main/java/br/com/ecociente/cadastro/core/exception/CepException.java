package br.com.ecociente.cadastro.core.exception;

public class CepException extends RuntimeException{
  private final String codigoErro;

  public CepException(String codigoErro, String message){
    super(message);
    this.codigoErro=codigoErro;
  }
  public String getCodigoErro(){
    return codigoErro;
  }
  
}
