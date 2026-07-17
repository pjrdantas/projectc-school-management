package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;
import br.com.escola.peopleservice.application.dto.PessoaProfessorResumoResponse;
import br.com.escola.peopleservice.application.dto.PessoaResponsavelVinculadoResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.peopleservice.application.port.in.PessoaQueryUseCase;
import br.com.escola.peopleservice.application.port.out.PessoaCatalogoPort;
import br.com.escola.peopleservice.application.port.out.PessoaPort;
import br.com.escola.peopleservice.application.port.out.AlunoResponsavelPort;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaQueryService implements PessoaQueryUseCase {

    private final PessoaReadPort pessoaReadPort;
    private final PessoaCatalogoPort catalogoPort;
    private final PessoaPort pessoaPort;
    private final AlunoResponsavelPort alunoResponsavelPort;
    private final PessoaAlunoResponsavelCatalogoService pessoaAlunoResponsavelCatalogoService;
    private final PessoaEnderecoService pessoaEnderecoService;
    private final PessoaContatoService pessoaContatoService;
    private final PessoaDocumentoMetadataService pessoaDocumentoMetadataService;
    private final PessoaFuncionarioResumoService pessoaFuncionarioResumoService;
    private final PessoaProfessorResumoService pessoaProfessorResumoService;
    private final OrigemLeituraPolicy readRoutingPolicy;
    private final MeterRegistry meterRegistry;

    public PessoaQueryService(
            PessoaReadPort pessoaReadPort,
            PessoaCatalogoPort catalogoPort,
            PessoaPort pessoaPort,
            AlunoResponsavelPort alunoResponsavelPort,
            PessoaAlunoResponsavelCatalogoService pessoaAlunoResponsavelCatalogoService,
            PessoaEnderecoService pessoaEnderecoService,
            PessoaContatoService pessoaContatoService,
            PessoaDocumentoMetadataService pessoaDocumentoMetadataService,
            PessoaFuncionarioResumoService pessoaFuncionarioResumoService,
            PessoaProfessorResumoService pessoaProfessorResumoService,
            OrigemLeituraPolicy readRoutingPolicy,
            MeterRegistry meterRegistry) {
        this.pessoaReadPort = pessoaReadPort;
        this.catalogoPort = catalogoPort;
        this.pessoaPort = pessoaPort;
        this.alunoResponsavelPort = alunoResponsavelPort;
        this.pessoaAlunoResponsavelCatalogoService = pessoaAlunoResponsavelCatalogoService;
        this.pessoaEnderecoService = pessoaEnderecoService;
        this.pessoaContatoService = pessoaContatoService;
        this.pessoaDocumentoMetadataService = pessoaDocumentoMetadataService;
        this.pessoaFuncionarioResumoService = pessoaFuncionarioResumoService;
        this.pessoaProfessorResumoService = pessoaProfessorResumoService;
        this.readRoutingPolicy = readRoutingPolicy;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context) {
        var decision = readRoutingPolicy.registrarDecisao("listarTiposPessoa");
        if (decision.localReadEligible()) {
            try {
                List<PessoaCatalogoResponse> response = catalogoPort.listarTiposPessoa();
                registrarLeituraLocal("listarTiposPessoa", "success");
                return response;
            } catch (RuntimeException ex) {
                registrarLeituraLocal("listarTiposPessoa", "fallback");
            }
        }
        return pessoaReadPort.listarTiposPessoa(authorization, context);
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
        var decision = readRoutingPolicy.registrarDecisao("listarTiposEndereco");
        if (decision.localReadEligible()) {
            try {
                List<PessoaCatalogoResponse> response = catalogoPort.listarTiposEndereco();
                registrarLeituraLocal("listarTiposEndereco", "success");
                return response;
            } catch (RuntimeException ex) {
                registrarLeituraLocal("listarTiposEndereco", "fallback");
            }
        }
        return pessoaReadPort.listarTiposEndereco(authorization, context);
    }

    @Override
    public List<PessoaCatalogoResponse> listarStatusAluno(String authorization, InternalRequestContext context) {
        return pessoaAlunoResponsavelCatalogoService.listarStatusAluno();
    }

    @Override
    public List<PessoaCatalogoResponse> listarParentescos(String authorization, InternalRequestContext context) {
        return pessoaAlunoResponsavelCatalogoService.listarParentescos();
    }

    @Override
    public PessoaEnderecoResponse buscarEnderecoPrincipalPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId) {
        return pessoaEnderecoService.buscarEnderecoPrincipalPorPessoa(pessoaId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Endereco principal nao encontrado"));
    }

    @Override
    public List<PessoaEnderecoResponse> listarEnderecosPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId) {
        return pessoaEnderecoService.listarEnderecosPorPessoa(pessoaId, context.escolaId());
    }

    @Override
    public PessoaContatoResponse buscarContatoPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId) {
        return pessoaContatoService.buscarContatoPorPessoa(pessoaId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Contato nao encontrado"));
    }

    @Override
    public PessoaDocumentoMetadataResponse buscarDocumentoPorId(
            String authorization,
            InternalRequestContext context,
            UUID documentoId) {
        return pessoaDocumentoMetadataService.buscarDocumentoPorId(documentoId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento nao encontrado"));
    }

    @Override
    public List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId) {
        return pessoaDocumentoMetadataService.listarDocumentosPorPessoa(pessoaId, context.escolaId());
    }

    @Override
    public PessoaFuncionarioResumoResponse buscarFuncionarioPorId(
            String authorization,
            InternalRequestContext context,
            UUID funcionarioId) {
        return pessoaFuncionarioResumoService.buscarFuncionarioPorId(funcionarioId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Funcionario nao encontrado"));
    }

    @Override
    public List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(
            String authorization,
            InternalRequestContext context) {
        return pessoaFuncionarioResumoService.listarFuncionariosAtivosPorEscola(context.escolaId());
    }

    @Override
    public PessoaProfessorResumoResponse buscarProfessorPorId(
            String authorization,
            InternalRequestContext context,
            UUID professorId) {
        return pessoaProfessorResumoService.buscarProfessorPorId(professorId, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Professor nao encontrado"));
    }

    @Override
    public List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(
            String authorization,
            InternalRequestContext context) {
        return pessoaProfessorResumoService.listarProfessoresPorEscola(context.escolaId());
    }

    @Override
    public PessoaResumoResponse buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId) {
        var decision = readRoutingPolicy.registrarDecisao("buscarPorId");
        if (decision.localReadEligible()) {
            try {
                var localResponse = pessoaPort.buscarPessoaPorId(pessoaId, context.escolaId());
                if (localResponse.isPresent()) {
                    registrarLeituraIdentidadeLocal("buscarPorId", "success");
                    return localResponse.get();
                }
                registrarLeituraIdentidadeLocal("buscarPorId", "fallback_not_found");
            } catch (RuntimeException ex) {
                registrarLeituraIdentidadeLocal("buscarPorId", "fallback_error");
            }
        }
        return pessoaReadPort.buscarPessoaPorId(authorization, context, pessoaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pessoa nao encontrada"));
    }

    @Override
    public PessoaConsultaCadastralPageResponse consultarCadastro(
            String authorization,
            InternalRequestContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        var decision = readRoutingPolicy.registrarDecisao("consultarCadastro");
        if (decision.localReadEligible()) {
            try {
                PessoaConsultaCadastralPageResponse response = alunoResponsavelPort.consultarCadastro(
                        nomeAluno,
                        cpfAluno,
                        nomeResponsavel,
                        cpfResponsavel,
                        page,
                        size);
                registrarLeituraAlunoResponsavelLocal("consultarCadastro", "success");
                return response;
            } catch (RuntimeException ex) {
                registrarLeituraAlunoResponsavelLocal("consultarCadastro", "fallback_error");
            }
        }
        return pessoaReadPort.consultarCadastro(
                authorization,
                context,
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
    }

    @Override
    public List<PessoaResponsavelVinculadoResponse> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        var decision = readRoutingPolicy.registrarDecisao("listarResponsaveisPorAluno");
        if (decision.localReadEligible()) {
            try {
                var localResponse = alunoResponsavelPort.listarResponsaveisPorAluno(alunoId);
                if (localResponse.isPresent()) {
                    registrarLeituraAlunoResponsavelLocal("listarResponsaveisPorAluno", "success");
                    return localResponse.get();
                }
                registrarLeituraAlunoResponsavelLocal("listarResponsaveisPorAluno", "fallback_not_found");
            } catch (RuntimeException ex) {
                registrarLeituraAlunoResponsavelLocal("listarResponsaveisPorAluno", "fallback_error");
            }
        }
        return pessoaReadPort.listarResponsaveisPorAluno(authorization, context, alunoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Aluno nao encontrado"));
    }

    private void registrarLeituraLocal(String operation, String result) {
        meterRegistry.counter(
                "people.catalog.reads",
                "operation", operation,
                "result", result)
                .increment();
    }

    private void registrarLeituraAlunoResponsavelLocal(String operation, String result) {
        meterRegistry.counter(
                "people.studentresponsible.reads",
                "operation", operation,
                "result", result)
                .increment();
    }

    private void registrarLeituraIdentidadeLocal(String operation, String result) {
        meterRegistry.counter(
                "people.identity.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}


