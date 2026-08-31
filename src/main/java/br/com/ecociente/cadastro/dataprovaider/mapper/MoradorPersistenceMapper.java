package br.com.ecociente.cadastro.dataprovaider.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Morador;
import br.com.ecociente.cadastro.dataprovaider.entity.MoradorEntity;

@Component 
public class MoradorPersistenceMapper {

  public MoradorEntity toEntity(Morador morador){
    return MoradorEntity.builder()
    .idMorador(morador.getIdMorador())
    .usuarioId(morador.getUsuarioId())
    .condominioId(morador.getCondominioId())
    .dataEntrada(morador.getDataEntrada())
    .dataSaida(morador.getDataSaida())
    .aprovado(morador.getAprovado())
    .build();
  }

  public Morador toDomain(MoradorEntity moradorEntity){
    return Morador.builder()
    .idMorador(moradorEntity.getIdMorador())
    .usuarioId(moradorEntity.getUsuarioId())
    .condominioId(moradorEntity.getCondominioId())
    .dataEntrada(moradorEntity.getDataEntrada())
    .dataSaida(moradorEntity.getDataSaida())
    .aprovado(moradorEntity.getAprovado())
    .build();
  }
  
}
