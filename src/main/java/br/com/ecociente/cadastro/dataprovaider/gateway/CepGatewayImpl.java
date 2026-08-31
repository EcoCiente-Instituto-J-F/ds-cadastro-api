package br.com.ecociente.cadastro.dataprovaider.gateway;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.exception.CepException;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.entrypoint.dto.response.CepResponseDto;

@Component 
public class CepGatewayImpl implements CepGateway {
  private final RestTemplate restTemplate;

  public CepGatewayImpl(RestTemplate restTemplate){
    this.restTemplate = restTemplate;
  }

  @Override
  public Endereco buscarEnderecoPorCep(String cep) {
    String cepLimpo = cep.replaceAll("\\D","");
    String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

    CepResponseDto responseDto;
    try{
      responseDto = restTemplate.getForObject(url, CepResponseDto.class);
    }catch(Exception e){
      throw new CepException("CEP_EXCEPTION_UNAVAILABLE","Consulta de CEP indisponível");
    }
    if (responseDto == null) {
      throw new CepException("CEP_EXCEPTION_UNAVAILABLE","Consulta de CEP indisponível");      
    }

    return Endereco.builder()
      .idEndereco(null)
      .cep(responseDto.cep())
      .estado(responseDto.estado())
      .cidade(responseDto.cidade())
      .logradouro(responseDto.logradouro())
      .numero(null)
      .complemento(null)
      .build();
  }

  

  
}
