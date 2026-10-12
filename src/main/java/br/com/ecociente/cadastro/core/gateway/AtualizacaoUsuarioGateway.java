package br.com.ecociente.cadastro.core.gateway;

import java.util.List;
import java.util.Optional;

import br.com.ecociente.cadastro.core.domain.AtualizacaoDados;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.UsuarioEdicao;

public interface AtualizacaoUsuarioGateway {

    Optional<UsuarioEdicao> buscarUsuarioAtivo(Integer usuarioId);

    boolean emailPertenceAOutroUsuario(
        String email,
        Integer usuarioId
    );

    void atualizarDados(
        Integer usuarioId,
        AtualizacaoDados dados
    );

    List<Integer> buscarTelefonesAtivos(Integer usuarioId);

    void criarTelefone(Integer usuarioId, String numero);

    void atualizarTelefone(
        Integer usuarioId,
        Integer telefoneId,
        String numero
    );

    Optional<Integer> buscarEnderecoCooperativa(Integer usuarioId);

    boolean enderecoExclusivoDoUsuario(
        Integer usuarioId,
        Integer enderecoId
    );

    Optional<Integer> buscarEnderecoCondominioDoSindico(
        Integer usuarioId,
        Integer condominioId
    );

    void atualizarEndereco(Endereco endereco);
}