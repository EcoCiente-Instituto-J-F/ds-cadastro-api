package br.com.ecociente.cadastro.entrypoint.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AtualizarEnderecoRequestDto(

    @NotBlank(message = "CEP é obrigatório")
    @Pattern(
        regexp = "^\\d{5}-?\\d{3}$",
        message = "CEP inválido"
    )
    String cep,

    @NotBlank(message = "Número é obrigatório")
    @Size(max = 20)
    String numero,

    @Size(max = 100)
    String complemento

) {
}