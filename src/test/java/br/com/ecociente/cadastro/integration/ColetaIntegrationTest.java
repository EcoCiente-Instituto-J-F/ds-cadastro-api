package br.com.ecociente.cadastro.integration;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import org.testcontainers.containers.PostgreSQLContainer;

import br.com.ecociente.cadastro.config.SecurityConfig;
import br.com.ecociente.cadastro.config.security.JwtAuthenticationFilter;
import br.com.ecociente.cadastro.config.security.JwtService;
import br.com.ecociente.cadastro.core.Service.ColetaService;
import br.com.ecociente.cadastro.core.domain.StatusAgendamento;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.dataprovaider.gateway.ColetaGatewayImpl;
import br.com.ecociente.cadastro.dataprovaider.mapper.ColetaPersistenciaMapper;
import br.com.ecociente.cadastro.entrypoint.controller.ColetaController;
import br.com.ecociente.cadastro.entrypoint.exception.GlobalExceptionHandler;
import br.com.ecociente.cadastro.entrypoint.mapper.ColetaMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@SpringJUnitConfig(ColetaIntegrationTest.Config.class)
@WebAppConfiguration
@TestPropertySource(properties = {
    "app.security.jwt.secret=0123456789abcdef0123456789abcdef"
})
@Execution(ExecutionMode.SAME_THREAD)
class ColetaIntegrationTest {

  private static final String SECRET =
      "0123456789abcdef0123456789abcdef";


  private static final PostgreSQLContainer<?> POSTGRES =
      new PostgreSQLContainer<>("postgres:16-alpine");

  static {
    POSTGRES.start();
  }

  @Autowired
  private WebApplicationContext context;

  @Autowired
  private DataSource dataSource;

  @Autowired
  private JdbcTemplate jdbc;

  @Autowired
  private ColetaService service;

  private MockMvc mvc;

  @BeforeEach
  void preparar() throws Exception {
    String schema = recurso("sql/ecociente-schema.sql");
    String dados = recurso("sql/coletas-dados-test.sql");

   
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {

      statement.execute("DROP SCHEMA public CASCADE");
      statement.execute("CREATE SCHEMA public");
      statement.execute(schema);
      statement.execute(dados);
    }

    Integer campos = jdbc.queryForObject("""
        SELECT COUNT(*)
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'tb_agendamentos_coletas'
          AND column_name IN (
            'solicitado_por_usuario_id',
            'origem_solicitacao',
            'respondido_por_usuario_id',
            'respondido_em'
          )
        """, Integer.class);

    assertEquals(
        4, campos,
        "Atualize ecociente-schema.sql com os quatro campos novos");

    mvc = MockMvcBuilders
        .webAppContextSetup(context)
        .apply(springSecurity())
        .build();
  }

  @Test
  void sindicoDeveCriarComIdentidadeDoJwt() throws Exception {
    int id = criar(10);

    assertEquals(10, inteiro(id, "solicitado_por_usuario_id"));
    assertEquals(1, inteiro(id, "status_agendamento_id"));

    assertEquals(
        "SINDICO",
        jdbc.queryForObject("""
            SELECT origem_solicitacao
            FROM tb_agendamentos_coletas
            WHERE id_agendamento_coleta = ?
            """, String.class, id));

    assertFalse(Boolean.TRUE.equals(
        jdbc.queryForObject("""
            SELECT possui_recorrencia
            FROM tb_agendamentos_coletas
            WHERE id_agendamento_coleta = ?
            """, Boolean.class, id)));

    assertNull(inteiro(id, "respondido_por_usuario_id"));
    assertNull(respondidoEm(id));

    assertEquals(0L, jdbc.queryForObject("""
        SELECT COUNT(*)
        FROM tb_rel_recorrencias_agendamentos
        WHERE agendamento_coleta_id = ?
        """, Long.class, id));
  }

  @Test
  void cooperativaDeveCriarSolicitacao() throws Exception {
    int id = criar(20);

    assertEquals(20, inteiro(id, "solicitado_por_usuario_id"));

    assertEquals(
        "COOPERATIVA",
        jdbc.queryForObject("""
            SELECT origem_solicitacao
            FROM tb_agendamentos_coletas
            WHERE id_agendamento_coleta = ?
            """, String.class, id));
  }

  @ParameterizedTest
  @CsvSource({
      "10,20",
      "20,10"
  })
  void destinatarioDeveConfirmar(
      int solicitante,
      int destinatario) throws Exception {

    int id = criar(solicitante);

    alterar(id, destinatario, "CONFIRMADO", 200);

    assertEquals(2, inteiro(id, "status_agendamento_id"));
    assertEquals(
        destinatario,
        inteiro(id, "respondido_por_usuario_id"));
    assertNotNull(respondidoEm(id));
  }

  @ParameterizedTest
  @CsvSource({
      "10,20,RECUSADO,3",
      "20,10,RECUSADO,3",
      "10,20,CANCELADO,5",
      "20,10,CANCELADO,5"
  })
  void destinatarioDeveRecusarOuCancelar(
      int solicitante,
      int destinatario,
      String status,
      int statusId) throws Exception {

    int id = criar(solicitante);

    alterar(id, destinatario, status, 200);

    assertEquals(statusId, inteiro(id, "status_agendamento_id"));
    assertEquals(
        destinatario,
        inteiro(id, "respondido_por_usuario_id"));
  }

  @ParameterizedTest
  @CsvSource({
      "10,CONFIRMADO",
      "10,RECUSADO",
      "10,CANCELADO",
      "20,CONFIRMADO",
      "20,RECUSADO",
      "20,CANCELADO"
  })
  void solicitanteNaoDeveResponder(
      int solicitante,
      String status) throws Exception {

    int id = criar(solicitante);

    alterar(id, solicitante, status, 403);

    assertEquals(1, inteiro(id, "status_agendamento_id"));
    assertNull(inteiro(id, "respondido_por_usuario_id"));
  }

  @ParameterizedTest
  @ValueSource(ints = {30, 40, 50})
  void terceiroNaoDeveAlterarColeta(int usuario) throws Exception {
    int id = criar(10);

    alterar(id, usuario, "CONFIRMADO", 403);

    assertEquals(1, inteiro(id, "status_agendamento_id"));
  }

  @ParameterizedTest
  @CsvSource({
      "10,20",
      "20,10"
  })
  void somenteCooperativaDeveConcluirAposConfirmacao(
      int solicitante,
      int destinatario) throws Exception {

    int id = criar(solicitante);
    alterar(id, destinatario, "CONFIRMADO", 200);

    OffsetDateTime respostaAntes = respondidoEm(id);

    alterar(id, 10, "REALIZADO", 403);
    alterar(id, 20, "REALIZADO", 200);

    assertEquals(4, inteiro(id, "status_agendamento_id"));

    assertEquals(
        destinatario,
        inteiro(id, "respondido_por_usuario_id"));
    assertEquals(respostaAntes, respondidoEm(id));
  }

  @Test
  void naoDeveConcluirAntesDeConfirmar() throws Exception {
    int id = criar(10);

    alterar(id, 20, "REALIZADO", 400);

    assertEquals(1, inteiro(id, "status_agendamento_id"));
  }

  @ParameterizedTest
  @CsvSource({
      "10,20",
      "20,10"
  })
  void destinatarioDeveCancelarDepoisDaConfirmacao(
      int solicitante,
      int destinatario) throws Exception {

    int id = criar(solicitante);

    alterar(id, destinatario, "CONFIRMADO", 200);
    alterar(id, solicitante, "CANCELADO", 403);
    alterar(id, destinatario, "CANCELADO", 200);

    assertEquals(5, inteiro(id, "status_agendamento_id"));
  }

  @ParameterizedTest
  @ValueSource(strings = {"RECUSADO", "CANCELADO"})
  void naoDeveReabrirStatusFinal(String status) throws Exception {
    int id = criar(10);

    alterar(id, 20, status, 200);
    alterar(id, 20, "CONFIRMADO", 400);
  }

  @Test
  void naoDeveCancelarColetaRealizada() throws Exception {
    int id = criar(10);

    alterar(id, 20, "CONFIRMADO", 200);
    alterar(id, 20, "REALIZADO", 200);
    alterar(id, 20, "CANCELADO", 400);

    assertEquals(4, inteiro(id, "status_agendamento_id"));
  }

  @Test
  void devePreservarDadosDuranteMudancaDeStatus() throws Exception {
    int id = criar(10);

    var antes = jdbc.queryForMap("""
        SELECT condominio_id, cooperativa_id,
               data_inicio, data_fim,
               solicitado_por_usuario_id, origem_solicitacao
        FROM tb_agendamentos_coletas
        WHERE id_agendamento_coleta = ?
        """, id);

    alterar(id, 20, "CONFIRMADO", 200);

    var depois = jdbc.queryForMap("""
        SELECT condominio_id, cooperativa_id,
               data_inicio, data_fim,
               solicitado_por_usuario_id, origem_solicitacao
        FROM tb_agendamentos_coletas
        WHERE id_agendamento_coleta = ?
        """, id);

    assertEquals(antes, depois);
  }

  @ParameterizedTest
  @ValueSource(ints = {30, 40, 50})
  void usuarioSemVinculoNaoDeveCriar(int usuario) throws Exception {
    mvc.perform(post("/api/coletas")
            .header("Authorization", bearer(usuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isForbidden());

    assertEquals(0L, quantidadeColetas());
  }

  @Test
  void usuarioInativoNaoDeveCriar() throws Exception {
    jdbc.update("""
        UPDATE tb_usuarios
        SET ativo = FALSE
        WHERE id_usuario = 10
        """);

    mvc.perform(post("/api/coletas")
            .header("Authorization", bearer(10))
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isForbidden());

    assertEquals(0L, quantidadeColetas());
  }

  @Test
  void condominioInativoNaoDeveReceberSolicitacao() throws Exception {
    jdbc.update("""
        UPDATE tb_condominios
        SET ativo = FALSE
        WHERE id_condominio = 1
        """);

    mvc.perform(post("/api/coletas")
            .header("Authorization", bearer(20))
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isNotFound());
  }

  @Test
  void deveRetornar404ParaColetaInexistente() throws Exception {
    alterar(999999, 20, "CONFIRMADO", 404);
  }

  @ParameterizedTest
  @ValueSource(strings = {
      "{}",
      "{\"status\":null}",
      "{\"status\":\"INEXISTENTE\"}",
      "{"
  })
  void deveRejeitarCorpoDeStatusInvalido(String corpo)
      throws Exception {

    int id = criar(10);

    mvc.perform(patch("/api/coletas/{id}/status", id)
            .header("Authorization", bearer(20))
            .contentType(MediaType.APPLICATION_JSON)
            .content(corpo))
        .andExpect(status().isBadRequest());

    assertEquals(1, inteiro(id, "status_agendamento_id"));
  }

  @Test
  void deveRejeitarCriacaoInvalida() throws Exception {
    mvc.perform(post("/api/coletas")
            .header("Authorization", bearer(10))
            .contentType(MediaType.APPLICATION_JSON)
            .content("""
                {
                  "condominioId": 0,
                  "cooperativaId": -1,
                  "dataInicio": "2020-01-01T10:00:00-03:00"
                }
                """))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.codigoError").value("ERRO_VALIDACAO"));

    assertEquals(0L, quantidadeColetas());
  }

  @Test
  void deveExigirTokenNaCriacao() throws Exception {
    mvc.perform(post("/api/coletas")
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isUnauthorized());

    assertEquals(0L, quantidadeColetas());
  }

  @Test
  void deveExigirTokenNoPatch() throws Exception {
    int id = criar(10);

    mvc.perform(patch("/api/coletas/{id}/status", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"CONFIRMADO\"}"))
        .andExpect(status().isUnauthorized());

    assertEquals(1, inteiro(id, "status_agendamento_id"));
  }

  @Test
  void deveRejeitarTokenExpirado() throws Exception {
    String token = token(
        10, SECRET, Instant.now().minusSeconds(60));

    mvc.perform(post("/api/coletas")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deveRejeitarTokenComOutraAssinatura() throws Exception {
    String token = token(
        10,
        "abcdef0123456789abcdef0123456789",
        Instant.now().plusSeconds(600));

    mvc.perform(post("/api/coletas")
            .header("Authorization", "Bearer " + token)
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void respostasConcorrentesNaoDevemSobrescreverDecisao()
      throws Exception {

    int id = criar(10);

    CountDownLatch prontas = new CountDownLatch(2);
    CountDownLatch iniciar = new CountDownLatch(1);
    ExecutorService executor = Executors.newFixedThreadPool(2);

    try {
      Callable<Boolean> confirmar = () -> responderConcorrente(
          id, StatusAgendamento.CONFIRMADO, prontas, iniciar);

      Callable<Boolean> recusar = () -> responderConcorrente(
          id, StatusAgendamento.RECUSADO, prontas, iniciar);

      Future<Boolean> primeira = executor.submit(confirmar);
      Future<Boolean> segunda = executor.submit(recusar);

      assertTrue(prontas.await(10, TimeUnit.SECONDS));
      iniciar.countDown();

      boolean sucesso1 = primeira.get(20, TimeUnit.SECONDS);
      boolean sucesso2 = segunda.get(20, TimeUnit.SECONDS);

      assertNotEquals(
          sucesso1, sucesso2,
          "Uma resposta deve vencer e a outra deve ser rejeitada");

      assertTrue(List.of(2, 3).contains(
          inteiro(id, "status_agendamento_id")));
      assertEquals(20, inteiro(id, "respondido_por_usuario_id"));

    } finally {
      iniciar.countDown();
      executor.shutdownNow();
    }
  }

  private boolean responderConcorrente(
      int id,
      StatusAgendamento status,
      CountDownLatch prontas,
      CountDownLatch iniciar) throws Exception {

    prontas.countDown();

    if (!iniciar.await(10, TimeUnit.SECONDS)) {
      throw new IllegalStateException("Tempo de início excedido");
    }

    try {
      service.atualizarStatus(20, id, status);
      return true;
    } catch (BusinessException ex) {
      if (!"TRANSICAO_STATUS_INVALIDA".equals(ex.getCodigoErro())) {
        throw ex;
      }
      return false;
    }
  }

  private int criar(int usuario) throws Exception {
    mvc.perform(post("/api/coletas")
            .header("Authorization", bearer(usuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content(criacaoJson()))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("AGENDADO"))
        .andExpect(jsonPath("$.possuiRecorrencia").value(false));

    return jdbc.queryForObject("""
        SELECT MAX(id_agendamento_coleta)
        FROM tb_agendamentos_coletas
        """, Integer.class);
  }

  private void alterar(
      int id,
      int usuario,
      String novoStatus,
      int esperado) throws Exception {

    mvc.perform(patch("/api/coletas/{id}/status", id)
            .header("Authorization", bearer(usuario))
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"status\":\"" + novoStatus + "\"}"))
        .andExpect(status().is(esperado));
  }

  private String criacaoJson() {
    OffsetDateTime inicio = OffsetDateTime.now().plusDays(2);

    return """
        {
          "condominioId": 1,
          "cooperativaId": 1,
          "dataInicio": "%s",
          "dataFim": "%s"
        }
        """.formatted(inicio, inicio.plusHours(1));
  }

  private Integer inteiro(int id, String coluna) {
    return jdbc.queryForObject(
        "SELECT " + coluna
            + " FROM tb_agendamentos_coletas"
            + " WHERE id_agendamento_coleta = ?",
        Integer.class,
        id);
  }

  private OffsetDateTime respondidoEm(int id) {
    return jdbc.queryForObject("""
        SELECT respondido_em
        FROM tb_agendamentos_coletas
        WHERE id_agendamento_coleta = ?
        """,
        (rs, rowNum) ->
            rs.getObject("respondido_em", OffsetDateTime.class),
        id);
  }

  private long quantidadeColetas() {
    return jdbc.queryForObject(
        "SELECT COUNT(*) FROM tb_agendamentos_coletas",
        Long.class);
  }

  private String bearer(int usuario) {
    return "Bearer " + token(
        usuario, SECRET, Instant.now().plusSeconds(600));
  }

  private String token(
      int usuario,
      String segredo,
      Instant expiracao) {

    return Jwts.builder()
        .subject("usuario-" + usuario + "@teste.com")
        .claim("usuarioId", usuario)
        .issuedAt(Date.from(Instant.now().minusSeconds(120)))
        .expiration(Date.from(expiracao))
        .signWith(Keys.hmacShaKeyFor(
            segredo.getBytes(StandardCharsets.UTF_8)))
        .compact();
  }

  private String recurso(String caminho) throws Exception {
    try (var stream = new ClassPathResource(caminho)
        .getInputStream()) {
      return new String(
          stream.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  @Configuration
  @EnableWebMvc
  @EnableTransactionManagement
  @Import({
      SecurityConfig.class,
      JwtService.class,
      JwtAuthenticationFilter.class,
      ColetaService.class,
      ColetaGatewayImpl.class,
      ColetaPersistenciaMapper.class,
      ColetaController.class,
      ColetaMapper.class,
      GlobalExceptionHandler.class
  })
  static class Config {

    @Bean
    DataSource dataSource() {
      return new DriverManagerDataSource(
          POSTGRES.getJdbcUrl(),
          POSTGRES.getUsername(),
          POSTGRES.getPassword());
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