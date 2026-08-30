package br.com.ecociente.cadastro.core.domain;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder 
public class Cooperativa {
  private Integer idCoop;
  private String cnpjCoop;
  private String nomeCoop;
  private String emailCoop;
  private String telefoneCoop;
  private OffsetDateTime dataCadastro;
  private Integer usuarioId;
  private Integer enderecoId;
}
