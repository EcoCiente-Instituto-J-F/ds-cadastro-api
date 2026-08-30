package br.com.ecociente.cadastro.core.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.core.domain.Morador;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.gateway.CondominioGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.CadastrarMoradorUseCase;

@Service 
public class CadastrarMoradorService implements CadastrarMoradorUseCase {
  private final UsuarioGateway usuarioGateway;
  private final CondominioGateway condominioGateway;
  private final PasswordEncoder passwordEncoder;

  public CadastrarMoradorService(
    UsuarioGateway usuarioGateway,
    CondominioGateway condominioGateway,
    PasswordEncoder passwordEncoder
  ){
    this.usuarioGateway = usuarioGateway;
    this.condominioGateway = condominioGateway;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional 
  public Usuario executar(String nomeCompleto, String email, String senha, LocalDate dataNascimento, String cpf,
      String codigoCondominio) {
      validarDataNascimento(dataNascimento);
      String emailLimpo = email.trim().toLowerCase();
      String cpfLimpo = cpf.replaceAll("\\D","");

      if (usuarioGateway.existePorEmail(emailLimpo)) {
      throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email já cadastrado");
    }
    if (usuarioGateway.existePorCpf(cpfLimpo)) {
      throw new  BusinessException("CPF_ALREADY_EXISTS", "CPF já cadastrado");
    }

    Condominio condominio = condominioGateway.buscarPorCodigoAcesso(codigoCondominio.trim().toUpperCase()).orElseThrow(() -> new NotFoundException("CONDOMINIUM_NOT_FOUND","Código de condomínio não encontrado"));
    
    int tipoUsuarioId = (condominio.getTipoCondominioId() != null && condominio.getTipoCondominioId() == 1)?4:5;

    String senhaHash = passwordEncoder.encode(senha);
    Usuario usuario = new Usuario(
      null, nomeCompleto, emailLimpo, senhaHash, dataNascimento, cpfLimpo,
      null, true, null, tipoUsuarioId, null
    );

    Usuario usuarioSalvo = usuarioGateway.salvar(usuario);

    Morador vinculoMorador = new Morador(
      null,usuarioSalvo.getIdUsuario(),condominio.getIdCondominio(),
      null,null,true,null,1,BigDecimal.ZERO,0,0,0,null);

      condominioGateway.vincularMorador(vinculoMorador);

      return usuarioSalvo;
  }

  private void validarDataNascimento(LocalDate data){
    if (data.isAfter(LocalDate.now())) {
      throw new BusinessException("VALIDATION_ERROR", "Data de nascimento não pode ser futura");
    }
    if (Period.between(data, LocalDate.now()).getYears()<18) {
      throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email já cadastrado");
    }
  }

  
  
}
