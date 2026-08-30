package br.com.ecociente.cadastro.core.exception;

public class AlreadyExistsException extends RuntimeException {
  private final String codigoErro;

  public AlreadyExistsException(String codigoErro, String message){
    super(message);
    this.codigoErro=codigoErro;
  }
  public String getCodigoErro(){
    return codigoErro;
  }
}
