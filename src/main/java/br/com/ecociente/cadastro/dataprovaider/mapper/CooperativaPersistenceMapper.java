package br.com.ecociente.cadastro.dataprovaider.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.dataprovaider.entity.CooperativaEntity;

@Component 
public class CooperativaPersistenceMapper {

  public CooperativaEntity toEntity(Cooperativa cooperativa){
    return CooperativaEntity.builder()
    .idCooperativa(cooperativa.getIdCoop())
    .cnpjCooperativa(cooperativa.getCnpjCoop())
    .nomeCooperativa(cooperativa.getNomeCoop())
    .emailCooperativa(cooperativa.getEmailCoop())
    .telefoneCooperativa(cooperativa.getTelefoneCoop())
    .dataCadastro(cooperativa.getDataCadastro())
    .userId(cooperativa.getUsuarioId())
    .enderecoId(cooperativa.getEnderecoId())
    .build();
  }

  public Cooperativa toDomain(CooperativaEntity cooperativaEntity){
    return Cooperativa.builder()
    .idCoop(cooperativaEntity.getIdCooperativa())
    .cnpjCoop(cooperativaEntity.getCnpjCooperativa())
    .nomeCoop(cooperativaEntity.getNomeCooperativa())
    .emailCoop(cooperativaEntity.getEmailCooperativa())
    .telefoneCoop(cooperativaEntity.getTelefoneCooperativa())
    .dataCadastro(cooperativaEntity.getDataCadastro())
    .usuarioId(cooperativaEntity.getUserId())
    .enderecoId(cooperativaEntity.getEnderecoId())
    .build();
    
  }
  
}
