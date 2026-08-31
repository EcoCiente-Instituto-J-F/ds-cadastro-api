package br.com.ecociente.cadastro.dataprovaider.entity;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_cooperativas",
  uniqueConstraints = {
    @UniqueConstraint(name = "uq_cooperativas_endereco_id", columnNames = "endereco_id"),
    @UniqueConstraint(name = "uq_cooperativas_usuario_id", columnNames = "usuario_id")
  }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CooperativaEntity {
 
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_cooperativa")
  private Integer idCooperativa;

  @Column(name = "cnpj_cooperativa", nullable = false, length = 18)
  private String cnpjCooperativa;

  @Column(name = "nome_cooperativa", nullable = false, length = 255)
  private String nomeCooperativa;

  @Column(name = "email_cooperativa", length = 255)
  private String emailCooperativa;

  @Column(name = "telefone_cooperativa", length = 20)
  private String telefoneCooperativa;

  @Column(name = "data_cadastro",nullable = false, updatable = false)
  @Builder.Default
  private OffsetDateTime dataCadastro = OffsetDateTime.now();

  @Column(name = "usuario_id", nullable = false)
private Integer userId;

@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(
    name = "usuario_id",
    insertable = false,
    updatable = false
)
private UserEntity userEntity;

@Column(name = "endereco_id", nullable = false)
private Integer enderecoId;

@OneToOne(fetch = FetchType.LAZY)
@JoinColumn(
    name = "endereco_id",
    insertable = false,
    updatable = false
)
private EnderecoEntity enderecoEntity;
}
