package br.com.ecociente.cadastro.core.domain;

import java.time.LocalDate;

public record UsuarioEdicao(
    Integer id,
    String tipo,
    Integer enderecoId,
    LocalDate dataNascimento
) {
}