package br.com.ecociente.cadastro.core.service;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.ecociente.cadastro.core.Service.CadastrarMoradorService;
import br.com.ecociente.cadastro.core.domain.Condominio;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Morador;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.exception.NotFoundException;
import br.com.ecociente.cadastro.core.gateway.CondominioGateway;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.output.CadastroMoradorOutput;

@ExtendWith(MockitoExtension.class)
class CadastrarMoradorServiceTest {

  @Mock
  private UsuarioGateway usuarioGateway;

  @Mock
  private CondominioGateway condominioGateway;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private EnderecoGateway enderecoGateway;

  @InjectMocks
  private CadastrarMoradorService service;

  private Condominio condominio;
  private Endereco enderecoCondominio;
  private Usuario usuarioSalvo;

  @BeforeEach
  void setUp() {
    condominio = Condominio.builder()
        .idCondominio(1)
        .nomeCondominio("Condomínio Verde")
        .codigoAcesso("COD123")
        .ativo(true)
        .tipoCondominioId(1)
        .enderecoId(5)
        .build();

    enderecoCondominio = Endereco.builder()
        .idEndereco(5)
        .cep("05280130")
        .estado("SP")
        .cidade("São Paulo")
        .logradouro("Rua das Flores")
        .numero("100")
        .build();

    usuarioSalvo = Usuario.builder()
        .idUsuario(10)
        .nomeUsuario("Emanuelly Mendes")
        .emaiUsuario("emanuelly@gmail.com")
        .senhaHash("hash")
        .dataNascimento(LocalDate.of(2000, 3, 12))
        .cpf("63082449026")
        .ativo(true)
        .registroEm(OffsetDateTime.now())
        .tipoUsuarioId(4)
        .enderecoId(5)
        .build();
  }

  @Nested
  @DisplayName("executar - Cadastro de morador")
  class Executar {

    @Test
    @DisplayName("Deve cadastrar morador com sucesso quando dados válidos")
    void shouldCadastrarMoradorComSucesso() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
      when(usuarioGateway.existePorCpf(anyString())).thenReturn(false);
      when(condominioGateway.buscarPorCodigoAcesso(anyString())).thenReturn(Optional.of(condominio));
      when(enderecoGateway.buscarPorId(any())).thenReturn(enderecoCondominio);
      when(passwordEncoder.encode(anyString())).thenReturn("hash");
      when(usuarioGateway.salvar(any(Usuario.class))).thenReturn(usuarioSalvo);
      when(condominioGateway.vincularMorador(any(Morador.class)))
          .thenReturn(Morador.builder().idMorador(1).build());

      CadastroMoradorOutput resultado = service.executar(
          "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
          LocalDate.of(2000, 3, 12), "630.824.490-26", "COD123");

      assertNotNull(resultado);
      assertEquals(10, resultado.usuario().getIdUsuario());
      assertEquals("COD123", resultado.codigoCondominio());
      assertEquals("Condomínio Verde", resultado.nomeCondominio());
      verify(usuarioGateway).salvar(any(Usuario.class));
      verify(condominioGateway).vincularMorador(any(Morador.class));
    }

    @Test
    @DisplayName("Deve lançar AlreadyExistsException quando email já cadastrado")
    void shouldThrowWhenEmailAlreadyExists() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(true);

      AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              LocalDate.of(2000, 3, 12), "630.824.490-26", "COD123"));

      assertEquals("EMAIL_ALREADY_EXISTS", ex.getCodigoErro());
      verify(condominioGateway, never()).buscarPorCodigoAcesso(anyString());
    }

    @Test
    @DisplayName("Deve lançar AlreadyExistsException quando CPF já cadastrado")
    void shouldThrowWhenCpfAlreadyExists() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
      when(usuarioGateway.existePorCpf(anyString())).thenReturn(true);

      AlreadyExistsException ex = assertThrows(AlreadyExistsException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              LocalDate.of(2000, 3, 12), "630.824.490-26", "COD123"));

      assertEquals("CPF_ALREADY_EXISTS", ex.getCodigoErro());
      verify(condominioGateway, never()).buscarPorCodigoAcesso(anyString());
    }

    @Test
    @DisplayName("Deve lançar NotFoundException quando código de condomínio não encontrado")
    void shouldThrowWhenCondominioNotFound() {
      when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
      when(usuarioGateway.existePorCpf(anyString())).thenReturn(false);
      when(condominioGateway.buscarPorCodigoAcesso(anyString())).thenReturn(Optional.empty());

      NotFoundException ex = assertThrows(NotFoundException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              LocalDate.of(2000, 3, 12), "630.824.490-26", "COD999"));

      assertEquals("CONDOMINIUM_NOT_FOUND", ex.getCodigoErro());
      verify(usuarioGateway, never()).salvar(any());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando data de nascimento for futura")
    void shouldThrowWhenDataNascimentoFutura() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              LocalDate.now().plusDays(1), "630.824.490-26", "COD123"));

      assertEquals("VALIDATION_ERROR", ex.getCodigoErro());
      verify(usuarioGateway, never()).existePorEmail(anyString());
    }

    @Test
    @DisplayName("Deve lançar BusinessException quando menor de idade")
    void shouldThrowWhenMenorDeIdade() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> service.executar(
              "Emanuelly Mendes", "emanuelly@gmail.com", "Senha@T3ste",
              LocalDate.now().minusYears(17), "630.824.490-26", "COD123"));

      assertEquals("VALIDATION_ERROR", ex.getCodigoErro());
      verify(usuarioGateway, never()).existePorEmail(anyString());
    }
  }
}
