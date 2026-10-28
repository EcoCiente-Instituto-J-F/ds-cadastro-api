package br.com.ecociente.cadastro.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Date;

import javax.crypto.SecretKey;
import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import org.testcontainers.postgresql.PostgreSQLContainer;

import br.com.ecociente.cadastro.config.SecurityConfig;
import br.com.ecociente.cadastro.config.security.JwtAuthenticationFilter;
import br.com.ecociente.cadastro.config.security.JwtService;
import br.com.ecociente.cadastro.core.Service.AtualizarUsuarioService;
import br.com.ecociente.cadastro.core.domain.AtualizacaoDados;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.exception.CepException;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.dataprovaider.gateway.AtualizacaoUsuarioGatewayImpl;
import br.com.ecociente.cadastro.entrypoint.controller.AtualizacaoUsuarioController;
import br.com.ecociente.cadastro.entrypoint.exception.GlobalExceptionHandler;
import br.com.ecociente.cadastro.entrypoint.mapper.AtualizacaoUsuarioMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@Tag("integracao")
@WebMvcTest(controllers = AtualizacaoUsuarioController.class, properties = {
    "app.security.jwt.secret=segredo-integracao-com-mais-de-32-caracteres",
    "spring.jackson.deserialization.fail-on-unknown-properties=true"
})
@Import({
    SecurityConfig.class,
    JwtAuthenticationFilter.class,
    JwtService.class,
    AtualizacaoUsuarioMapper.class,
    GlobalExceptionHandler.class,
    AtualizarUsuarioService.class,
    AtualizacaoUsuarioGatewayImpl.class,
    AtualizacaoUsuarioIntegrationTest.BancoConfig.class
})
@Sql("/sql/atualizacao-test.sql")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AtualizacaoUsuarioIntegrationTest {

  private static final String SECRET = "segredo-integracao-com-mais-de-32-caracteres";

  private static final String DADOS = "/api/usuarios/me";
  private static final String ENDERECO = "/api/usuarios/me/endereco";
  private static final String CONDOMINIO = "/api/condominios/100";

  private static final String ENDERECO_JSON = """
      {
          "cep": "05280-130",
          "numero": "41",
          "complemento": "Novo complemento"
      }
      """;

  private static final SecretKey KEY = Keys.hmacShaKeyFor(
      SECRET.getBytes(StandardCharsets.UTF_8));

  private MockMvc mvc;

  @Autowired
  private WebApplicationContext contexto;

  @Autowired
  private JwtService jwtService;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private AtualizarUsuarioService service;

  @MockitoBean
  private CepGateway cepGateway;


  @BeforeEach
  void configurarCep() {
    assertEquals(
        1,
        jwtService.extrairUsuarioId(token(1)),
        "O JWT gerado pelo teste deve ser aceito pelo JwtService real");

    mvc = MockMvcBuilders
        .webAppContextSetup(contexto)
        .apply(springSecurity())
        .build();

    when(cepGateway.buscarEnderecoPorCep("05280130")).thenReturn(
        new Endereco(
            null,
            "05280130",
            "SP",
            "São Paulo",
            "Rua das Flores",
            null,
            null));
  }

  private String token(int usuarioId) {
    return Jwts.builder()
        .subject("email-do-token@teste.com")
        .claim("usuarioId", usuarioId)
        .claim("perfil", "COOPERATIVA")
        .expiration(Date.from(Instant.now().plusSeconds(600)))
        .signWith(KEY)
        .compact();
  }

  private MockHttpServletRequestBuilder requisicao(
      String caminho,
      int usuarioId,
      String json) {
    return patch(caminho)
        .header("Authorization", "Bearer " + token(usuarioId))
        .contentType(MediaType.APPLICATION_JSON)
        .content(json);
  }

  private String texto(String sql) {
    return jdbc.queryForObject(sql, String.class);
  }

  private int inteiro(String sql) {
    return jdbc.queryForObject(sql, Integer.class);
  }

  private void usuarioUmPreservado() {
    assertEquals(
        "Usuário Um",
        texto("SELECT nome_usuario FROM tb_usuarios WHERE id_usuario = 1"));

    assertEquals(
        "um@teste.com",
        texto("SELECT email_usuario FROM tb_usuarios WHERE id_usuario = 1"));

    assertEquals(
        "11911111111",
        texto("SELECT numero_contato FROM tb_telefones WHERE id_telefone = 10"));
  }

  private void enderecoPreservado(int id) {
    assertEquals(
        "Rua Antiga",
        jdbc.queryForObject(
            "SELECT logradouro FROM tb_enderecos WHERE id_endereco = ?",
            String.class,
            id));
  }

  @Test
  void deveAtualizarDadosPermitidosSemAlterarOutroUsuarioOuCamposImutaveis()
      throws Exception {

    var antes = jdbc.queryForMap(
        """
            SELECT cpf, senha_hash, tipo_usuario_id,
                   endereco_id, ativo, registro_em
              FROM tb_usuarios
             WHERE id_usuario = 1
            """);

    mvc.perform(requisicao(DADOS, 1, """
        {
            "nome": "  Novo Nome  ",
            "email": "NOVO@TESTE.COM",
            "dataNascimento": "1995-02-03",
            "urlAvatar": "https://teste.com/novo.png",
            "telefone": "11999999999"
        }
        """))
        .andExpect(status().isNoContent())
        .andExpect(content().string(""));

    assertEquals(
        "Novo Nome",
        texto("SELECT nome_usuario FROM tb_usuarios WHERE id_usuario = 1"));

    assertEquals(
        "novo@teste.com",
        texto("SELECT email_usuario FROM tb_usuarios WHERE id_usuario = 1"));

    assertEquals(
        LocalDate.of(1995, 2, 3),
        jdbc.queryForObject(
            "SELECT data_nascimento FROM tb_usuarios WHERE id_usuario = 1",
            java.sql.Date.class).toLocalDate());

    assertEquals(
        "https://teste.com/novo.png",
        texto("SELECT url_avatar FROM tb_usuarios WHERE id_usuario = 1"));

    assertEquals(
        "11999999999",
        texto("SELECT numero_contato FROM tb_telefones WHERE id_telefone = 10"));

    assertEquals(
        "Usuário Dois",
        texto("SELECT nome_usuario FROM tb_usuarios WHERE id_usuario = 2"));

    var depois = jdbc.queryForMap(
        """
            SELECT cpf, senha_hash, tipo_usuario_id,
                   endereco_id, ativo, registro_em
              FROM tb_usuarios
             WHERE id_usuario = 1
            """);

    assertEquals(antes, depois);
  }

  @Test
  void devePreservarCamposOmitidosOuNulos() throws Exception {
    var antes = jdbc.queryForMap(
        """
            SELECT email_usuario, data_nascimento, url_avatar, cpf
              FROM tb_usuarios WHERE id_usuario = 1
            """);

    mvc.perform(requisicao(DADOS, 1, """
        {
            "nome": "Novo Nome",
            "email": null,
            "dataNascimento": null,
            "urlAvatar": null
        }
        """))
        .andExpect(status().isNoContent());

    var depois = jdbc.queryForMap(
        """
            SELECT email_usuario, data_nascimento, url_avatar, cpf
              FROM tb_usuarios WHERE id_usuario = 1
            """);

    assertEquals(antes, depois);
  }

  @Test
  void deveLimparAvatarComStringVazia() throws Exception {
    mvc.perform(requisicao(DADOS, 1, """
        {"urlAvatar": ""}
        """))
        .andExpect(status().isNoContent());

    assertEquals(
        "",
        texto("SELECT url_avatar FROM tb_usuarios WHERE id_usuario = 1"));
  }

  @Test
  void deveRejeitarAtualizacaoVazia() throws Exception {
    mvc.perform(requisicao(DADOS, 1, "{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("EMPTY_UPDATE"));

    usuarioUmPreservado();
  }

  @Test
  void deveDetectarEmailDuplicadoSemDiferenciarMaiusculas()
      throws Exception {

    mvc.perform(requisicao(DADOS, 1, """
        {"email": "OUTRO@TESTE.COM", "telefone": "11999999999"}
        """))
        .andExpect(status().isNotFound())
        .andExpect(
            jsonPath("$.codigoError").value("EMAIL_ALREADY_EXISTS"));

    usuarioUmPreservado();
  }

  @Test
  void devePermitirManterOProprioEmail() throws Exception {
    mvc.perform(requisicao(DADOS, 1, """
        {"email": "UM@TESTE.COM"}
        """))
        .andExpect(status().isNoContent());

    assertEquals(
        "um@teste.com",
        texto("SELECT email_usuario FROM tb_usuarios WHERE id_usuario = 1"));
  }

  @ParameterizedTest
  @ValueSource(ints = { 7, 999 })
  void deveRejeitarUsuarioInativoOuInexistente(int id) throws Exception {
    mvc.perform(requisicao(DADOS, id, """
        {"nome": "Tentativa"}
        """))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.codigoError").value("UPDATE_NOT_ALLOWED"));

    usuarioUmPreservado();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "/api/usuarios/me",
      "/api/usuarios/me/endereco",
      "/api/condominios/100/endereco"
  })
  void deveExigirJwtNosTresEndpoints(String caminho) throws Exception {
    mvc.perform(
        patch(caminho)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{}"))
        .andExpect(status().isUnauthorized());

    usuarioUmPreservado();
    enderecoPreservado(1);
    enderecoPreservado(3);
  }

  @Test
  void deveRejeitarJwtExpirado() throws Exception {
    String expirado = Jwts.builder()
        .claim("usuarioId", 1)
        .expiration(Date.from(Instant.now().minusSeconds(60)))
        .signWith(KEY)
        .compact();

    mvc.perform(
        patch(DADOS)
            .header("Authorization", "Bearer " + expirado)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nome": "Tentativa"}
                """))
        .andExpect(status().isUnauthorized());

    usuarioUmPreservado();
  }

  @Test
  void deveRejeitarJwtAssinadoComOutroSegredo() throws Exception {
    SecretKey outraChave = Keys.hmacShaKeyFor(
        "outra-chave-integracao-com-mais-de-32-caracteres"
            .getBytes(StandardCharsets.UTF_8));

    String invalido = Jwts.builder()
        .claim("usuarioId", 1)
        .expiration(Date.from(Instant.now().plusSeconds(600)))
        .signWith(outraChave)
        .compact();

    mvc.perform(
        patch(DADOS)
            .header("Authorization", "Bearer " + invalido)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nome": "Tentativa"}
                """))
        .andExpect(status().isUnauthorized());

    usuarioUmPreservado();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "Bearer invalido",
      "Bearer ",
      "Basic abc"
  })
  void deveRejeitarCredenciaisInvalidas(String header) throws Exception {
    mvc.perform(
        patch(DADOS)
            .header("Authorization", header)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {"nome": "Tentativa"}
                """))
        .andExpect(status().isUnauthorized());

    usuarioUmPreservado();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "cpf",
      "cnpj",
      "senha",
      "senhaHash",
      "tipoUsuarioId",
      "usuarioId",
      "enderecoId",
      "ativo"
  })
  void deveRejeitarCamposNaoPermitidos(String campo) throws Exception {
    String json = """
        {"nome": "Tentativa", "%s": "valor"}
        """.formatted(campo);

    mvc.perform(requisicao(DADOS, 1, json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("JSON_MALFORMADO"));

    usuarioUmPreservado();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"nome\":\"\"}",
      "{\"nome\":\"   \"}",
      "{\"email\":\"invalido\"}",
      "{\"email\":\"a @teste.com\"}",
      "{\"telefone\":\"123\"}",
      "{\"telefone\":\"1199999999a\"}",
      "{\"telefoneId\":0}",
      "{\"telefoneId\":-1}",
      "{\"urlAvatar\":\"ftp://teste.com/imagem\"}",
      "{\"dataNascimento\":\"2999-01-01\"}"
  })
  void deveRejeitarDadosInvalidosNoDto(String json) throws Exception {
    mvc.perform(requisicao(DADOS, 1, json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));

    usuarioUmPreservado();
  }

  @Test
  void deveRejeitarNomeAcimaDoLimite() throws Exception {
    mvc.perform(requisicao(
        DADOS,
        1,
        "{\"nome\":\"" + "a".repeat(101) + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));

    usuarioUmPreservado();
  }

  @Test
  void deveRejeitarJsonMalformado() throws Exception {
    mvc.perform(requisicao(DADOS, 1, "{\"nome\":"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("JSON_MALFORMADO"));

    usuarioUmPreservado();
  }

  @Test
  void deveRejeitarDataQueNaoPodeSerInterpretada() throws Exception {
    mvc.perform(requisicao(DADOS, 1, """
        {"dataNascimento": "isso-nao-e-data"}
        """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("JSON_MALFORMADO"));

    usuarioUmPreservado();
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"cep\":\"123\",\"numero\":\"41\"}",
      "{\"cep\":\"05280130\",\"numero\":\"\"}",
      "{\"cep\":\"05280130\",\"numero\":\"   \"}",
      "{\"numero\":\"41\"}",
      "{\"cep\":\"05280130\"}"
  })
  void deveRejeitarEnderecoInvalido(String json) throws Exception {
    mvc.perform(requisicao(ENDERECO, 1, json))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));

    enderecoPreservado(1);
    verify(cepGateway, never()).buscarEnderecoPorCep(anyString());
  }

  @Test
  void deveAtualizarEnderecoComumPreservandoSeuId() throws Exception {
    mvc.perform(requisicao(ENDERECO, 1, ENDERECO_JSON))
        .andExpect(status().isNoContent());

    assertEquals(
        "Rua das Flores",
        texto("SELECT logradouro FROM tb_enderecos WHERE id_endereco = 1"));

    assertEquals(
        "05280130",
        texto("SELECT cep FROM tb_enderecos WHERE id_endereco = 1"));

    assertEquals(
        "41",
        texto("SELECT numero FROM tb_enderecos WHERE id_endereco = 1"));

    assertEquals(
        "Novo complemento",
        texto("SELECT complemento FROM tb_enderecos WHERE id_endereco = 1"));

    assertEquals(
        1,
        inteiro("SELECT endereco_id FROM tb_usuarios WHERE id_usuario = 1"));

    assertEquals(7, inteiro("SELECT count(*) FROM tb_enderecos"));
    enderecoPreservado(2);

    verify(cepGateway).buscarEnderecoPorCep("05280130");
  }

  @Test
  void deveLimparComplementoQuandoOmitido() throws Exception {
    mvc.perform(requisicao(ENDERECO, 1, """
        {"cep": "05280130", "numero": "41"}
        """))
        .andExpect(status().isNoContent());

    assertNull(
        texto("SELECT complemento FROM tb_enderecos WHERE id_endereco = 1"));
  }

  @Test
  void deveAtualizarEnderecoDaCooperativaSemAlterarCnpj()
      throws Exception {

    mvc.perform(requisicao(ENDERECO, 5, ENDERECO_JSON))
        .andExpect(status().isNoContent());

    assertEquals(
        "Rua das Flores",
        texto("SELECT logradouro FROM tb_enderecos WHERE id_endereco = 5"));

    assertEquals(
        "34238864000168",
        texto("SELECT cnpj_cooperativa FROM tb_cooperativas WHERE id_coop = 200"));

    assertEquals(
        5,
        inteiro("SELECT endereco_id FROM tb_usuarios WHERE id_usuario = 5"));

    assertEquals(
        5,
        inteiro("SELECT endereco_id FROM tb_cooperativas WHERE id_coop = 200"));
  }

  @ParameterizedTest
  @ValueSource(ints = { 3, 4 })
  void deveBloquearSindicoEMoradorNoEnderecoPessoal(int usuarioId)
      throws Exception {

    mvc.perform(requisicao(ENDERECO, usuarioId, ENDERECO_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.codigoError").value("UPDATE_NOT_ALLOWED"));

    enderecoPreservado(3);
    verify(cepGateway, never()).buscarEnderecoPorCep(anyString());
  }

  @Test
  void deveAtualizarCondominioDoSindicoSemTrocarVinculosDosMoradores()
      throws Exception {

    mvc.perform(requisicao(
        "/api/condominios/100/endereco",
        3,
        ENDERECO_JSON)).andExpect(status().isNoContent());

    assertEquals(
        "Rua das Flores",
        texto("SELECT logradouro FROM tb_enderecos WHERE id_endereco = 3"));

    assertEquals(
        3,
        inteiro("SELECT endereco_id FROM tb_condominios WHERE id_condominio = 100"));

    assertEquals(
        3,
        inteiro("SELECT endereco_id FROM tb_usuarios WHERE id_usuario = 4"));

    enderecoPreservado(6);
  }

  @Test
  void deveAtualizarNomeTipoEFotoDoCondominio() throws Exception {
    mvc.perform(requisicao(
        CONDOMINIO,
        3,
        """
        {
            "nome": "  Residencial Eco Vida  ",
            "tipoCondominioId": 2,
            "urlFoto": "https://res.cloudinary.com/demo/condominio.jpg"
        }
        """)).andExpect(status().isNoContent());

    assertEquals(
        "Residencial Eco Vida",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 100"));

    assertEquals(
        2,
        inteiro("SELECT tipo_condominio_id FROM tb_condominios WHERE id_condominio = 100"));

    assertEquals(
        "https://res.cloudinary.com/demo/condominio.jpg",
        texto("SELECT url_foto FROM tb_condominios WHERE id_condominio = 100"));
  }

  @Test
  void deveAtualizarSoONomeSemApagarTipoNemFoto() throws Exception {
    mvc.perform(requisicao(CONDOMINIO, 3, "{\"nome\": \"So o nome\"}"))
        .andExpect(status().isNoContent());

    assertEquals(
        "So o nome",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 100"));

    assertEquals(
        1,
        inteiro("SELECT tipo_condominio_id FROM tb_condominios WHERE id_condominio = 100"));

    assertEquals(
        "https://exemplo.com/foto-um.jpg",
        texto("SELECT url_foto FROM tb_condominios WHERE id_condominio = 100"));
  }

  @Test
  void naoDeveAlterarOutroCondominioAoAtualizarOProprio() throws Exception {
    mvc.perform(requisicao(CONDOMINIO, 3, "{\"nome\": \"Alterado\"}"))
        .andExpect(status().isNoContent());

    assertEquals(
        "Condomínio Dois",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 101"));
  }

  @Test
  void deveBloquearSindicoDeOutroCondominioNaAtualizacaoDeDados() throws Exception {
    mvc.perform(requisicao("/api/condominios/101", 3, "{\"nome\": \"Invasao\"}"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.codigoError").value("UPDATE_NOT_ALLOWED"));

    assertEquals(
        "Condomínio Dois",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 101"));
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 4, 5 })
  void deveBloquearNaoSindicoNaAtualizacaoDeDadosDoCondominio(int usuarioId)
      throws Exception {

    mvc.perform(requisicao(CONDOMINIO, usuarioId, "{\"nome\": \"Invasao\"}"))
        .andExpect(status().isForbidden());

    assertEquals(
        "Condomínio Um",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 100"));
  }

  @Test
  void deveBloquearAtualizacaoDeCondominioInativo() throws Exception {
    jdbc.update(
        "UPDATE tb_condominios SET ativo = FALSE WHERE id_condominio = 100");

    mvc.perform(requisicao(CONDOMINIO, 3, "{\"nome\": \"Novo\"}"))
        .andExpect(status().isForbidden());

    assertEquals(
        "Condomínio Um",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 100"));
  }

  @Test
  void deveRejeitarAtualizacaoDeCondominioSemNenhumCampo() throws Exception {
    mvc.perform(requisicao(CONDOMINIO, 3, "{}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("EMPTY_UPDATE"));
  }

  @Test
  void deveRejeitarTipoDeCondominioInexistente() throws Exception {
    mvc.perform(requisicao(CONDOMINIO, 3, "{\"tipoCondominioId\": 99}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("CONDOMINIUM_TYPE_NOT_FOUND"));

    assertEquals(
        1,
        inteiro("SELECT tipo_condominio_id FROM tb_condominios WHERE id_condominio = 100"));
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{\"nome\": \"   \"}",
      "{\"nome\": \"\"}",
      "{\"tipoCondominioId\": 0}",
      "{\"urlFoto\": \"ftp://exemplo.com/foto.jpg\"}",
      "{\"urlFoto\": \"sem protocolo\"}"
  })
  void deveRejeitarCamposInvalidosNoCondominio(String corpo) throws Exception {
    mvc.perform(requisicao(CONDOMINIO, 3, corpo))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));

    assertEquals(
        "Condomínio Um",
        texto("SELECT nome_condominio FROM tb_condominios WHERE id_condominio = 100"));
  }

  @Test
  void deveRejeitarUrlDeFotoComMaisDe500Caracteres() throws Exception {
    String urlLonga = "https://exemplo.com/" + "a".repeat(500);

    mvc.perform(requisicao(CONDOMINIO, 3, "{\"urlFoto\": \"" + urlLonga + "\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));
  }

  @Test
  void deveRejeitarIdDeCondominioQueNaoEInteiroNaAtualizacaoDeDados()
      throws Exception {

    mvc.perform(requisicao("/api/condominios/abc", 3, "{\"nome\": \"Novo\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("PARAMETRO_INVALIDO"));
  }

  @Test
  void deveBloquearSindicoQueNaoEResponsavelPeloCondominio()
      throws Exception {

    mvc.perform(requisicao(
        "/api/condominios/101/endereco",
        3,
        ENDERECO_JSON))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.codigoError").value("UPDATE_NOT_ALLOWED"));

    enderecoPreservado(6);
    verify(cepGateway, never()).buscarEnderecoPorCep(anyString());
  }

  @ParameterizedTest
  @ValueSource(ints = { 1, 4, 5 })
  void deveBloquearNaoSindicoNoCondominioMesmoComPerfilForjadoNoToken(
      int usuarioId) throws Exception {

    mvc.perform(requisicao(
        "/api/condominios/100/endereco",
        usuarioId,
        ENDERECO_JSON)).andExpect(status().isForbidden());

    enderecoPreservado(3);
  }

  @Test
  void deveBloquearCondominioInativo() throws Exception {
    jdbc.update(
        "UPDATE tb_condominios SET ativo = FALSE WHERE id_condominio = 100");

    mvc.perform(requisicao(
        "/api/condominios/100/endereco",
        3,
        ENDERECO_JSON)).andExpect(status().isForbidden());

    enderecoPreservado(3);
  }

  @Test
  void deveBloquearCondominioSemEndereco() throws Exception {
    jdbc.update(
        "UPDATE tb_condominios SET endereco_id = NULL WHERE id_condominio = 100");

    mvc.perform(requisicao(
        "/api/condominios/100/endereco",
        3,
        ENDERECO_JSON)).andExpect(status().isForbidden());

    enderecoPreservado(3);
  }

  @Test
  void deveRejeitarIdDeCondominioQueNaoEInteiro() throws Exception {
    mvc.perform(requisicao(
        "/api/condominios/abc/endereco",
        3,
        ENDERECO_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("PARAMETRO_INVALIDO"));

    enderecoPreservado(3);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "UPDATE tb_usuarios SET endereco_id = 1 WHERE id_usuario = 2",
      "UPDATE tb_condominios SET endereco_id = 1 WHERE id_condominio = 101",
      "UPDATE tb_cooperativas SET endereco_id = 1 WHERE id_coop = 200",
      "INSERT INTO tb_pontos_coletas (id_ponto_coleta, endereco_id) VALUES (1, 1)"
  })
  void deveBloquearEnderecoComumCompartilhadoComOutrosRegistros(
      String preparacao) throws Exception {

    jdbc.update(preparacao);

    mvc.perform(requisicao(ENDERECO, 1, ENDERECO_JSON))
        .andExpect(status().isForbidden());

    enderecoPreservado(1);
    verify(cepGateway, never()).buscarEnderecoPorCep(anyString());
  }

  @Test
  void deveRejeitarCooperativaComEnderecoDivergente() throws Exception {
    jdbc.update(
        "UPDATE tb_cooperativas SET endereco_id = 2 WHERE id_coop = 200");

    mvc.perform(requisicao(ENDERECO, 5, ENDERECO_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.codigoError").value("ADDRESS_LINK_INCONSISTENT"));

    enderecoPreservado(2);
    enderecoPreservado(5);
  }

  @Test
  void deveRejeitarUsuarioCooperativaSemRegistroVinculado()
      throws Exception {

    jdbc.update("DELETE FROM tb_cooperativas WHERE id_coop = 200");

    mvc.perform(requisicao(ENDERECO, 5, ENDERECO_JSON))
        .andExpect(status().isForbidden());

    enderecoPreservado(5);
  }

  @Test
  void deveRetornarErroDeCepSemAlterarEndereco() throws Exception {
    when(cepGateway.buscarEnderecoPorCep("05280130"))
        .thenThrow(new CepException("CEP_INVALIDO", "CEP não encontrado"));

    mvc.perform(requisicao(ENDERECO, 1, ENDERECO_JSON))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("CEP_INVALIDO"));

    enderecoPreservado(1);
  }

  @Test
  void deveRetornarErroGenericoSemExporDetalhesInternos()
      throws Exception {

    when(cepGateway.buscarEnderecoPorCep("05280130"))
        .thenThrow(new RuntimeException("senha-interna-do-provedor"));

    mvc.perform(requisicao(ENDERECO, 1, ENDERECO_JSON))
        .andExpect(status().isInternalServerError())
        .andExpect(jsonPath("$.codigoError").value("ERRO_INTERNO"))
        .andExpect(
            jsonPath("$.details[0].message")
                .value("Erro interno do servidor"));

    enderecoPreservado(1);
  }

  @ParameterizedTest
  @ValueSource(ints = { 11, 12 })
  void deveRejeitarTelefoneDeOutroUsuarioOuInativo(int telefoneId)
      throws Exception {

    mvc.perform(requisicao(DADOS, 1, """
        {"telefone": "11999999999", "telefoneId": %d}
        """.formatted(telefoneId)))
        .andExpect(status().isForbidden());

    usuarioUmPreservado();

    assertEquals(
        "11922222222",
        texto("SELECT numero_contato FROM tb_telefones WHERE id_telefone = 11"));

    assertEquals(
        "11900000000",
        texto("SELECT numero_contato FROM tb_telefones WHERE id_telefone = 12"));
  }

  @Test
  void deveCriarTelefoneSemAlterarTelefoneDaEmpresa() throws Exception {
    mvc.perform(requisicao(DADOS, 5, """
        {"telefone": "11999999999"}
        """))
        .andExpect(status().isNoContent());

    assertEquals(
        "11999999999",
        texto("SELECT numero_contato FROM tb_telefones WHERE usuario_id = 5"));

    assertEquals(
        "1133334444",
        texto("SELECT telefone_cooperativa FROM tb_cooperativas WHERE id_coop = 200"));
  }

  @Test
  void deveExigirIdQuandoExistemVariosTelefonesAtivos() throws Exception {
    jdbc.update(
        """
            INSERT INTO tb_telefones (
                id_telefone, usuario_id, numero_contato, ativo
            ) VALUES (13, 1, '11933333333', TRUE)
            """);

    mvc.perform(requisicao(DADOS, 1, """
        {"telefone": "11999999999"}
        """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("PHONE_ID_REQUIRED"));

    usuarioUmPreservado();
  }

  @Test
  void deveAtualizarSomenteTelefoneSelecionado() throws Exception {
    jdbc.update(
        """
            INSERT INTO tb_telefones (
                id_telefone, usuario_id, numero_contato, ativo
            ) VALUES (13, 1, '11933333333', TRUE)
            """);

    mvc.perform(requisicao(DADOS, 1, """
        {"telefone": "11999999999", "telefoneId": 13}
        """))
        .andExpect(status().isNoContent());

    assertEquals(
        "11999999999",
        texto("SELECT numero_contato FROM tb_telefones WHERE id_telefone = 13"));

    assertEquals(
        "11911111111",
        texto("SELECT numero_contato FROM tb_telefones WHERE id_telefone = 10"));
  }

  @Test
  void deveDesfazerAlteracaoDoTelefoneSeAtualizacaoDoUsuarioFalhar() {
    AtualizacaoDados entrada = new AtualizacaoDados(
        "a".repeat(101),
        null,
        null,
        null,
        "11999999999",
        10);

    assertThrows(
        DataIntegrityViolationException.class,
        () -> service.atualizarDados(1, entrada));

    usuarioUmPreservado();
  }

  @TestConfiguration(proxyBeanMethods = false)
  @EnableTransactionManagement
  static class BancoConfig {

    @Bean(initMethod = "start", destroyMethod = "stop")
    PostgreSQLContainer postgres() {
      return new PostgreSQLContainer("postgres:16-alpine")
          .withDatabaseName("cadastro_test")
          .withUsername("teste")
          .withPassword("teste");
    }

    @Bean
    DataSource dataSource(PostgreSQLContainer postgres) {
      DriverManagerDataSource dataSource = new DriverManagerDataSource();

      dataSource.setDriverClassName("org.postgresql.Driver");
      dataSource.setUrl(postgres.getJdbcUrl());
      dataSource.setUsername(postgres.getUsername());
      dataSource.setPassword(postgres.getPassword());

      return dataSource;
    }

    @Bean
    JdbcTemplate jdbcTemplate(DataSource dataSource) {
      return new JdbcTemplate(dataSource);
    }

    @Bean
    PlatformTransactionManager transactionManager(
        DataSource dataSource) {
      return new DataSourceTransactionManager(dataSource);
    }
  }
}