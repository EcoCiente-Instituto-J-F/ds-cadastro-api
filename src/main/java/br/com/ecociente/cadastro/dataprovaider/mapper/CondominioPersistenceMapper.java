package br.com.ecociente.cadastro.dataprovaider.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.dataprovaider.entity.CondominioEntity;

@Component 
public class CondominioPersistenceMapper {

  public CondominioEntity toEntity(Condominio condominio){
    return CondominioEntity.builder()
    .idCondominio(condominio.getIdCondominio())
    .nomeCondominio(condominio.getNomeCondominio())
    .cnpj(condominio.getCnpj())
    .codigoAcesso(condominio.getCodigoAcesso())
    .ativo(condominio.getAtivo())
    .tipoCondominioId(condominio.getTipoCondominioId())
    .enderecoId(condominio.getEnderecoId())
    .build();
  }

  public Condominio toDomain(CondominioEntity condominio){
    return Condominio.builder()
    .idCondominio(condominio.getIdCondominio())
    .nomeCondominio(condominio.getNomeCondominio())
    .cnpj(condominio.getCnpj())
    .codigoAcesso(condominio.getCodigoAcesso())
    .ativo(condominio.getAtivo())
    .tipoCondominioId(condominio.getTipoCondominioId())
    .enderecoId(condominio.getEnderecoId())
    .build();
  }

  
}
