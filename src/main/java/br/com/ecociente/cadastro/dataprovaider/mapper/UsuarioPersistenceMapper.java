package br.com.ecociente.cadastro.dataprovaider.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.dataprovaider.entity.UserEntity;

@Component 
public class UsuarioPersistenceMapper {

  public UserEntity toEntity(Usuario usuario){
    return UserEntity.builder()
    .id(usuario.getIdUsuario())
    .nomeUsuario(usuario.getNomeUsuario())
    .emailUsuario(usuario.getEmaiUsuario())
    .senhaHash(usuario.getSenhaHash())
    .dataNascimento(usuario.getDataNascimento())
    .cpf(usuario.getCpf())
    .urlAvatar(usuario.getUrlAvatar())
    .ativo(usuario.getAtivo())
    .registroEm(usuario.getRegistroEm())
    .tipoUsuarioId(usuario.getTipoUsuarioId())
    .enderecoId(usuario.getEnderecoId())
    .build();
  }

  public Usuario toDomain(UserEntity userEntity){
    return Usuario.builder()
    .idUsuario(userEntity.getId())
    .nomeUsuario(userEntity.getNomeUsuario())
    .emaiUsuario(userEntity.getEmailUsuario())
    .senhaHash(userEntity.getSenhaHash())
    .dataNascimento(userEntity.getDataNascimento())
    .cpf(userEntity.getCpf())
    .urlAvatar(userEntity.getUrlAvatar())
    .ativo(userEntity.getAtivo())
    .registroEm(userEntity.getRegistroEm())
    .tipoUsuarioId(userEntity.getTipoUsuarioId())
    .enderecoId(userEntity.getEnderecoId())
    .build();
  }
  
}
