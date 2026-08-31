package br.com.ecociente.cadastro.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.ecociente.cadastro.core.Service.CadastrarUsuarioComumService;
import br.com.ecociente.cadastro.core.domain.Endereco;
import br.com.ecociente.cadastro.core.domain.Usuario;
import br.com.ecociente.cadastro.core.exception.AlreadyExistsException;
import br.com.ecociente.cadastro.core.exception.BusinessException;
import br.com.ecociente.cadastro.core.gateway.CepGateway;
import br.com.ecociente.cadastro.core.gateway.EnderecoGateway;
import br.com.ecociente.cadastro.core.gateway.UsuarioGateway;
import br.com.ecociente.cadastro.core.usecase.output.CadastroUsuarioComumOutput;

@ExtendWith(MockitoExtension.class)
class CadastrarUsuarioComumServiceTest {

    @Mock
    private UsuarioGateway usuarioGateway;

    @Mock
    private EnderecoGateway enderecoGateway;

    @Mock
    private CepGateway cepGateway;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CadastrarUsuarioComumService service;

    private Endereco enderecoCep;
    private Endereco enderecoSalvo;
    private Usuario usuarioSalvo;

    @BeforeEach
    void setUp() {

        enderecoCep = Endereco.builder()
                .cep("05280130")
                .estado("SP")
                .cidade("São Paulo")
                .logradouro("Rua das Flores")
                .build();

        enderecoSalvo = Endereco.builder()
                .idEndereco(1)
                .cep("05280130")
                .estado("SP")
                .cidade("São Paulo")
                .logradouro("Rua das Flores")
                .numero("41")
                .complemento("Apto 02")
                .build();

        usuarioSalvo = Usuario.builder()
                .idUsuario(10)
                .nomeUsuario("Emanuelly Mendes")
                .emaiUsuario("emanuelly@gmail.com")
                .senhaHash("hash")
                .dataNascimento(LocalDate.of(2000, 3, 12))
                .cpf("63082449026")
                .ativo(true)
                .registroEm(OffsetDateTime.now())
                .tipoUsuarioId(1)
                .enderecoId(1)
                .build();
    }

    @Nested
    @DisplayName("executar - Cadastro de usuário comum")
    class Executar {

        @Test
        @DisplayName("Deve cadastrar usuário com sucesso quando dados válidos")
        void shouldCadastrarUsuarioComSucesso() {

            when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
            when(usuarioGateway.existePorCpf(anyString())).thenReturn(false);
            when(cepGateway.buscarEnderecoPorCep(anyString())).thenReturn(enderecoCep);
            when(enderecoGateway.salvar(any(Endereco.class))).thenReturn(enderecoSalvo);
            when(passwordEncoder.encode(anyString())).thenReturn("hash");
            when(usuarioGateway.salvar(any(Usuario.class))).thenReturn(usuarioSalvo);

            CadastroUsuarioComumOutput resultado = service.executar(
                    "Emanuelly Mendes",
                    "emanuelly@gmail.com",
                    "Senha@T3ste",
                    LocalDate.of(2000, 3, 12),
                    "630.824.490-26",
                    "05280130",
                    "41",
                    "Apto 02"
            );

            assertNotNull(resultado);
            assertEquals(10, resultado.usuario().getIdUsuario());
            assertEquals("05280130", resultado.endereco().getCep());

            verify(usuarioGateway).salvar(any(Usuario.class));
            verify(enderecoGateway).salvar(any(Endereco.class));
        }

        @Test
        @DisplayName("Deve lançar AlreadyExistsException quando email já cadastrado")
        void shouldThrowWhenEmailAlreadyExists() {

            when(usuarioGateway.existePorEmail(anyString())).thenReturn(true);

            AlreadyExistsException ex = assertThrows(
                    AlreadyExistsException.class,
                    () -> service.executar(
                            "Emanuelly Mendes",
                            "emanuelly@gmail.com",
                            "Senha@T3ste",
                            LocalDate.of(2000, 3, 12),
                            "630.824.490-26",
                            "05280130",
                            "41",
                            "Apto 02"
                    )
            );

            assertEquals("EMAIL_ALREADY_EXISTS", ex.getCodigoErro());

            verify(usuarioGateway, never()).salvar(any());
            verify(enderecoGateway, never()).salvar(any());
        }

        @Test
        @DisplayName("Deve lançar AlreadyExistsException quando CPF já cadastrado")
        void shouldThrowWhenCpfAlreadyExists() {

            when(usuarioGateway.existePorEmail(anyString())).thenReturn(false);
            when(usuarioGateway.existePorCpf(anyString())).thenReturn(true);

            AlreadyExistsException ex = assertThrows(
                    AlreadyExistsException.class,
                    () -> service.executar(
                            "Emanuelly Mendes",
                            "emanuelly@gmail.com",
                            "Senha@T3ste",
                            LocalDate.of(2000, 3, 12),
                            "630.824.490-26",
                            "05280130",
                            "41",
                            "Apto 02"
                    )
            );

            assertEquals("CPF_ALREADY_EXISTS", ex.getCodigoErro());

            verify(usuarioGateway, never()).salvar(any());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando data de nascimento for futura")
        void shouldThrowWhenDataNascimentoFutura() {

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.executar(
                            "Emanuelly Mendes",
                            "emanuelly@gmail.com",
                            "Senha@T3ste",
                            LocalDate.now().plusDays(1),
                            "630.824.490-26",
                            "05280130",
                            "41",
                            "Apto 02"
                    )
            );

            assertEquals("VALIDATION_ERROR", ex.getCodigoErro());

            verify(usuarioGateway, never()).existePorEmail(anyString());
        }

        @Test
        @DisplayName("Deve lançar BusinessException quando menor de idade")
        void shouldThrowWhenMenorDeIdade() {

            BusinessException ex = assertThrows(
                    BusinessException.class,
                    () -> service.executar(
                            "Emanuelly Mendes",
                            "emanuelly@gmail.com",
                            "Senha@T3ste",
                            LocalDate.now().minusYears(17),
                            "630.824.490-26",
                            "05280130",
                            "41",
                            "Apto 02"
                    )
            );

            assertEquals("VALIDATION_ERROR", ex.getCodigoErro());

            verify(usuarioGateway, never()).existePorEmail(anyString());
        }
    }
}