package br.com.ecociente.cadastro.core.domain;

import java.time.LocalDate;

public record AtualizacaoDados(
    String nome,
    String email,
    LocalDate dataNascimento,
    String urlAvatar,
    String telefone,
    Integer telefoneId
) {
}