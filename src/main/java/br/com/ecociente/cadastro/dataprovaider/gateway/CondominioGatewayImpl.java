package br.com.ecociente.cadastro.dataprovaider.gateway;

import java.util.Optional;

import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.core.domain.Morador;
import br.com.ecociente.cadastro.core.gateway.CondominioGateway;
import br.com.ecociente.cadastro.dataprovaider.entity.MoradorEntity;
import br.com.ecociente.cadastro.dataprovaider.mapper.CondominioPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.mapper.MoradorPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.repository.CondominioRepository;
import br.com.ecociente.cadastro.dataprovaider.repository.MoradorRepository;

public class CondominioGatewayImpl implements CondominioGateway{

  private final CondominioRepository condominioRepository;
  private final MoradorRepository moradorRepository;
  private final CondominioPersistenceMapper condominioPersistenceMapper;
  private final MoradorPersistenceMapper moradorPersistenceMapper;

  public CondominioGatewayImpl(
    CondominioRepository condominioRepository,
    MoradorRepository moradorRepository,
    CondominioPersistenceMapper condominioPersistenceMapper,
    MoradorPersistenceMapper moradorPersistenceMapper
  ){
    this.condominioRepository = condominioRepository;
    this.moradorRepository = moradorRepository;
    this.condominioPersistenceMapper = condominioPersistenceMapper;
    this.moradorPersistenceMapper = moradorPersistenceMapper;
  }

  @Override
  public Optional<Condominio> buscarPorCodigoAcesso(String codigoAcesso) {
    return condominioRepository.findByCodigoAcessoIgnoreCase(codigoAcesso).map(condominioPersistenceMapper::toDomain);
  }

  @Override
  public Morador vincularMorador(Morador morador) {
    MoradorEntity entity = moradorPersistenceMapper.toEntity(morador);
    MoradorEntity salvar = moradorRepository.save(entity);
    return moradorPersistenceMapper.toDomain(salvar);
  }

  
 
  
}
