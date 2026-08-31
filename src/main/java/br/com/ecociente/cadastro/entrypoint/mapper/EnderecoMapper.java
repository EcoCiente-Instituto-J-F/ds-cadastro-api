package br.com.ecociente.cadastro.entrypoint.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.entrypoint.dto.response.CepResponseDto;

@Component 
public class EnderecoMapper {
  public Endereco toDomain(CepResponseDto cep, String numero, String complemento){
    return new Endereco(
      null,
      cep.cep(),
      cep.estado(),
      cep.cidade(),
      cep.logradouro(),
      numero,
      complemento
    );
  }
}
