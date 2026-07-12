package br.com.escola.responsavel.application.usecase.consulta;

import org.springframework.stereotype.Service;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaAlunoResponsaveisResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaConsultaCadastralPage;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResponsavelResumo;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaConsultaPort;
import br.com.escola.responsavel.application.dto.consulta.AlunoComResponsaveisOutput;
import br.com.escola.responsavel.application.dto.consulta.ConsultaCadastralPageOutput;
import br.com.escola.responsavel.application.dto.consulta.ResponsavelResumoOutput;

@Service
public class ConsultarCadastroAlunoResponsavelUseCase {

    private final PessoaConsultaPort pessoaConsultaPort;

    public ConsultarCadastroAlunoResponsavelUseCase(PessoaConsultaPort pessoaConsultaPort) {
        this.pessoaConsultaPort = pessoaConsultaPort;
    }

    public ConsultaCadastralPageOutput executar(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        PessoaConsultaCadastralPage consulta = pessoaConsultaPort.consultarCadastroAlunoResponsavel(
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);

        return new ConsultaCadastralPageOutput(
                consulta.content().stream()
                        .map(this::toOutput)
                        .toList(),
                consulta.totalElements(),
                consulta.page(),
                consulta.size());
    }

    private AlunoComResponsaveisOutput toOutput(PessoaAlunoResponsaveisResumo resumo) {
        return new AlunoComResponsaveisOutput(
                resumo.idAluno(),
                resumo.nomeCompleto(),
                resumo.cpf(),
                resumo.email(),
                resumo.telefone(),
                resumo.dataNascimento(),
                resumo.createdAt(),
                resumo.responsaveis().stream()
                        .map(this::toOutput)
                        .toList());
    }

    private ResponsavelResumoOutput toOutput(PessoaResponsavelResumo resumo) {
        return new ResponsavelResumoOutput(
                resumo.id(),
                resumo.nomeCompleto(),
                resumo.cpf(),
                resumo.email(),
                resumo.telefone(),
                resumo.createdAt());
    }
}
