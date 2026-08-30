package br.com.ecociente.cadastro.core.domain;

import java.time.LocalDate;
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
public class Usuario {
  private Integer idUsuario;
  private String nomeUsuario;
  private String emaiUsuario;
  private String senhaHash;
  private LocalDate dataNascimento;
  private String cpf;
  private String urlAvatar;
  private Boolean ativo;
  private OffsetDateTime registroEm;
  private Integer tipoUsuarioId;
  private Integer enderecoId;
  
}
