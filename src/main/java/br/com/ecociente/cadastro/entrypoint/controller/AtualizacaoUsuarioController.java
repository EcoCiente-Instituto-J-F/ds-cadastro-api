package br.com.ecociente.cadastro.entrypoint.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.ecociente.cadastro.core.Service.AtualizarUsuarioService;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarCondominioRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarEnderecoRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarUsuarioRequestDto;
import br.com.ecociente.cadastro.entrypoint.mapper.AtualizacaoUsuarioMapper;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

@RestController 
@AllArgsConstructor 
public class AtualizacaoUsuarioController {

    private final AtualizarUsuarioService service;
    private final AtualizacaoUsuarioMapper mapper;


    @PatchMapping("/api/usuarios/me")
    public ResponseEntity<Void> atualizarDados(
        Authentication authentication,
        @Valid @RequestBody AtualizarUsuarioRequestDto request
    ) {
        service.atualizarDados(
            usuarioId(authentication),
            mapper.toDomain(request)
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/usuarios/me/endereco")
    public ResponseEntity<Void> atualizarMeuEndereco(
        Authentication authentication,
        @Valid @RequestBody AtualizarEnderecoRequestDto request
    ) {
        service.atualizarMeuEndereco(
            usuarioId(authentication),
            request.cep(),
            request.numero(),
            request.complemento()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/condominios/{id}/endereco")
    public ResponseEntity<Void> atualizarEnderecoCondominio(
        Authentication authentication,
        @PathVariable("id") Integer condominioId,
        @Valid @RequestBody AtualizarEnderecoRequestDto request
    ) {
        service.atualizarEnderecoCondominio(
            usuarioId(authentication),
            condominioId,
            request.cep(),
            request.numero(),
            request.complemento()
        );

        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/condominios/{id}")
    public ResponseEntity<Void> atualizarCondominio(
        Authentication authentication,
        @PathVariable("id") Integer condominioId,
        @Valid @RequestBody AtualizarCondominioRequestDto request
    ) {
        service.atualizarCondominio(
            usuarioId(authentication),
            condominioId,
            mapper.toDomain(request)
        );

        return ResponseEntity.noContent().build();
    }

    private Integer usuarioId(Authentication authentication) {
        return (Integer) authentication.getPrincipal();
    }
}