package br.com.ecociente.cadastro.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.ecociente.cadastro.core.Service.CadastrarCooperativaService;
import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.core.gateway.CooperativaGateway;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.output.CadastroCooperativaOutput;

@ExtendWith(MockitoExtension.class)
class CadastrarCooperativaServiceTest {

  @Mock
  private UsuarioGateway usuarioGateway;

  @Mock
  private CooperativaGateway cooperativaGateway;

  @Mock
  private EnderecoGateway enderecoGateway;

  @Mock
  private CepGateway cepGateway;

  @Mock
  private PasswordEncoder passwordEncoder;

  @InjectMocks
  private CadastrarCooperativaService service;

  private Endereco enderecoCep;
  private Endereco enderecoSalvo;
  private Usuario usuarioSalvo;
  private Cooperativa cooperativaSalva;

  @BeforeEach
  void setUp() {
    enderecoCep = Endereco.builder()
        .cep("05280130")
        .estado("SP")
        .cidade("São Paulo")
        .logradouro("Rua das Flores")
        .build();

    enderecoSalvo = Endereco.builder()
        .idEndereco(1)
        .cep("05280130")
        .estado("SP")
        .cidade("São Paulo")
        .logradouro("Rua das Flores")
        .numero("41")
        .complemento("Apto 01")
        .build();

    usuarioSalvo = Usuario.builder()
        .idUsuario(10)
        .nomeUsuario("Emanuelly Mendes")
        .emaiUsuario("emanuelly@gmail.com")
        .senhaHash("hash")
        .ativo(true)
        .registroEm(OffsetDateTime.now())
        .tipoUsuarioId(6)
        .enderecoId(1)
        .build();

    cooperativaSalva = Cooperativa.builder()
        .idCoop(20)
        .cnpjCoop("54934758000126")
        .nomeCoop("EcoCoop")
        .emailCoop("ecocoop@gmail.com")
        .telefoneCoop("11953762717")
        .dataCadastro(OffsetDateTime.now())
        .usuarioId(10)
        .enderecoId(1)
        .build();
  }

  @Nested
  @DisplayName("executar - Cadastro de cooperativa")
  class Executar {

    @Test
    @DisplayName("Deve cadastrar cooperativa com sucesso quando dados válidos")
    void shouldCadastrarCooperativaComSucesso() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
      when(cooperativaGateway.existePorCnpj(anyString())).thenReturn(false);
      when(cepGateway.buscarEnderecoPorCep(anyString())).thenReturn(enderecoCep);
      when(enderecoGateway.salvar(any(Endereco.class))).thenReturn(enderecoSalvo);
      when(passwordEncoder.encode(anyString())).thenReturn("hash");
      when(usuarioGateway.salvar(any(Usuario.class))).thenReturn(usuarioSalvo);
      when(cooperativaGateway.salvar(any(Cooperativa.class))).thenReturn(cooperativaSalva);

      CadastroCooperativaOutput resultado = service.executar(
          "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
          "EcoCoop", "54934758000126", "ecocoop@gmail.com",
          "11953762717", "05280130", "41", "Apto 01");

      assertNotNull(resultado);
      assertEquals(10, resultado.usuario().getIdUsuario());
      assertEquals(20, resultado.cooperativa().getIdCoop());
      assertEquals("EcoCoop", resultado.cooperativa().getNomeCoop());
      verify(usuarioGateway).salvar(any(Usuario.class));
      verify(cooperativaGateway).salvar(any(Cooperativa.class));
    }

    @Test
    @DisplayName("Deve lançar AlreadyExistsException quando email já cadastrado")
    void shouldThrowWhenEmailAlreadyExists() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(true);

      AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              "EcoCoop", "54934758000126", "ecocoop@gmail.com",
              "11953762717", "05280130", "41", "Apto 01"));

      assertEquals("EMAIL_ALREADY_EXISTS", ex.getCodigoErro());
      verify(cooperativaGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar AlreadyExistsException quando CNPJ já cadastrado")
    void shouldThrowWhenCnpjAlreadyExists() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
      when(cooperativaGateway.existePorCnpj(anyString())).thenReturn(true);

      AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              "EcoCoop", "54934758000126", "ecocoop@gmail.com",
              "11953762717", "05280130", "41", "Apto 01"));

      assertEquals("CNPJ_ALREADY_EXISTS", ex.getCodigoErro());
      verify(usuarioGateway, never()).salvar(any());
      verify(cooperativaGateway, never()).salvar(any());
    }
  }
}