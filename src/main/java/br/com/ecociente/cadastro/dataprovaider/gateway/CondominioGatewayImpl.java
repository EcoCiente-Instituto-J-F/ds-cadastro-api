package br.com.ecociente.cadastro.dataprovaider.gateway;

import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.core.domain.Morador;
import br.com.ecociente.cadastro.core.gateway.CondominioGateway;
import br.com.ecociente.cadastro.dataprovaider.entity.MoradorEntity;
import br.com.ecociente.cadastro.dataprovaider.mapper.CondominioPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.mapper.MoradorPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.repository.CondominioRepository;
import br.com.ecociente.cadastro.dataprovaider.repository.MoradorRepository;

@Component 
public class CondominioGatewayImpl implements CondominioGateway{

  private final CondominioRepository condominioRepository;
  private final MoradorRepository moradorRepository;
  private final CondominioPersistenceMapper condominioPersistenceMapper;
  private final MoradorPersistenceMapper moradorPersistenceMapper;
  private final JdbcTemplate jdbc;

  public CondominioGatewayImpl(
    CondominioRepository condominioRepository,
    MoradorRepository moradorRepository,
    CondominioPersistenceMapper condominioPersistenceMapper,
    MoradorPersistenceMapper moradorPersistenceMapper,
    JdbcTemplate jdbc
  ){
    this.condominioRepository = condominioRepository;
    this.moradorRepository = moradorRepository;
    this.condominioPersistenceMapper = condominioPersistenceMapper;
    this.moradorPersistenceMapper = moradorPersistenceMapper;
    this.jdbc = jdbc;
  }

  @Override
  public Optional<Condominio> buscarPorCodigoAcesso(String codigoAcesso) {
    return condominioRepository.findByCodigoAcessoIgnoreCase(codigoAcesso).map(condominioPersistenceMapper::toDomain);
  }

  @Override
  public Morador vincularMorador(Morador morador) {
    MoradorEntity entity = moradorPersistenceMapper.toEntity(morador);
    MoradorEntity salvar = moradorRepository.save(entity);

    // A mobile-api só reconhece o morador com a linha em tb_moradores além do vínculo aprovado.
    jdbc.update(
      "INSERT INTO tb_moradores (condominio_id, usuario_id) VALUES (?, ?) ON CONFLICT (usuario_id) DO NOTHING",
      salvar.getCondominioId(), salvar.getUsuarioId());

    return moradorPersistenceMapper.toDomain(salvar);
  }

  
 
  
}
