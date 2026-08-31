package br.com.ecociente.cadastro.dataprovaider.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.gateway.CooperativaGateway;
import br.com.ecociente.cadastro.dataprovaider.entity.CooperativaEntity;
import br.com.ecociente.cadastro.dataprovaider.mapper.CooperativaPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.repository.CooperativaRepository;

@Component 
public class CooperativaGatewayImpl implements CooperativaGateway {

  private final CooperativaRepository cooperativaRepository;
  private final CooperativaPersistenceMapper cooperativaPersistenceMapper;

  public CooperativaGatewayImpl(CooperativaRepository cooperativaRepository,CooperativaPersistenceMapper cooperativaPersistenceMapper){
    this.cooperativaRepository=cooperativaRepository;
    this.cooperativaPersistenceMapper=cooperativaPersistenceMapper;
  }


  @Override
  public Cooperativa salvar(Cooperativa cooperativa) {
    CooperativaEntity entity = cooperativaPersistenceMapper.toEntity(cooperativa);
    CooperativaEntity salvar = cooperativaRepository.save(entity);
    return cooperativaPersistenceMapper.toDomain(salvar);
  }

  @Override
  public boolean existePorCnpj(String cnpj) {
    return cooperativaRepository.existsByCnpjCooperativa(cnpj);
  }
  
}
