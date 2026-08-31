package br.com.ecociente.cadastro.entrypoint.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import br.com.ecociente.cadastro.config.SecurityConfig;
import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.exception.NotFoundException;
import br.com.ecociente.cadastro.core.usecase.input.CadastrarCooperativaUseCase;
import br.com.ecociente.cadastro.core.usecase.input.CadastrarMoradorUseCase;
import br.com.ecociente.cadastro.core.usecase.input.CadastroUsuarioComumUseCase;
import br.com.ecociente.cadastro.core.usecase.output.CadastroCooperativaOutput;
import br.com.ecociente.cadastro.core.usecase.output.CadastroMoradorOutput;
import br.com.ecociente.cadastro.core.usecase.output.CadastroUsuarioComumOutput;
import br.com.ecociente.cadastro.entrypoint.exception.GlobalExceptionHandler;
import br.com.ecociente.cadastro.entrypoint.mapper.UsuarioMapper;

@WebMvcTest(controllers = CadastroController.class)
@Import({
    SecurityConfig.class,
    GlobalExceptionHandler.class,
    UsuarioMapper.class
})
class CadastroControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CadastroUsuarioComumUseCase cadastroUsuarioComumUseCase;

  @MockitoBean
  private CadastrarMoradorUseCase cadastrarMoradorUseCase;

  @MockitoBean
  private CadastrarCooperativaUseCase cadastrarCooperativaUseCase;

  private Usuario usuario;
  private Endereco endereco;
  private Cooperativa cooperativa;

  @BeforeEach
  void setUp() {

    usuario = Usuario.builder()
        .idUsuario(10)
        .nomeUsuario("Emanuelly Mendes")
        .emaiUsuario("emanuelly@gmail.com")
        .dataNascimento(LocalDate.of(2000, 3, 12))
        .cpf("63082449026")
        .registroEm(OffsetDateTime.now())
        .build();

    endereco = Endereco.builder()
        .idEndereco(1)
        .cep("05280130")
        .estado("SP")
        .cidade("São Paulo")
        .logradouro("Rua das Flores")
        .numero("41")
        .complemento("Apto 02")
        .build();

    cooperativa = Cooperativa.builder()
        .idCoop(20)
        .cnpjCoop("54934758000126")
        .nomeCoop("EcoCoop")
        .emailCoop("ecocoop@gmail.com")
        .telefoneCoop("11953762717")
        .build();
  }

  @Nested
  @DisplayName("POST /api/cadastro/comum")
  class CadastrarComum {

    @Test
    @DisplayName("Deve retornar 201 quando cadastro de usuário comum for bem-sucedido")
    void shouldReturn201WhenCadastroComumSucesso() throws Exception {

      when(cadastroUsuarioComumUseCase.executar(
          any(), any(), any(), any(),
          any(), any(), any(), any())).thenReturn(
              new CadastroUsuarioComumOutput(usuario, endereco));

      mockMvc.perform(
          post("/api/cadastro/comum")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "cep": "05280130",
                          "numero": "41",
                          "complemento": "Apto 02"
                      }
                  """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(10))
          .andExpect(jsonPath("$.tipo").value("comum"))
          .andExpect(jsonPath("$.nomeCompleto").value("Emanuelly Mendes"))
          .andExpect(jsonPath("$.email").value("emanuelly@gmail.com"))
          .andExpect(jsonPath("$.endereco.cep").value("05280130"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando email estiver ausente")
    void shouldReturn400WhenEmailAusente() throws Exception {

      mockMvc.perform(
          post("/api/cadastro/comum")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando senha não atender aos critérios")
    void shouldReturn400WhenSenhaFraca() throws Exception {

      mockMvc.perform(
          post("/api/cadastro/comum")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "123",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando CPF for inválido")
    void shouldReturn400WhenCpfInvalido() throws Exception {

      mockMvc.perform(
          post("/api/cadastro/comum")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "123",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando use case lançar AlreadyExistsException")
    void shouldReturn404WhenAlreadyExists() throws Exception {

      when(cadastroUsuarioComumUseCase.executar(
          any(), any(), any(), any(),
          any(), any(), any(), any())).thenThrow(
              new AlreadyExistsException(
                  "EMAIL_ALREADY_EXISTS",
                  "Email já cadastrado"));

      mockMvc.perform(
          post("/api/cadastro/comum")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.codigoError").value("EMAIL_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("Deve retornar 500 quando use case lançar erro genérico")
    void shouldReturn500WhenGenericError() throws Exception {

      when(cadastroUsuarioComumUseCase.executar(
          any(), any(), any(), any(),
          any(), any(), any(), any())).thenThrow(
              new RuntimeException("Erro interno"));

      mockMvc.perform(
          post("/api/cadastro/comum")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isInternalServerError())
          .andExpect(jsonPath("$.codigoError").value("ERRO_INTERNO"));
    }
  }

  @Nested
  @DisplayName("POST /api/cadastro/morador")
  class CadastrarMorador {

    @Test
    @DisplayName("Deve retornar 201 quando cadastro de morador for bem-sucedido")
    void shouldReturn201WhenCadastroMoradorSucesso() throws Exception {

      when(cadastrarMoradorUseCase.executar(
          any(), any(), any(),
          any(), any(), any())).thenReturn(
              new CadastroMoradorOutput(
                  usuario,
                  "COD123",
                  "Condomínio Verde",
                  endereco));

      mockMvc.perform(
          post("/api/cadastro/morador")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "codigoCondominio": "COD123"
                      }
                  """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(10))
          .andExpect(jsonPath("$.tipo").value("morador"))
          .andExpect(jsonPath("$.condominio.codigo").value("COD123"))
          .andExpect(jsonPath("$.condominio.nome").value("Condomínio Verde"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando código do condomínio estiver ausente")
    void shouldReturn400WhenCodigoCondominioAusente() throws Exception {

      mockMvc.perform(
          post("/api/cadastro/morador")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeUsuario": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "dataNascimento": "2000-03-12",
                          "cpf": "630.824.490-26",
                          "codigoCondominio": ""
                      }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
    }

    @Test
    @DisplayName("Deve retornar 404 quando condomínio não encontrado")
    void shouldReturn404WhenCondominioNotFound() throws Exception {

      when(cadastrarMoradorUseCase.executar(
          any(), any(), any(),
          any(), any(), any())).thenThrow(
              new NotFoundException(
                  "CONDOMINIUM_NOT_FOUND",
                  "Código de condomínio não encontrado"));

      mockMvc.perform(post("/api/cadastro/morador")
          .contentType(MediaType.APPLICATION_JSON)
          .content("""
              {
                "nomeUsuario": "Emanuelly Mendes",
                "email": "emanuelly@gmail.com",
                "senha": "Senha@T3ste",
                "dataNascimento": "2000-03-12",
                "cpf": "630.824.490-26",
                "codigoCondominio": "COD999"
              }
              """))
          .andExpect(status().isNotFound())
          .andExpect(jsonPath("$.codigoError").value("CONDOMINIUM_NOT_FOUND"));
    }
  }

  @Nested
  @DisplayName("POST /api/cadastro/cooperativa")
  class CadastrarCooperativa {

    @Test
    @DisplayName("Deve retornar 201 quando cadastro de cooperativa for bem-sucedido")
    void shouldReturn201WhenCadastroCooperativaSucesso() throws Exception {

      when(cadastrarCooperativaUseCase.executar(
          any(), any(), any(),
          any(), any(), any(),
          any(), any(), any(), any())).thenReturn(
              new CadastroCooperativaOutput(
                  usuario,
                  cooperativa,
                  endereco));

      mockMvc.perform(
          post("/api/cadastro/cooperativa")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeResponsavel": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "nomeCooperativa": "EcoCoop",
                          "cnpj": "54934758000126",
                          "emailCooperativa": "ecocoop@gmail.com",
                          "telefone": "11953762717",
                          "cep": "05280130",
                          "numero": "41",
                          "complemento": "Apto 01"
                      }
                  """))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.id").value(10))
          .andExpect(jsonPath("$.tipo").value("cooperativa"))
          .andExpect(jsonPath("$.cooperativa.nome").value("EcoCoop"))
          .andExpect(jsonPath("$.cooperativa.cnpj").value("54934758000126"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando CNPJ não tiver 14 dígitos")
    void shouldReturn400WhenCnpjInvalido() throws Exception {

      mockMvc.perform(
          post("/api/cadastro/cooperativa")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeResponsavel": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "nomeCooperativa": "EcoCoop",
                          "cnpj": "123",
                          "emailCooperativa": "ecocoop@gmail.com",
                          "telefone": "11953762717",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
    }

    @Test
    @DisplayName("Deve retornar 400 quando telefone não tiver 10 ou 11 dígitos")
    void shouldReturn400WhenTelefoneInvalido() throws Exception {

      mockMvc.perform(
          post("/api/cadastro/cooperativa")
              .contentType(MediaType.APPLICATION_JSON)
              .content("""
                      {
                          "nomeResponsavel": "Emanuelly Mendes",
                          "email": "emanuelly@gmail.com",
                          "senha": "Senha@T3ste",
                          "nomeCooperativa": "EcoCoop",
                          "cnpj": "54934758000126",
                          "emailCooperativa": "ecocoop@gmail.com",
                          "telefone": "123",
                          "cep": "05280130",
                          "numero": "41"
                      }
                  """))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
    }
  }
}