package br.com.ecociente.cadastro.core.Service;

import java.time.OffsetDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.core.gateway.CooperativaGateway;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.input.CadastrarCooperativaUseCase;
import br.com.ecociente.cadastro.core.usecase.output.CadastroCooperativaOutput;

@Service 
public class CadastrarCooperativaService implements CadastrarCooperativaUseCase {
  private final UsuarioGateway usuarioGateway;
  private final CooperativaGateway cooperativaGateway;
  private final EnderecoGateway enderecoGateway;
  private final CepGateway cepGateway;
  private final PasswordEncoder passwordEncoder;

  public CadastrarCooperativaService(
    UsuarioGateway usuarioGateway,
    CooperativaGateway cooperativaGateway,
    EnderecoGateway enderecoGateway,
    CepGateway cepGateway,
    PasswordEncoder passwordEncoder
  ){
    this.usuarioGateway = usuarioGateway;
    this.cooperativaGateway = cooperativaGateway;
    this.enderecoGateway = enderecoGateway;
    this.cepGateway = cepGateway;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional 
  public CadastroCooperativaOutput executar(String nomeResponsavel, String email, String senha, String nomeCooperativa, String cnpj,
      String emailCooperativa, String telefone, String cep, String numero, String complemento) {
        String emailLimpo = email.trim().toLowerCase();
        String cnpjLimpo = cnpj.replaceAll("\\D","");

    if (usuarioGateway.existePorEmail(emailLimpo)) {
      throw new AlreadyExistsException ("EMAIL_ALREADY_EXISTS", "Email já cadastrado");
    }
    if (cooperativaGateway.existePorCnpj(cnpjLimpo)) {
      throw new AlreadyExistsException ("CNPJ_ALREADY_EXISTS", "CPF já cadastrado");
    }

    Endereco enderecoCep = cepGateway.buscarEnderecoPorCep(cep);
    Endereco enderecoSalvo = enderecoGateway.salvar(new Endereco(
      null,enderecoCep.getCep(),enderecoCep.getEstado(),enderecoCep.getCidade(),
      enderecoCep.getLogradouro(),numero,complemento
    ));

    String senhaHash = passwordEncoder.encode(senha);
    Usuario usuario = new Usuario(
      null, nomeResponsavel, emailLimpo, senhaHash, null,null,
      null,true, OffsetDateTime.now(), 6, enderecoSalvo.getIdEndereco()
    );
    Usuario usuarioSalvo = usuarioGateway.salvar(usuario);

    Cooperativa cooperativa = new Cooperativa(
      null, cnpjLimpo,nomeCooperativa, emailCooperativa.trim().toLowerCase(),
      telefone.replaceAll("\\D",""),OffsetDateTime.now(), usuarioSalvo.getIdUsuario(),enderecoSalvo.getIdEndereco()
    );

    Cooperativa cooperativaSalva = cooperativaGateway.salvar(cooperativa);
    return new CadastroCooperativaOutput(usuarioSalvo, cooperativaSalva, enderecoSalvo);   
  }

  
  
}
