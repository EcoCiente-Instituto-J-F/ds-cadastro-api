package br.com.ecociente.cadastro.dataprovaider.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_enderecos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnderecoEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_endereco")
  private Integer idEndereco;

  @Column(name = "cep", length = 10)
  private String cep;

  @Column(name = "estado", length = 2)
  private String estado;

  @Column(name = "cidade", length = 100)
  private String cidade;

  @Column(name = "logradouro", length = 150)
  private String logradouro;

  @Column(name = "numero",length = 20)
  private String numero;

  @Column(name = "complemento",length = 100)
  private String complemento;

  @Column(name = "latitude", precision = 9, scale = 6)
  private Double latitude;

  @Column(name = "longitude", precision = 9, scale = 6)
  private Double longitude;

}
