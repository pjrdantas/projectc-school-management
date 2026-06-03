package br.com.escola.documento.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.documento.adapter.out.persistence.entity.DocumentoEntity;
import br.com.escola.documento.adapter.out.persistence.repository.DocumentoJpaRepository;
import br.com.escola.documento.application.dto.DocumentoInput;
import br.com.escola.documento.application.dto.DocumentoOutput;
import br.com.escola.documento.application.port.out.DocumentoGateway;
import br.com.escola.documento.domain.EntidadeDocumentalTipo;
import br.com.escola.documento.domain.TipoDocumento;
import br.com.escola.documento.domain.exception.DocumentoInvalidoException;
import br.com.escola.documento.domain.exception.DocumentoNaoEncontradoException;
import br.com.escola.responsavel.adapter.out.persistence.entity.ResponsavelEntity;
import br.com.escola.responsavel.adapter.out.persistence.repository.ResponsavelJpaRepository;
import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;

@Component
public class DocumentoPersistenceGateway implements DocumentoGateway {

    private final DocumentoJpaRepository documentoJpaRepository;
    private final AlunoJpaRepository alunoJpaRepository;
    private final ResponsavelJpaRepository responsavelJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    public DocumentoPersistenceGateway(
            DocumentoJpaRepository documentoJpaRepository,
            AlunoJpaRepository alunoJpaRepository,
            ResponsavelJpaRepository responsavelJpaRepository,
            JdbcTemplate jdbcTemplate) {
        this.documentoJpaRepository = documentoJpaRepository;
        this.alunoJpaRepository = alunoJpaRepository;
        this.responsavelJpaRepository = responsavelJpaRepository;
        this.jdbcTemplate = jdbcTemplate;
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
        DocumentoEntity documento = documentoJpaRepository.findById(id)
                .orElseThrow(() -> new DocumentoNaoEncontradoException(id));
        return toOutput(
                documento,
                EntidadeDocumentalTipo.ALUNO,
                buscarAlunoIdPorDocumento(id),
                buscarTipoDocumentoPorDocumento(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DocumentoOutput> findByEntidade(EntidadeDocumentalTipo entidadeTipo, UUID entidadeId) {
        UUID pessoaId = resolvePessoaId(entidadeTipo, entidadeId);
        return documentoJpaRepository.findByPessoaIdOrderByDataUploadDesc(pessoaId).stream()
                .map(documento -> toOutput(documento, entidadeTipo, entidadeId, buscarTipoDocumentoPorDocumento(documento.getId())))
                .toList();
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        if (!documentoJpaRepository.existsById(id)) {
            throw new DocumentoNaoEncontradoException(id);
        }
        documentoJpaRepository.deletePessoaDocumentoByDocumentoId(id);
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
            return alunoJpaRepository.findById(entidadeId)
                    .map(AlunoEntity::getPessoa)
                    .map(pessoa -> pessoa.getId())
                    .orElseThrow(() -> new DocumentoInvalidoException("Aluno não encontrado: " + entidadeId));
        }
        if (entidadeTipo == EntidadeDocumentalTipo.RESPONSAVEL) {
            return responsavelJpaRepository.findById(entidadeId)
                    .map(ResponsavelEntity::getPessoa)
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
        return new DocumentoOutput(
                entity.getId(),
                entidadeTipo.name(),
                entidadeId,
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

    private UUID buscarAlunoIdPorDocumento(UUID documentoId) {
        List<UUID> ids = jdbcTemplate.query(
                """
                SELECT a.id_aluno
                  FROM aluno a
                  JOIN pessoa_documento pd ON pd.id_pessoa = a.id_pessoa
                 WHERE pd.id_documento = ?
                 LIMIT 1
                """,
                (rs, rowNum) -> rs.getObject("id_aluno", UUID.class),
                documentoId);
        return ids.isEmpty() ? null : ids.getFirst();
    }
}
