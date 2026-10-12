package br.com.ecociente.cadastro.core.exception;

public class AtualizacaoNaoPermitidaException extends DomainException {

    public AtualizacaoNaoPermitidaException(String mensagem) {
        super("UPDATE_NOT_ALLOWED", mensagem);
    }
}