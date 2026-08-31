package br.com.ecociente.cadastro.dataprovaider.gateway;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.exception.NotFoundException;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.dataprovaider.entity.EnderecoEntity;
import br.com.ecociente.cadastro.dataprovaider.mapper.EnderecoPersistenceMapper;
import br.com.ecociente.cadastro.dataprovaider.repository.EnderecoRepository;

@Component 
public class EnderecoGatewayImpl implements EnderecoGateway  {

  private final EnderecoRepository enderecoRepository;
  private final EnderecoPersistenceMapper enderecoPersistenceMapper;

  public EnderecoGatewayImpl(EnderecoPersistenceMapper enderecoPersistenceMapper, EnderecoRepository enEnderecoRepository){
    this.enderecoPersistenceMapper=enderecoPersistenceMapper;
    this.enderecoRepository=enEnderecoRepository;
  }


  @Override
  public Endereco salvar(Endereco endereco) {
    EnderecoEntity entity = enderecoPersistenceMapper.toEntity(endereco);
    EnderecoEntity salvar = enderecoRepository.save(entity);
    return enderecoPersistenceMapper.toDomain(salvar);
  }

  @Override
  public Endereco buscarPorId(Integer idEndereco) {
    return enderecoRepository.findById(idEndereco).map(enderecoPersistenceMapper::toDomain).orElseThrow(() -> new NotFoundException("ENDERECO_NAO_ENCONTRADO",
      "Endereço não encontrado"
    ));
  }
  
}
