package br.com.ecociente.cadastro.core.Service;

import java.time.LocalDate;
import java.time.Period;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.input.CadastroUsuarioComumUseCase;
import br.com.ecociente.cadastro.core.usecase.output.CadastroUsuarioComumOutput;

@Service 
public class CadastrarUsuarioComumService implements CadastroUsuarioComumUseCase {

  private final UsuarioGateway usuarioGateway;
  private final EnderecoGateway enderecoGateway;
  private final CepGateway cepGateway;
  private final PasswordEncoder passwordEncoder;

  public CadastrarUsuarioComumService(
    UsuarioGateway usuarioGateway,
    EnderecoGateway enderecoGateway,
    CepGateway cepGateway,
    PasswordEncoder passwordEncoder
  ){
    this.usuarioGateway = usuarioGateway;
    this.enderecoGateway = enderecoGateway;
    this.cepGateway = cepGateway;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional 
  public CadastroUsuarioComumOutput executar(String nomeCompleto, String email, String senha, LocalDate dataNascimento, String cpf,
      String cep, String numero, String complemento) {
    
    validarDataNascimento(dataNascimento);
    String emaiLimpo = email.trim().toLowerCase();
    String cpfLimpo = cpf.replaceAll("\\D","");

    if (usuarioGateway.existePorEmail(emaiLimpo)) {
      throw new AlreadyExistsException ("EMAIL_ALREADY_EXISTS", "Email já cadastrado");
    }
    if (usuarioGateway.existePorCpf(cpfLimpo)) {
      throw new AlreadyExistsException("CPF_ALREADY_EXISTS", "CPF já cadastrado");
    }
    Endereco enderecoCep = cepGateway.buscarEnderecoPorCep(cep);
    Endereco enderecoSalvo = enderecoGateway.salvar(new Endereco(
      null, enderecoCep.getCep(), enderecoCep.getEstado(), enderecoCep.getCidade(),
      enderecoCep.getLogradouro(), numero,complemento
    ));

    String senhaHash = passwordEncoder.encode(senha);
    Usuario usuario = new Usuario(
      null, nomeCompleto, emaiLimpo, senhaHash, dataNascimento, cpfLimpo,
      null,true,OffsetDateTime.now(),1,enderecoSalvo.getIdEndereco());
    Usuario usuarioSalvo = usuarioGateway.salvar(usuario);
    
    return new CadastroUsuarioComumOutput(usuarioSalvo, enderecoSalvo);
  }

  private void validarDataNascimento(LocalDate data){
    if (data.isAfter(LocalDate.now())) {
      throw new BusinessException("VALIDATION_ERROR", "Data de nascimento não pode ser futura");
    }
    if (Period.between(data, LocalDate.now()).getYears()<18) {
      throw new BusinessException("VALIDATION_ERROR", "Menor de idade");
    }
  }

  

  
}
