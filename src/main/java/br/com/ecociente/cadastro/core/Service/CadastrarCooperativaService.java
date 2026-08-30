package br.com.ecociente.cadastro.core.Service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.core.gateway.CooperativaGateway;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.CadastrarCooperativaUseCase;

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
  public Cooperativa executar(String nomeResponsavel, String email, String senha, String nomeCooperativa, String cnpj,
      String emailCooperativa, String telefone, String cep, String numero, String complemento) {
        String emailLimpo = email.trim().toLowerCase();
        String cnpjLimpo = cnpj.replaceAll("\\D","");

    if (usuarioGateway.existePorEmail(emailLimpo)) {
      throw new BusinessException("EMAIL_ALREADY_EXISTS", "Email já cadastrado");
    }
    if (usuarioGateway.existePorCpf(cpfLimpo)) {
      throw new  BusinessException("CPF_ALREADY_EXISTS", "CPF já cadastrado");
    }

    Endereco enderecoCep = cepGateway.buscarEnderecoPorCep(cep);
    Endereco enderecoSalvo = enderecoGateway.salvar(new Endereco(
      null,enderecoCep.getCep(),enderecoCep.getEstado(),enderecoCep.getCidade(),
      enderecoCep.getLogradouro(),numero,complemento
    ));

    String senhaHash = passwordEncoder.encode(senha);
    Usuario usuario = new Usuario(
      null, nomeResponsavel, emailLimpo, senhaHash, null,null,
      null,true, null, 6, null
    );
    Usuario usuarioSalvo = usuarioGateway.salvar(usuario);

    Cooperativa cooperativa = new Cooperativa(
      null, cnpjLimpo,nomeCooperativa, emailCooperativa.trim().toLowerCase(),
      telefone.replaceAll("\\D",""),null, usuarioSalvo.getIdUsuario(),enderecoSalvo.getIdEndereco()
    );

    return cooperativaGateway.salvar(cooperativa);
        
  }

  
  
}
