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
@Table(name = "tb_lkp_tipos_usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoUsuarioEntity {
  
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id_tipo_usuario")
  private Integer idTipoUsuario;

  @Column(name = "nome_tipo",nullable = false, length = 50)
  private String nomeTipo;

  @Column(name = "descricao",length = 255)
  private String descricao;

  public static final int USUARIO_COMUM = 1;
  public static final int SINDICO_RESIDENCIAL = 2;
  public static final int SINDICO_COMERCIAL = 3;
  public static final int MORADOR_RESIDENCIAL = 4;
  public static final int USUARIO_COMERCIAL = 5;
  public static final int COOPERATIVA = 6;
  public static final int ADMINISTRADOR = 7;
  
} 
