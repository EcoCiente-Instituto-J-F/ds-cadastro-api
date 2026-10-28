package br.com.ecociente.cadastro.entrypoint.mapper;

import org.springframework.stereotype.Component;

import br.com.ecociente.cadastro.core.domain.AtualizacaoCondominio;
import br.com.ecociente.cadastro.core.domain.AtualizacaoDados;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarCondominioRequestDto;
import br.com.ecociente.cadastro.entrypoint.dto.request.AtualizarUsuarioRequestDto;

@Component
public class AtualizacaoUsuarioMapper {

    public AtualizacaoDados toDomain(AtualizarUsuarioRequestDto request) {
        return new AtualizacaoDados(
            request.nome(),
            request.email(),
            request.dataNascimento(),
            request.urlAvatar(),
            request.telefone(),
            request.telefoneId()
        );
    }

    public AtualizacaoCondominio toDomain(AtualizarCondominioRequestDto request) {
        return new AtualizacaoCondominio(
            request.nome(),
            request.tipoCondominioId(),
            request.urlFoto()
        );
    }
}