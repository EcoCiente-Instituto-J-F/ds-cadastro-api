package br.com.ecociente.cadastro.dataprovaider.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecociente.cadastro.dataprovaider.entity.CooperativaEntity;

public interface CooperativaRepository extends JpaRepository<CooperativaEntity, Integer> {
  boolean existsByCnpjCooperativa(String cnpj);
  Optional<CooperativaEntity> findByEmailCooperativa(String email);
}
