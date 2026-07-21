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
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaQueryService implements PessoaQueryUseCase {

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
        List<PessoaCatalogoResponse> response = catalogoPort.listarTiposPessoa();
        registrarLeituraLocal("listarTiposPessoa", "success");
        return response;
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
        readRoutingPolicy.registrarDecisao("listarTiposEndereco");
        List<PessoaCatalogoResponse> response = catalogoPort.listarTiposEndereco();
        registrarLeituraLocal("listarTiposEndereco", "success");
        return response;
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
        readRoutingPolicy.registrarDecisao("buscarPorId");
        return pessoaPort.buscarPessoaPorId(pessoaId, context.escolaId())
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
        readRoutingPolicy.registrarDecisao("consultarCadastro");
        PessoaConsultaCadastralPageResponse response = alunoResponsavelPort.consultarCadastro(
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
        registrarLeituraAlunoResponsavelLocal("consultarCadastro", "success");
        return response;
    }

    @Override
    public List<PessoaResponsavelVinculadoResponse> listarResponsaveisPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        readRoutingPolicy.registrarDecisao("listarResponsaveisPorAluno");
        return alunoResponsavelPort.listarResponsaveisPorAluno(alunoId)
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
}


