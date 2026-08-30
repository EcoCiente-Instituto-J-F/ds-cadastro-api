package br.com.ecociente.cadastro.dataprovaider.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecociente.cadastro.dataprovaider.entity.MoradorEntity;


public interface MoradorRepository extends JpaRepository<MoradorEntity,Integer>{
  
}
