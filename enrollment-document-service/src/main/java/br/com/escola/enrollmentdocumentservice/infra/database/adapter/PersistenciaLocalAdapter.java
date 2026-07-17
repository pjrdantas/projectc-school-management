package br.com.escola.enrollmentdocumentservice.infra.database.adapter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.EscolaOrigemResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaEtapaResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.TransferenciaAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.exception.ConflitoNegocioException;
import br.com.escola.enrollmentdocumentservice.application.exception.RecursoNaoEncontradoException;
import br.com.escola.enrollmentdocumentservice.application.port.out.EnrollmentTransferPort;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAdministrativoJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAlunoJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.EscolaOrigemJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaEtapaJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.TransferenciaJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.DocumentoAdministrativoJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.DocumentoAlunoJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.EscolaOrigemJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.MatriculaEtapaJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.MatriculaJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.TransferenciaJpaRepository;

@Repository
@Transactional
public class PersistenciaLocalAdapter implements EnrollmentTransferPort {

    private final EscolaOrigemJpaRepository escolaOrigemRepository;
    private final TransferenciaJpaRepository transferenciaRepository;
    private final DocumentoAlunoJpaRepository documentoAlunoRepository;
    private final DocumentoAdministrativoJpaRepository documentoAdministrativoRepository;
    private final MatriculaJpaRepository matriculaRepository;
    private final MatriculaEtapaJpaRepository matriculaEtapaRepository;

    public PersistenciaLocalAdapter(
            EscolaOrigemJpaRepository escolaOrigemRepository,
            TransferenciaJpaRepository transferenciaRepository,
            DocumentoAlunoJpaRepository documentoAlunoRepository,
            DocumentoAdministrativoJpaRepository documentoAdministrativoRepository,
            MatriculaJpaRepository matriculaRepository,
            MatriculaEtapaJpaRepository matriculaEtapaRepository) {
        this.escolaOrigemRepository = escolaOrigemRepository;
        this.transferenciaRepository = transferenciaRepository;
        this.documentoAlunoRepository = documentoAlunoRepository;
        this.documentoAdministrativoRepository = documentoAdministrativoRepository;
        this.matriculaRepository = matriculaRepository;
        this.matriculaEtapaRepository = matriculaEtapaRepository;
    }

    @Override
    public EscolaOrigemResponse criarEscolaOrigem(String authorization, InternalRequestContext context, EscolaOrigemRequest request) {
        var now = LocalDateTime.now();
        EscolaOrigemJpaEntity entity = new EscolaOrigemJpaEntity(
                UUID.randomUUID(),
                context.escolaId(),
                request.nomeEscola(),
                request.codigoInep(),
                request.cnpj(),
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.complemento(),
                request.bairro(),
                request.cidade(),
                request.uf(),
                now);
        escolaOrigemRepository.save(entity);
        return toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscolaOrigemResponse> listarEscolasOrigem(String authorization, InternalRequestContext context) {
        return escolaOrigemRepository.findAllBySchoolIdOrderByNomeEscolaAscIdAsc(context.escolaId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EscolaOrigemResponse buscarEscolaOrigem(String authorization, InternalRequestContext context, UUID id) {
        return escolaOrigemRepository.findByIdAndSchoolId(id, context.escolaId())
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escola de origem nao encontrada"));
    }

    @Override
    public TransferenciaAlunoResponse criarTransferencia(
            String authorization,
            InternalRequestContext context,
            TransferenciaAlunoRequest request) {
        EscolaOrigemJpaEntity escolaOrigem = resolverEscolaOrigem(context, request);
        var now = LocalDateTime.now();
        TransferenciaJpaEntity entity = new TransferenciaJpaEntity(
                UUID.randomUUID(),
                context.escolaId(),
                request.alunoId(),
                escolaOrigem.getId(),
                request.serieOrigem(),
                request.anoLetivoOrigem(),
                request.dataTransferencia(),
                request.motivoTransferencia(),
                request.situacaoOrigem(),
                request.documentosEntregues(),
                request.observacao(),
                request.tipoTransferencia(),
                request.statusTransferencia(),
                request.usuarioOperacao(),
                now,
                now);
        transferenciaRepository.save(entity);
        return toResponse(entity, escolaOrigem);
    }

    @Override
    @Transactional(readOnly = true)
    public TransferenciaAlunoResponse buscarTransferencia(String authorization, InternalRequestContext context, UUID id) {
        TransferenciaJpaEntity entity = transferenciaRepository.findByIdAndSchoolId(id, context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Transferencia nao encontrada"));
        EscolaOrigemJpaEntity escolaOrigem = escolaOrigemRepository.findByIdAndSchoolId(entity.getEscolaOrigemId(), context.escolaId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Escola de origem nao encontrada"));
        return toResponse(entity, escolaOrigem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaAlunoResponse> listarTransferenciasPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        return transferenciaRepository.findAllBySchoolIdAndAlunoIdOrderByCreatedAtDescIdAsc(context.escolaId(), alunoId).stream()
                .map(entity -> toResponse(entity, escolaOrigemRepository.findByIdAndSchoolId(entity.getEscolaOrigemId(), context.escolaId())
                        .orElseThrow(() -> new RecursoNaoEncontradoException("Escola de origem nao encontrada"))))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoAlunoResponse> listarDocumentosPorAluno(
            String authorization,
            InternalRequestContext context,
            UUID alunoId) {
        return documentoAlunoRepository.findAllBySchoolIdAndAlunoIdOrderByDataUploadDescIdAsc(context.escolaId(), alunoId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoAlunoResponse buscarDocumentoAlunoPorId(String authorization, InternalRequestContext context, UUID id) {
        return documentoAlunoRepository.findByIdAndSchoolId(id, context.escolaId())
                .map(this::toResponse)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Documento do aluno nao encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaResponse> listarMatriculas(
            String authorization,
            InternalRequestContext context,
            UUID alunoId,
            UUID turmaId,
            UUID periodoLetivoId,
            String status) {
        return matriculaRepository.findAllBySchoolIdOrderByCreatedAtDescIdAsc(context.escolaId()).stream()
                .filter(entity -> alunoId == null || alunoId.equals(entity.getAlunoId()))
                .filter(entity -> turmaId == null || turmaId.equals(entity.getTurmaId()))
                .filter(entity -> periodoLetivoId == null || periodoLetivoId.equals(entity.getPeriodoLetivoId()))
                .filter(entity -> status == null || status.equalsIgnoreCase(entity.getStatus()))
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoResponse> listarDocumentosPorEntidade(
            String authorization,
            InternalRequestContext context,
            String entidadeTipo,
            UUID entidadeId) {
        return documentoAdministrativoRepository
                .findAllBySchoolIdAndEntidadeTipoAndEntidadeIdOrderByDataUploadDescIdAsc(
                        context.escolaId(),
                        entidadeTipo,
                        entidadeId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private EscolaOrigemJpaEntity resolverEscolaOrigem(InternalRequestContext context, TransferenciaAlunoRequest request) {
        if (request.escolaOrigemId() != null) {
            return escolaOrigemRepository.findByIdAndSchoolId(request.escolaOrigemId(), context.escolaId())
                    .orElseThrow(() -> new RecursoNaoEncontradoException("Escola de origem nao encontrada"));
        }
        if (request.escolaOrigem() == null) {
            throw new ConflitoNegocioException("Transferencia exige escolaOrigemId ou escolaOrigem");
        }
        var now = LocalDateTime.now();
        EscolaOrigemJpaEntity novaEscolaOrigem = new EscolaOrigemJpaEntity(
                UUID.randomUUID(),
                context.escolaId(),
                request.escolaOrigem().nomeEscola(),
                request.escolaOrigem().codigoInep(),
                request.escolaOrigem().cnpj(),
                request.escolaOrigem().cep(),
                request.escolaOrigem().logradouro(),
                request.escolaOrigem().numero(),
                request.escolaOrigem().complemento(),
                request.escolaOrigem().bairro(),
                request.escolaOrigem().cidade(),
                request.escolaOrigem().uf(),
                now);
        return escolaOrigemRepository.save(novaEscolaOrigem);
    }

    private EscolaOrigemResponse toResponse(EscolaOrigemJpaEntity entity) {
        return new EscolaOrigemResponse(
                entity.getId(),
                entity.getNomeEscola(),
                entity.getCodigoInep(),
                entity.getCnpj(),
                entity.getCep(),
                entity.getLogradouro(),
                entity.getNumero(),
                entity.getComplemento(),
                entity.getBairro(),
                entity.getCidade(),
                entity.getUf(),
                entity.getCreatedAt());
    }

    private TransferenciaAlunoResponse toResponse(TransferenciaJpaEntity entity, EscolaOrigemJpaEntity escolaOrigem) {
        return new TransferenciaAlunoResponse(
                entity.getId(),
                entity.getAlunoId(),
                toResponse(escolaOrigem),
                entity.getSerieOrigem(),
                entity.getAnoLetivoOrigem(),
                entity.getDataTransferencia(),
                entity.getMotivoTransferencia(),
                entity.getSituacaoOrigem(),
                entity.getDocumentosEntregues(),
                entity.getObservacao(),
                entity.getTipoTransferencia(),
                entity.getStatusTransferencia(),
                entity.getUsuarioOperacao(),
                entity.getDataHoraOperacao(),
                entity.getCreatedAt());
    }

    private DocumentoAlunoResponse toResponse(DocumentoAlunoJpaEntity entity) {
        return new DocumentoAlunoResponse(
                entity.getId(),
                entity.getAlunoId(),
                entity.getTipoDocumento(),
                entity.getNomeArquivo(),
                entity.getUrlArquivo(),
                entity.getNumeroDocumento(),
                entity.getCaminhoArquivo(),
                entity.getDataUpload(),
                entity.getObservacao());
    }

    private DocumentoResponse toResponse(DocumentoAdministrativoJpaEntity entity) {
        return new DocumentoResponse(
                entity.getId(),
                entity.getEntidadeTipo(),
                entity.getEntidadeId(),
                entity.getSchoolId(),
                entity.getEscolaNome(),
                entity.getTipoDocumento(),
                entity.getNumeroDocumento(),
                entity.getCaminhoArquivo(),
                entity.getDataUpload(),
                entity.getObservacao());
    }

    private MatriculaResponse toResponse(MatriculaJpaEntity entity) {
        List<MatriculaEtapaResponse> etapas = matriculaEtapaRepository.findAllByMatriculaIdOrderByOrdemAscIdAsc(entity.getId()).stream()
                .map(this::toResponse)
                .toList();
        return new MatriculaResponse(
                entity.getId(),
                entity.getAlunoId(),
                entity.getTurmaId(),
                entity.getSchoolId(),
                entity.getEscolaNome(),
                entity.getSerieId(),
                entity.getSerieNome(),
                entity.getPeriodoLetivoId(),
                entity.getStatus(),
                entity.getTipoMatricula(),
                entity.getDataMatricula(),
                entity.getObservacao(),
                entity.getCreatedAt(),
                etapas);
    }

    private MatriculaEtapaResponse toResponse(MatriculaEtapaJpaEntity entity) {
        return new MatriculaEtapaResponse(
                entity.getId(),
                entity.getDescricao(),
                entity.getOrdem(),
                entity.getStatus(),
                entity.getDataInicio(),
                entity.getDataConclusao(),
                entity.getObservacao());
    }
}
