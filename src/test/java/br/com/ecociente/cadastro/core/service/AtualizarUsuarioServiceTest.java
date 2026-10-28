package br.com.ecociente.cadastro.core.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.ecociente.cadastro.core.Service.AtualizarUsuarioService;
import br.com.ecociente.cadastro.core.domain.AtualizacaoCondominio;
import br.com.ecociente.cadastro.core.domain.AtualizacaoDados;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.UsuarioEdicao;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.exception.AtualizacaoNaoPermitidaException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.exception.CepException;
import br.com.ecociente.cadastro.core.gateway.AtualizacaoUsuarioGateway;
import br.com.ecociente.cadastro.core.gateway.CepGateway;

@ExtendWith(MockitoExtension.class)
class AtualizarUsuarioServiceTest {

    @Mock
    private AtualizacaoUsuarioGateway gateway;

    @Mock
    private CepGateway cepGateway;

    @InjectMocks
    private AtualizarUsuarioService service;

    private void usuario(String tipo, Integer enderecoId) {
        when(gateway.buscarUsuarioAtivo(1)).thenReturn(
            Optional.of(new UsuarioEdicao(
                1,
                tipo,
                enderecoId,
                LocalDate.of(1990, 1, 1)
            ))
        );
    }

    private AtualizacaoDados dados(
        String nome,
        String email,
        LocalDate nascimento,
        String avatar,
        String telefone,
        Integer telefoneId
    ) {
        return new AtualizacaoDados(
            nome, email, nascimento, avatar, telefone, telefoneId
        );
    }

    private void nenhumaEscrita() {
        verify(gateway, never()).atualizarDados(any(), any());
        verify(gateway, never()).criarTelefone(any(), any());
        verify(gateway, never()).atualizarTelefone(any(), any(), any());
        verify(gateway, never()).atualizarEndereco(any());
    }

    private Endereco enderecoConsultado() {
        return new Endereco(
            null,
            "05280130",
            "SP",
            "São Paulo",
            "Rua das Flores",
            null,
            null
        );
    }

    @Test
    void deveNormalizarNomeEmailEPreservarCamposOmitidos() {
        usuario("Usuário comum", 10);

        service.atualizarDados(
            1,
            dados("  Emanuelly  ", "NOVO@TESTE.COM", null, null, null, null)
        );

        ArgumentCaptor<AtualizacaoDados> captor =
            ArgumentCaptor.forClass(AtualizacaoDados.class);

        verify(gateway).atualizarDados(eq(1), captor.capture());

        AtualizacaoDados salvo = captor.getValue();

        assertEquals("Emanuelly", salvo.nome());
        assertEquals("novo@teste.com", salvo.email());
        assertNull(salvo.dataNascimento());
        assertNull(salvo.urlAvatar());
        assertNull(salvo.telefone());
        assertNull(salvo.telefoneId());

        verify(gateway).emailPertenceAOutroUsuario("novo@teste.com", 1);
        verify(gateway, never()).buscarTelefonesAtivos(any());
        verifyNoInteractions(cepGateway);
    }

    @Test
    void devePermitirLimparAvatarComStringVazia() {
        usuario("Usuário comum", 10);

        service.atualizarDados(
            1,
            dados(null, null, null, "", null, null)
        );

        verify(gateway).atualizarDados(
            1,
            dados(null, null, null, "", null, null)
        );
    }

    @Test
    void deveRejeitarAtualizacaoVazia() {
        usuario("Usuário comum", 10);

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarDados(
                1,
                dados(null, null, null, null, null, null)
            )
        );

        assertEquals("EMPTY_UPDATE", erro.getCodigoErro());
        nenhumaEscrita();
    }

    @Test
    void deveRejeitarUsuarioAusenteOuInativoNosTresFluxos() {
        when(gateway.buscarUsuarioAtivo(1)).thenReturn(Optional.empty());

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarDados(
                1,
                dados("Nome", null, null, null, null, null)
            )
        );

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarEnderecoCondominio(
                1, 100, "05280130", "41", null
            )
        );

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @Test
    void deveRejeitarEmailDeOutroUsuarioAntesDeAlterarTelefone() {
        usuario("Usuário comum", 10);

        when(gateway.emailPertenceAOutroUsuario("outro@teste.com", 1))
            .thenReturn(true);

        AlreadyExistsException erro = assertThrows(
            AlreadyExistsException.class,
            () -> service.atualizarDados(
                1,
                dados(
                    "Novo",
                    "OUTRO@TESTE.COM",
                    null,
                    null,
                    "11999999999",
                    null
                )
            )
        );

        assertEquals("EMAIL_ALREADY_EXISTS", erro.getCodigoErro());
        verify(gateway, never()).buscarTelefonesAtivos(any());
        nenhumaEscrita();
    }

    @ParameterizedTest
    @CsvSource({
        "Usuário comum, 18",
        "USUARIO_COMUM, 18",
        "Morador, 10",
        "Morador dependente, 10",
        "Síndico, 20",
        "Cooperativa, 20"
    })
    void deveAceitarNascimentoValido(String tipo, int idade) {
        usuario(tipo, 10);

        LocalDate nascimento = LocalDate.now().minusYears(idade);

        service.atualizarDados(
            1,
            dados(null, null, nascimento, null, null, null)
        );

        verify(gateway).atualizarDados(
            1,
            dados(null, null, nascimento, null, null, null)
        );
    }

    @ParameterizedTest
    @CsvSource({
        "Usuário comum, 18",
        "Morador, 10"
    })
    void deveRejeitarUmDiaAntesDaIdadeMinima(String tipo, int idade) {
        usuario(tipo, 10);

        LocalDate nascimento = LocalDate.now()
            .minusYears(idade)
            .plusDays(1);

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarDados(
                1,
                dados(null, null, nascimento, null, null, null)
            )
        );

        assertEquals("BIRTH_DATE_INVALID", erro.getCodigoErro());
        nenhumaEscrita();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void deveRejeitarNascimentoHojeOuNoFuturo(int dias) {
        usuario("Cooperativa", 10);

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarDados(
                1,
                dados(
                    null,
                    null,
                    LocalDate.now().plusDays(dias),
                    null,
                    null,
                    null
                )
            )
        );

        assertEquals("BIRTH_DATE_INVALID", erro.getCodigoErro());
        nenhumaEscrita();
    }

    @Test
    void deveCriarTelefoneQuandoUsuarioNaoPossuiTelefoneAtivo() {
        usuario("Usuário comum", 10);
        when(gateway.buscarTelefonesAtivos(1)).thenReturn(List.of());

        service.atualizarDados(
            1,
            dados(null, null, null, null, "11999999999", null)
        );

        verify(gateway).criarTelefone(1, "11999999999");
        verify(gateway, never()).atualizarTelefone(any(), any(), any());
    }

    @Test
    void deveAtualizarUnicoTelefoneSemExigirId() {
        usuario("Usuário comum", 10);
        when(gateway.buscarTelefonesAtivos(1)).thenReturn(List.of(50));

        service.atualizarDados(
            1,
            dados(null, null, null, null, "11999999999", null)
        );

        verify(gateway).atualizarTelefone(1, 50, "11999999999");
        verify(gateway, never()).criarTelefone(any(), any());
    }

    @Test
    void deveAtualizarTelefoneSelecionadoEntreVarios() {
        usuario("Usuário comum", 10);
        when(gateway.buscarTelefonesAtivos(1)).thenReturn(List.of(50, 51));

        service.atualizarDados(
            1,
            dados(null, null, null, null, "11999999999", 51)
        );

        verify(gateway).atualizarTelefone(1, 51, "11999999999");
        verify(gateway, never()).atualizarTelefone(eq(1), eq(50), any());
    }

    @Test
    void deveExigirIdQuandoHaVariosTelefones() {
        usuario("Usuário comum", 10);
        when(gateway.buscarTelefonesAtivos(1)).thenReturn(List.of(50, 51));

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarDados(
                1,
                dados(null, null, null, null, "11999999999", null)
            )
        );

        assertEquals("PHONE_ID_REQUIRED", erro.getCodigoErro());
        nenhumaEscrita();
    }

    @Test
    void deveRejeitarTelefoneDeOutroUsuarioOuInativo() {
        usuario("Usuário comum", 10);
        when(gateway.buscarTelefonesAtivos(1)).thenReturn(List.of(50));

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarDados(
                1,
                dados(null, null, null, null, "11999999999", 99)
            )
        );

        nenhumaEscrita();
    }

    @Test
    void deveExigirNumeroQuandoTelefoneIdFoiEnviado() {
        usuario("Usuário comum", 10);

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarDados(
                1,
                dados("Novo nome", null, null, null, null, 50)
            )
        );

        assertEquals("PHONE_REQUIRED", erro.getCodigoErro());
        nenhumaEscrita();
    }

    @ParameterizedTest
    @ValueSource(strings = {"comum", "Usuário comum", "USUARIO_COMUM"})
    void deveAtualizarEnderecoExclusivoDoUsuarioComum(String tipo) {
        usuario(tipo, 10);

        when(gateway.enderecoExclusivoDoUsuario(1, 10)).thenReturn(true);
        when(cepGateway.buscarEnderecoPorCep("05280130"))
            .thenReturn(enderecoConsultado());

        service.atualizarMeuEndereco(1, "05280-130", " 41 ", "Casa");

        ArgumentCaptor<Endereco> captor =
            ArgumentCaptor.forClass(Endereco.class);

        verify(gateway).atualizarEndereco(captor.capture());

        Endereco salvo = captor.getValue();

        assertEquals(10, salvo.getIdEndereco());
        assertEquals("05280130", salvo.getCep());
        assertEquals("SP", salvo.getEstado());
        assertEquals("São Paulo", salvo.getCidade());
        assertEquals("Rua das Flores", salvo.getLogradouro());
        assertEquals("41", salvo.getNumero());
        assertEquals("Casa", salvo.getComplemento());
    }

    @Test
    void deveRejeitarEnderecoCompartilhadoDoUsuarioComum() {
        usuario("Usuário comum", 10);

        when(gateway.enderecoExclusivoDoUsuario(1, 10)).thenReturn(false);

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @Test
    void deveRejeitarUsuarioComumSemEndereco() {
        usuario("Usuário comum", null);

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
        "Morador",
        "Morador dependente",
        "Síndico",
        "Administrador",
        "Tipo desconhecido"
    })
    void deveBloquearTiposNaoPermitidosNoEnderecoPessoal(String tipo) {
        usuario(tipo, 10);

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @Test
    void deveAtualizarEnderecoVinculadoACooperativa() {
        usuario("Cooperativa", 10);

        when(gateway.buscarEnderecoCooperativa(1))
            .thenReturn(Optional.of(10));

        when(cepGateway.buscarEnderecoPorCep("05280130"))
            .thenReturn(enderecoConsultado());

        service.atualizarMeuEndereco(1, "05280130", "41", null);

        verify(gateway).atualizarEndereco(
            argThat(endereco ->
                endereco.getIdEndereco().equals(10)
                && endereco.getComplemento() == null
            )
        );

        verify(gateway, never()).enderecoExclusivoDoUsuario(any(), any());
    }

    @Test
    void deveRejeitarCooperativaSemVinculo() {
        usuario("Cooperativa", 10);

        when(gateway.buscarEnderecoCooperativa(1))
            .thenReturn(Optional.empty());

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @Test
    void deveRejeitarDivergenciaEntreEnderecoDoUsuarioEDaCooperativa() {
        usuario("Cooperativa", 10);

        when(gateway.buscarEnderecoCooperativa(1))
            .thenReturn(Optional.of(20));

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        assertEquals("ADDRESS_LINK_INCONSISTENT", erro.getCodigoErro());
        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Síndico", "SINDICO"})
    void devePermitirSindicoResponsavelAlterarCondominio(String tipo) {
        usuario(tipo, 10);

        when(gateway.buscarEnderecoCondominioDoSindico(1, 100))
            .thenReturn(Optional.of(20));

        when(cepGateway.buscarEnderecoPorCep("05280130"))
            .thenReturn(enderecoConsultado());

        service.atualizarEnderecoCondominio(
            1, 100, "05280130", "41", "Bloco A"
        );

        verify(gateway).atualizarEndereco(
            argThat(endereco ->
                endereco.getIdEndereco().equals(20)
                && "Bloco A".equals(endereco.getComplemento())
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Usuário comum", "Morador", "Cooperativa", "Administrador"
    })
    void deveRejeitarNaoSindicoNoEnderecoDoCondominio(String tipo) {
        usuario(tipo, 10);

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarEnderecoCondominio(
                1, 100, "05280130", "41", null
            )
        );

        verify(gateway, never())
            .buscarEnderecoCondominioDoSindico(any(), any());

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    @Test
    void deveRejeitarCondominioNaoVinculadoAoSindico() {
        usuario("Síndico", 10);

        when(gateway.buscarEnderecoCondominioDoSindico(1, 100))
            .thenReturn(Optional.empty());

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarEnderecoCondominio(
                1, 100, "05280130", "41", null
            )
        );

        nenhumaEscrita();
        verifyNoInteractions(cepGateway);
    }

    private AtualizacaoCondominio condominio(
        String nome,
        Integer tipoCondominioId,
        String urlFoto
    ) {
        return new AtualizacaoCondominio(nome, tipoCondominioId, urlFoto);
    }

    private void nenhumaEscritaNoCondominio() {
        verify(gateway, never()).atualizarCondominio(any(), any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Síndico", "SINDICO"})
    void devePermitirSindicoResponsavelAtualizarOCondominio(String tipo) {
        usuario(tipo, 10);

        when(gateway.condominioAtivoDoSindico(1, 100)).thenReturn(true);
        when(gateway.tipoCondominioExiste(2)).thenReturn(true);

        service.atualizarCondominio(
            1,
            100,
            condominio("  Residencial Eco  ", 2, "https://exemplo.com/foto.jpg")
        );

        ArgumentCaptor<AtualizacaoCondominio> captor =
            ArgumentCaptor.forClass(AtualizacaoCondominio.class);

        verify(gateway).atualizarCondominio(eq(100), captor.capture());

        assertEquals("Residencial Eco", captor.getValue().nome());
        assertEquals(2, captor.getValue().tipoCondominioId());
        assertEquals(
            "https://exemplo.com/foto.jpg",
            captor.getValue().urlFoto()
        );
    }

    @Test
    void deveAtualizarSoOQueFoiInformadoNoCondominio() {
        usuario("Síndico", 10);

        when(gateway.condominioAtivoDoSindico(1, 100)).thenReturn(true);

        service.atualizarCondominio(
            1, 100, condominio("Novo nome", null, null)
        );

        verify(gateway).atualizarCondominio(
            eq(100),
            argThat(dados ->
                "Novo nome".equals(dados.nome())
                && dados.tipoCondominioId() == null
                && dados.urlFoto() == null
            )
        );

        verify(gateway, never()).tipoCondominioExiste(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Usuário comum", "Morador", "Cooperativa", "Administrador"
    })
    void deveRejeitarNaoSindicoNoCondominio(String tipo) {
        usuario(tipo, 10);

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarCondominio(
                1, 100, condominio("Novo nome", null, null)
            )
        );

        verify(gateway, never()).condominioAtivoDoSindico(any(), any());
        nenhumaEscritaNoCondominio();
    }

    @Test
    void deveRejeitarCondominioDeOutroSindicoOuInativo() {
        usuario("Síndico", 10);

        when(gateway.condominioAtivoDoSindico(1, 100)).thenReturn(false);

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarCondominio(
                1, 100, condominio("Novo nome", null, null)
            )
        );

        nenhumaEscritaNoCondominio();
    }

    @Test
    void deveRejeitarUsuarioInexistenteNoCondominio() {
        when(gateway.buscarUsuarioAtivo(1)).thenReturn(Optional.empty());

        assertThrows(
            AtualizacaoNaoPermitidaException.class,
            () -> service.atualizarCondominio(
                1, 100, condominio("Novo nome", null, null)
            )
        );

        nenhumaEscritaNoCondominio();
    }

    @Test
    void deveExigirPeloMenosUmCampoNoCondominio() {
        usuario("Síndico", 10);

        when(gateway.condominioAtivoDoSindico(1, 100)).thenReturn(true);

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarCondominio(
                1, 100, condominio(null, null, null)
            )
        );

        assertEquals("EMPTY_UPDATE", erro.getCodigoErro());
        nenhumaEscritaNoCondominio();
    }

    @Test
    void deveRejeitarTipoDeCondominioInexistente() {
        usuario("Síndico", 10);

        when(gateway.condominioAtivoDoSindico(1, 100)).thenReturn(true);
        when(gateway.tipoCondominioExiste(99)).thenReturn(false);

        BusinessException erro = assertThrows(
            BusinessException.class,
            () -> service.atualizarCondominio(
                1, 100, condominio("Novo nome", 99, null)
            )
        );

        assertEquals("CONDOMINIUM_TYPE_NOT_FOUND", erro.getCodigoErro());
        nenhumaEscritaNoCondominio();
    }

    @Test
    void devePropagarFalhaDoCepSemSalvarEndereco() {
        usuario("Usuário comum", 10);

        when(gateway.enderecoExclusivoDoUsuario(1, 10)).thenReturn(true);

        CepException falha = new CepException(
            "CEP_INVALIDO", "CEP não encontrado"
        );

        when(cepGateway.buscarEnderecoPorCep("05280130"))
            .thenThrow(falha);

        CepException recebido = assertThrows(
            CepException.class,
            () -> service.atualizarMeuEndereco(1, "05280130", "41", null)
        );

        assertSame(falha, recebido);
        nenhumaEscrita();
    }
}