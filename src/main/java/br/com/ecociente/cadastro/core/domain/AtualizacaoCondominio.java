package br.com.ecociente.cadastro.core.domain;

public record AtualizacaoCondominio(
    String nome,
    Integer tipoCondominioId,
    String urlFoto
) {
}