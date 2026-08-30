package br.com.ecociente.cadastro.dataprovaider.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.dataprovaider.entity.UserEntity;
import br.com.ecociente.cadastro.dataprovaider.entity.UsuarioComumEntity;
import br.com.ecociente.cadastro.dataprovaider.mapper.UsuarioPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.repository.UsuarioComumRepository;
import br.com.ecociente.cadastro.dataprovaider.repository.UsuarioRepository;

@Component 
public class UsuarioGatewayImpl implements UsuarioGateway {

  private final UsuarioRepository usuarioRepository;
  private final UsuarioComumRepository usuarioComumRepository;
  private final UsuarioPersistenceMapper usuarioPersistenceMapper;

  public UsuarioGatewayImpl(
    UsuarioRepository usuarioRepository,
    UsuarioComumRepository usuarioComumRepository,
    UsuarioPersistenceMapper usuarioPersistenceMapper
  ){
    this.usuarioRepository = usuarioRepository;
    this.usuarioComumRepository = usuarioComumRepository;
    this.usuarioPersistenceMapper = usuarioPersistenceMapper;
  }

  @Override
  public Usuario salvar(Usuario usuario) {
    UserEntity userEntity = usuarioPersistenceMapper.toEntity(usuario);
    UserEntity salvar = usuarioRepository.save(userEntity);
    return usuarioPersistenceMapper.toDomain(salvar);
  }

  @Override
  public boolean existePorEmail(String email) {
    return usuarioRepository.existsByEmailUsuario(email);
  }

  @Override
  public boolean existePorCpf(String cpf) {
    return usuarioRepository.existsByCpf(cpf);
  }

  @Override
  public void salvarVinculoUsuarioComum(Integer usuarioId) {
    UserEntity userEntity = usuarioRepository.getReferenceById(usuarioId);
    UsuarioComumEntity usuarioComumEntity = UsuarioComumEntity.builder().userEntity(userEntity).build();
    usuarioComumRepository.save(usuarioComumEntity);
  }

}
