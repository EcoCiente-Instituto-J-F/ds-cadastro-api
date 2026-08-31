package br.com.ecociente.cadastro.dataprovaider.entity;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_usuario", nullable = false)
  private Integer id;

  @Column(name = "nome_usuario", nullable = false, length = 100)
  private String nomeUsuario;

  @Column(name = "email_usuario",nullable = false,length = 255)
  private String emailUsuario;

  @Column(name = "senha_hash",nullable = false, length = 255)
  private String senhaHash;

  @Column(name = "data_nascimento", nullable = false )
  private LocalDate dataNascimento;

  @Column(name = "cpf", nullable = false, length = 18)
  private String cpf;

  @Column(name = "url_avatar", length = 500)
  private String urlAvatar;

  @Column(name = "ativo", nullable = false)
  @Builder.Default
  private Boolean ativo = true;

  @Column(name = "registro_em", nullable = false, updatable = false)
  @Builder.Default
  private OffsetDateTime registroEm = OffsetDateTime.now();

  @Column(name = "tipo_usuario_id", nullable = false)
  private Integer tipoUsuarioId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "tipo_usuario_id", insertable = false, updatable = false)
  private TipoUsuarioEntity tipoUsuario;

  @Column(name = "endereco_id", nullable = false)
  private Integer enderecoId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "endereco_id", insertable = false, updatable = false)
  private EnderecoEntity endereco;
  
}
