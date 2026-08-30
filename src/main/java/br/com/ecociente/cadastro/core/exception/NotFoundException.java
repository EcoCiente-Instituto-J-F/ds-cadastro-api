package br.com.ecociente.cadastro.core.exception;

public class NotFoundException extends RuntimeException {
  private final String codigoErro;

  public NotFoundException(String codigoErro, String message){
    super(message);
    this.codigoErro=codigoErro;
  }
  public String getCodigoErro(){
    return codigoErro;
  }
  
}
