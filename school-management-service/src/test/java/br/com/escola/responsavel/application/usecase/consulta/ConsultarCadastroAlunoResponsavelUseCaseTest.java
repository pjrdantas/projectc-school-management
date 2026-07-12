package br.com.escola.responsavel.application.usecase.consulta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaAlunoResponsaveisResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaConsultaCadastralPage;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResponsavelResumo;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaConsultaPort;

@ExtendWith(MockitoExtension.class)
class ConsultarCadastroAlunoResponsavelUseCaseTest {

    @Mock
    private PessoaConsultaPort pessoaConsultaPort;

    private ConsultarCadastroAlunoResponsavelUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConsultarCadastroAlunoResponsavelUseCase(pessoaConsultaPort);
    }

    @Test
    void deveConsultarCadastroViaContratoInternoDePessoaSemExporEntidadeJpa() {
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        LocalDate nascimento = LocalDate.of(2015, 3, 10);
        LocalDateTime criadoEm = LocalDateTime.of(2026, 7, 2, 10, 0);

        when(pessoaConsultaPort.consultarCadastroAlunoResponsavel(
                "Ana",
                "11122233344",
                "Maria",
                "55566677788",
                0,
                20))
                .thenReturn(new PessoaConsultaCadastralPage(
                        List.of(new PessoaAlunoResponsaveisResumo(
                                alunoId,
                                "Ana Silva",
                                "11122233344",
                                "ana@example.com",
                                "11999990000",
                                nascimento,
                                criadoEm,
                                List.of(new PessoaResponsavelResumo(
                                        responsavelId,
                                        "Maria Silva",
                                        "55566677788",
                                        "maria@example.com",
                                        "11888880000",
                                        criadoEm)))),
                        1,
                        0,
                        20));

        var output = useCase.executar("Ana", "11122233344", "Maria", "55566677788", 0, 20);

        assertThat(output.totalElements()).isEqualTo(1);
        assertThat(output.page()).isZero();
        assertThat(output.size()).isEqualTo(20);
        assertThat(output.content()).hasSize(1);
        assertThat(output.content().get(0).idAluno()).isEqualTo(alunoId);
        assertThat(output.content().get(0).nomeCompleto()).isEqualTo("Ana Silva");
        assertThat(output.content().get(0).dataNascimento()).isEqualTo(nascimento);
        assertThat(output.content().get(0).responsaveis()).hasSize(1);
        assertThat(output.content().get(0).responsaveis().get(0).id()).isEqualTo(responsavelId);
        assertThat(output.content().get(0).responsaveis().get(0).nomeCompleto()).isEqualTo("Maria Silva");
        verify(pessoaConsultaPort).consultarCadastroAlunoResponsavel(
                "Ana",
                "11122233344",
                "Maria",
                "55566677788",
                0,
                20);
    }
}
