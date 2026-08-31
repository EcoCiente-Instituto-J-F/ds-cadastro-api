package br.com.ecociente.cadastro.entrypoint.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import br.com.ecociente.cadastro.core.domain.Cooperativa;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.entrypoint.dto.response.ComumCadastroResponseDto;
import br.com.ecociente.cadastro.entrypoint.dto.response.CooperativaCadastroResponseDto;
import br.com.ecociente.cadastro.entrypoint.dto.response.MoradorCadastroResponseDto;

class UsuarioMapperTest {

  private UsuarioMapper mapper;

  private Usuario usuario;
  private Endereco endereco;
  private Cooperativa cooperativa;

  @BeforeEach
  void setUp() {
    mapper = new UsuarioMapper();

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
  @DisplayName("toComumCadastroResponse")
  class ToComumCadastroResponse {

    @Test
    @DisplayName("Deve mapear usuário e endereço para DTO de resposta")
    void shouldMapToComumCadastroResponse() {
      ComumCadastroResponseDto dto = mapper.toComumCadastroResponse(usuario, endereco);

      assertNotNull(dto);
      assertEquals(10, dto.id());
      assertEquals("comum", dto.tipo());
      assertEquals("Emanuelly Mendes", dto.nomeCompleto());
      assertEquals("emanuelly@gmail.com", dto.email());
      assertEquals("63082449026", dto.cpf());
      assertNotNull(dto.endereco());
      assertEquals("05280130", dto.endereco().cep());
      assertEquals("SP", dto.endereco().estado());
      assertEquals("São Paulo", dto.endereco().cidade());
      assertEquals("Rua das Flores", dto.endereco().logradouro());
      assertEquals("41", dto.endereco().numero());
      assertEquals("Apto 02", dto.endereco().complemento());
    }
  }

  @Nested
  @DisplayName("toMoradorCadastroResponse")
  class ToMoradorCadastroResponse {

    @Test
    @DisplayName("Deve mapear usuário, condomínio e endereço para DTO de resposta")
    void shouldMapToMoradorCadastroResponse() {
      MoradorCadastroResponseDto dto = mapper.toMoradorCadastroResponse(
          usuario, "COD123", "Condomínio Verde", endereco);

      assertNotNull(dto);
      assertEquals(10, dto.id());
      assertEquals("morador", dto.tipo());
      assertEquals("Emanuelly Mendes", dto.nomeCompleto());
      assertNotNull(dto.condominio());
      assertEquals("COD123", dto.condominio().codigo());
      assertEquals("Condomínio Verde", dto.condominio().nome());
      assertEquals("05280130", dto.condominio().enderecoDto().cep());
    }
  }

  @Nested
  @DisplayName("toCooperativaCadastroResponse")
  class ToCooperativaCadastroResponse {

    @Test
    @DisplayName("Deve mapear usuário, cooperativa e endereço para DTO de resposta")
    void shouldMapToCooperativaCadastroResponse() {
      CooperativaCadastroResponseDto dto = mapper.toCooperativaCadastroResponse(
          usuario, cooperativa.getNomeCoop(), cooperativa.getCnpjCoop(),
          cooperativa.getEmailCoop(), cooperativa.getTelefoneCoop(), endereco);

      assertNotNull(dto);
      assertEquals(10, dto.id());
      assertEquals("cooperativa", dto.tipo());
      assertEquals("Emanuelly Mendes", dto.nomeResponsavel());
      assertNotNull(dto.cooperativa());
      assertEquals("EcoCoop", dto.cooperativa().nome());
      assertEquals("54934758000126", dto.cooperativa().cnpj());
      assertEquals("ecocoop@gmail.com", dto.cooperativa().email());
      assertEquals("11953762717", dto.cooperativa().telefone());
      assertEquals("05280130", dto.cooperativa().endreco().cep());
    }
  }
}