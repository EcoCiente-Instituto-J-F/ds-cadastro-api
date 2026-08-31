package br.com.ecociente.cadastro.entrypoint.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.exception.CepException;
import br.com.ecociente.cadastro.core.exception.NotFoundException;
import br.com.ecociente.cadastro.entrypoint.dto.ErrorResponse;

class GlobalExceptionHandlerTest {

  private GlobalExceptionHandler globalExceptionHandler;

  @BeforeEach
  void setUp() {
    globalExceptionHandler = new GlobalExceptionHandler();
  }

  @Test
  @DisplayName("Deve retornar 400 quando ocorrer BusinessException")
  void shouldReturn400WhenBusinessException() {
    BusinessException ex = new BusinessException("VALIDATION_ERROR", "Menor de idade");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleBusiness(ex);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().status());
    assertEquals("VALIDATION_ERROR", response.getBody().codigoError());
    assertEquals("Menor de idade", response.getBody().details().get(0).message());
  }

  @Test
  @DisplayName("Deve retornar 404 quando ocorrer AlreadyExistsException")
  void shouldReturn404WhenAlreadyExistsException() {
    AlreadyExistsException ex = new AlreadyExistsException("EMAIL_ALREADY_EXISTS", "Email já cadastrado");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleAlreadyExists(ex);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().status());
    assertEquals("EMAIL_ALREADY_EXISTS", response.getBody().codigoError());
  }

  @Test
  @DisplayName("Deve retornar 404 quando ocorrer NotFoundException")
  void shouldReturn404WhenNotFoundException() {
    NotFoundException ex = new NotFoundException("CONDOMINIUM_NOT_FOUND", "Condomínio não encontrado");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleNotFound(ex);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    assertEquals(404, response.getBody().status());
    assertEquals("CONDOMINIUM_NOT_FOUND", response.getBody().codigoError());
  }

  @Test
  @DisplayName("Deve retornar 400 quando ocorrer CepException")
  void shouldReturn400WhenCepException() {
    CepException ex = new CepException("CEP_INVALIDO", "CEP não encontrado");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleCep(ex);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals(400, response.getBody().status());
    assertEquals("CEP_INVALIDO", response.getBody().codigoError());
  }

  @Test
  @DisplayName("Deve retornar 400 com lista de erros quando MethodArgumentNotValidException")
  void shouldReturn400WithFieldErrorsWhenMethodArgumentNotValidException() throws Exception {
    Method method = GlobalExceptionHandlerTest.class.getDeclaredMethod("dummyMethod", String.class);
    MethodParameter parameter = new MethodParameter(method, 0);
    BindingResult bindingResult = mock(BindingResult.class);
    when(bindingResult.getFieldErrors()).thenReturn(List.of(
        new FieldError("request", "email", "Email é obrigatório"),
        new FieldError("request", "senha", "Senha é obrigatória")
    ));

    MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);
    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleValidationException(ex, null);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("ERRO_VALIDACAO", response.getBody().codigoError());
    assertEquals(2, response.getBody().details().size());
    assertEquals("email", response.getBody().details().get(0).field());
    assertEquals("Email é obrigatório", response.getBody().details().get(0).message());
  }

  @Test
  @DisplayName("Deve retornar 400 quando HttpMessageNotReadableException")
  void shouldReturn400WhenHttpMessageNotReadableException() {
    HttpMessageNotReadableException ex = new HttpMessageNotReadableException("JSON malformado", (org.springframework.http.HttpInputMessage) null);

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleHttpMessageNotReadableException(ex, null);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    assertEquals("JSON_MALFORMADO", response.getBody().codigoError());
    assertEquals("body", response.getBody().details().get(0).field());
  }

  @Test
  @DisplayName("Deve retornar 500 e mensagem genérica quando Exception não mapeada")
  void shouldReturn500WhenUnmappedException() {
    RuntimeException ex = new RuntimeException("Erro interno do banco");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(ex, null);

    assertNotNull(response.getBody());
    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    assertEquals(500, response.getBody().status());
    assertEquals("ERRO_INTERNO", response.getBody().codigoError());
    assertEquals("Erro interno do servidor", response.getBody().details().get(0).message());
  }

  @Test
  @DisplayName("Não deve vazar mensagem interna no fallback genérico")
  void shouldNotLeakInternalMessageInGenericFallback() {
    RuntimeException ex = new RuntimeException("Detalhe sensível do banco");

    ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleGenericException(ex, null);

    assertNotNull(response.getBody());
    String mensagem = response.getBody().details().get(0).message();
    assertEquals("Erro interno do servidor", mensagem);
  }

  private void dummyMethod(String param) {}
}