package br.com.ecociente.cadastro.dataprovaider.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecociente.cadastro.dataprovaider.entity.UsuarioComumEntity;

public interface UsuarioComumRepository extends JpaRepository<UsuarioComumEntity, Integer> {

  
}