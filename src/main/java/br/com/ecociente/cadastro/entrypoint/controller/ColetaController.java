package br.com.ecociente.cadastro.entrypoint.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.com.ecociente.cadastro.core.Service.ColetaService;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarStatusColetaRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.request.CriarColetaRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.response.ColetaResponseDto;
import br.com.ecociente.cadastro.entrypoint.mapper.ColetaMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coletas")
public class ColetaController {

  private final ColetaService service;
  private final ColetaMapper mapper;

  @PostMapping
  public ResponseEntity<ColetaResponseDto> criar(
      Authentication authentication,
      @Valid @RequestBody CriarColetaRequestDto request) {

    var coleta = service.criar(
        usuarioId(authentication),
        mapper.toDomain(request));

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(mapper.toResponse(coleta));
  }

  @PatchMapping("/{id}/status")
  public ResponseEntity<ColetaResponseDto> atualizarStatus(
      Authentication authentication,
      @PathVariable("id") Integer coletaId,
      @Valid @RequestBody AtualizarStatusColetaRequestDto request) {

    var coleta = service.atualizarStatus(
        usuarioId(authentication),
        coletaId,
        request.status());

    return ResponseEntity.ok(mapper.toResponse(coleta));
  }

  private Integer usuarioId(Authentication authentication) {
    return (Integer) authentication.getPrincipal();
  }
}