package br.com.ecociente.cadastro.entrypoint.exception;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.entrypoint.dto.ValidationError;


@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  private ErrorResponse createErrorResponse(HttpStatus status, String codigoError, List<ValidationError> details) {
    return new ErrorResponse(status.value(), codigoError, details);
  }

  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
    log.warn("Tipo inválido para o parâmetro {}: {}", ex.getName(), ex.getValue());
    List<ValidationError> details = new ArrayList<>();
    details.add(new ValidationError(ex.getName(), "Valor inválido para o parâmetro: " + ex.getName() + "'"));
    var response = createErrorResponse(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }
  
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, WebRequest request) {
    log.warn("Tipo inválido para o parâmetro {}: {}", ex.getName(), ex.getValue());
    List<ValidationError> details = new ArrayList<>();
    details.add(new ValidationError(ex.getName(), "Valor inválido para o parâmetro: " + ex.getName() + "'"));
    var response = createErrorResponse(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO", details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex, WebRequest request) {
    log.warn("Falha de validação: {}", ex.getMessage());
    List<ValidationError> details = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> new ValidationError(error.getField(), error.getDefaultMessage()))
        .toList();
    var response = createErrorResponse(HttpStatus.BAD_REQUEST, "ERRO_VALIDACAO", details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex, WebRequest request) {
    log.warn("JSON malformado: {}", ex.getMessage());
    List<ValidationError> details = new ArrayList<>();
    details.add(new ValidationError("body", "JSON malformado ou impossível de ler"));
    var response = createErrorResponse(HttpStatus.BAD_REQUEST, "JSON_MALFORMADO", details);
    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
    log.error("Erro inesperado: {}", ex.getMessage(), ex);
    List<ValidationError> details = new ArrayList<>();
    details.add(new ValidationError("erro", "Erro interno do servidor"));
    var response = createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "ERRO_INTERNO", details);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

}
