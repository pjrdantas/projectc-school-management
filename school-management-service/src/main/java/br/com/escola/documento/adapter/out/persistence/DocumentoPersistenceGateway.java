package br.com.escola.documento.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.application.port.internal.AlunoMatriculaPort;
import br.com.escola.documento.adapter.out.persistence.entity.DocumentoEntity;
import br.com.escola.documento.adapter.out.persistence.repository.DocumentoJpaRepository;
import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.port.out.DocumentoGateway;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.TipoDocumento;
import br.com.escola.documento.domain.exception.DocumentoInvalidoException;
import br.com.escola.documento.domain.exception.DocumentoNaoEncontradoException;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.responsavel.application.port.internal.ResponsavelDocumentoPort;

@Component
public class DocumentoPersistenceGateway implements DocumentoGateway {

    private final DocumentoJpaRepository documentoJpaRepository;
    private final AlunoMatriculaPort alunoMatriculaPort;
    private final ResponsavelDocumentoPort responsavelDocumentoPort;
    private final JdbcTemplate jdbcTemplate;
    private final EscolaTenantService escolaTenantService;

    public DocumentoPersistenceGateway(
            DocumentoJpaRepository documentoJpaRepository,
            AlunoMatriculaPort alunoMatriculaPort,
            ResponsavelDocumentoPort responsavelDocumentoPort,
            JdbcTemplate jdbcTemplate,
            EscolaTenantService escolaTenantService) {
        this.documentoJpaRepository = documentoJpaRepository;
        this.alunoMatriculaPort = alunoMatriculaPort;
        this.responsavelDocumentoPort = responsavelDocumentoPort;
        this.jdbcTemplate = jdbcTemplate;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    @Transactional
    public DocumentoOutput save(DocumentoInput input, EntidadeDocumentalTipo entidadeTipo, TipoDocumento tipoDocumento) {
        UUID pessoaId = resolvePessoaId(entidadeTipo, input.entidadeId());

        DocumentoEntity entity = new DocumentoEntity();
        entity.setTipoDocumentoId(buscarTipoDocumentoId(tipoDocumento));
        entity.setNumeroDocumento(input.numeroDocumento().trim());
        entity.setCaminhoArquivo(input.caminhoArquivo().trim());
        entity.setObservacao(input.observacao());

        DocumentoEntity documento = documentoJpaRepository.saveAndFlush(entity);
        jdbcTemplate.update(
                "INSERT INTO pessoa_documento (id_pessoa_documento, id_pessoa, id_documento, created_at) VALUES (?, ?, ?, CURRENT_TIMESTAMP)",
                UUID.randomUUID(),
                pessoaId,
                documento.getId());
        return toOutput(documento, entidadeTipo, input.entidadeId(), tipoDocumento.name());
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoOutput findById(UUID id) {
        UUID escolaId = escolaId();
        DocumentoEntity documento = documentoJpaRepository.findByIdAndEscolaId(id, escolaId)
                .orElseThrow(() -> new DocumentoNaoEncontradoException(id));
        DocumentoEntidade entidade = buscarEntidadePorDocumento(id, escolaId);
        return toOutput(
                documento,
                entidade.tipo(),
                entidade.id(),
                buscarTipoDocumentoPorDocumento(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoOutput> findByEntidade(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId) {
        UUID pessoaId = resolvePessoaId(entidadeTipo, entidadeId);
        return documentoJpaRepository.findByPessoaIdAndEscolaIdOrderByDataUploadDesc(pessoaId, escolaId()).stream()
                .map(documento -> toOutput(documento, entidadeTipo, entidadeId, buscarTipoDocumentoPorDocumento(documento.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        UUID escolaId = escolaId();
        if (!documentoJpaRepository.existsByIdAndEscolaId(id, escolaId)) {
            throw new DocumentoNaoEncontradoException(id);
        }
        documentoJpaRepository.deletePessoaDocumentoByDocumentoIdAndEscolaId(id, escolaId);
        documentoJpaRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteByEntidade(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId) {
        UUID pessoaId = resolvePessoaId(entidadeTipo, entidadeId);
        documentoJpaRepository.deletePessoaDocumentoByPessoaId(pessoaId);
        documentoJpaRepository.deleteDocumentosSemVinculo();
    }

    private UUID resolvePessoaId(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId) {
        if (entidadeTipo == EntidadeDocumentalTipo.ALUNO) {
            return alunoMatriculaPort.buscarAlunoPorIdEEscola(entidadeId, escolaId())
                    .map(aluno -> aluno.getPessoa())
                    .map(pessoa -> pessoa.getId())
                    .orElseThrow(() -> new DocumentoInvalidoException("Aluno não encontrado: " + entidadeId));
        }
        if (entidadeTipo == EntidadeDocumentalTipo.RESPONSAVEL) {
            return responsavelDocumentoPort.buscarResponsavelPorIdEEscola(entidadeId, escolaId())
                    .map(responsavel -> responsavel.getPessoa())
                    .map(pessoa -> pessoa.getId())
                    .orElseThrow(() -> new DocumentoInvalidoException("Responsável não encontrado: " + entidadeId));
        }
        throw new DocumentoInvalidoException("Vínculo documental ainda não implementado para: " + entidadeTipo.name());
    }

    private DocumentoOutput toOutput(
            DocumentoEntity entity,
            EntidadeDocumentalTipo entidadeTipo,
            UUID entidadeId,
            String tipoDocumento) {
        EscolaEntity escola = escolaTenantService.obterOuCriarEscolaPadrao();
        return new DocumentoOutput(
                entity.getId(),
                entidadeTipo.name(),
                entidadeId,
                escola.getId(),
                escola.getNome(),
                tipoDocumento,
                entity.getNumeroDocumento(),
                entity.getCaminhoArquivo(),
                entity.getDataUpload(),
                entity.getObservacao());
    }

    private UUID buscarTipoDocumentoId(TipoDocumento tipoDocumento) {
        List<UUID> ids = jdbcTemplate.query(
                "SELECT id_tipo_documento FROM tipo_documento WHERE codigo = ?",
                (rs, rowNum) -> rs.getObject("id_tipo_documento", UUID.class),
                tipoDocumento.name());
        if (ids.isEmpty()) {
            throw new DocumentoInvalidoException("Tipo de documento não cadastrado: " + tipoDocumento.name());
        }
        return ids.getFirst();
    }

    private String buscarTipoDocumentoPorDocumento(UUID documentoId) {
        List<String> codigos = jdbcTemplate.query(
                """
                SELECT td.codigo
                  FROM tipo_documento td
                  JOIN documento d ON d.id_tipo_documento = td.id_tipo_documento
                 WHERE d.id_documento = ?
                """,
                (rs, rowNum) -> rs.getString("codigo"),
                documentoId);
        return codigos.isEmpty() ? null : codigos.getFirst();
    }

    private DocumentoEntidade buscarEntidadePorDocumento(UUID documentoId, UUID escolaId) {
        List<DocumentoEntidade> alunos = jdbcTemplate.query(
                """
                SELECT a.id_aluno
                  FROM aluno a
                  JOIN pessoa p ON p.id_pessoa = a.id_pessoa
                  JOIN pessoa_documento pd ON pd.id_pessoa = a.id_pessoa
                 WHERE pd.id_documento = ?
                   AND p.id_escola = ?
                 LIMIT 1
                """,
                (rs, rowNum) -> new DocumentoEntidade(
                        EntidadeDocumentalTipo.ALUNO,
                        rs.getObject("id_aluno", UUID.class)),
                documentoId,
                escolaId);
        if (!alunos.isEmpty()) {
            return alunos.getFirst();
        }

        List<DocumentoEntidade> responsaveis = jdbcTemplate.query(
                """
                SELECT r.id_responsavel
                  FROM responsavel r
                  JOIN pessoa p ON p.id_pessoa = r.id_pessoa
                  JOIN pessoa_documento pd ON pd.id_pessoa = r.id_pessoa
                 WHERE pd.id_documento = ?
                   AND p.id_escola = ?
                 LIMIT 1
                """,
                (rs, rowNum) -> new DocumentoEntidade(
                        EntidadeDocumentalTipo.RESPONSAVEL,
                        rs.getObject("id_responsavel", UUID.class)),
                documentoId,
                escolaId);
        if (!responsaveis.isEmpty()) {
            return responsaveis.getFirst();
        }

        throw new DocumentoNaoEncontradoException(documentoId);
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }

    private record DocumentoEntidade(EntidadeDocumentalTipo tipo, UUID id) {
    }
}
