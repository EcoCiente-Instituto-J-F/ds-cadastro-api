package br.com.ecociente.cadastro.dataprovaider.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.ecociente.cadastro.dataprovaider.entity.UserEntity;

public interface UsuarioRepository extends JpaRepository<UserEntity,Integer> {
  Optional<UserEntity> findByEmailUsuario(String emailUsuario);
  boolean existsByEmailUsuario(String emailUsuario);
  boolean existsByCpf(String cpf);
  
}