package br.com.escola.enrollmentdocumentservice.application.service;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoAlunoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarDocumentoRequest;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoAlunoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoDownload;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoMetadata;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoArquivoExclusao;
import br.com.escola.enrollmentdocumentservice.application.dto.DocumentoResponse;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoAlunoCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.UploadDocumentoCommand;
import br.com.escola.enrollmentdocumentservice.application.port.in.DocumentoUseCase;
import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoArquivoStoragePort;
import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoMetadataPort;

@Service
public class DocumentoService implements DocumentoUseCase {

    private static final long TAMANHO_MAXIMO_ARQUIVO = 10L * 1024 * 1024;
    private static final Set<String> TIPOS_CONTEUDO_PERMITIDOS = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png");

    private final DocumentoMetadataPort documentoMetadataPort;
    private final DocumentoArquivoStoragePort documentoArquivoStoragePort;

    public DocumentoService(
            DocumentoMetadataPort documentoMetadataPort,
            DocumentoArquivoStoragePort documentoArquivoStoragePort) {
        this.documentoMetadataPort = documentoMetadataPort;
        this.documentoArquivoStoragePort = documentoArquivoStoragePort;
    }

    @Override
    public DocumentoAlunoResponse criarDocumentoAluno(
            InternalRequestContext context,
            CriarDocumentoAlunoRequest request) {
        String caminhoArquivo = primeiroTexto(request.caminhoArquivo(), request.urlArquivo());
        String urlArquivo = primeiroTexto(request.urlArquivo(), request.caminhoArquivo());
        return documentoMetadataPort.criarDocumentoAluno(
                context.escolaId(),
                new CriarDocumentoAlunoCommand(
                        request.alunoId(),
                        request.tipoDocumento().trim(),
                        limpar(request.nomeArquivo()),
                        urlArquivo,
                        limpar(request.numeroDocumento()),
                        caminhoArquivo,
                        limpar(request.observacao()),
                        null,
                        null));
    }

    @Override
    public DocumentoAlunoResponse enviarDocumentoAluno(
            InternalRequestContext context,
            UploadDocumentoAlunoCommand command) {
        String nomeArquivo = validarNomeArquivo(command.nomeArquivo());
        String tipoConteudo = validarTipoConteudo(command.tipoConteudo());
        validarTamanhoArquivo(command.tamanhoArquivo());
        DocumentoArquivoStoragePort.ReferenciaArquivoDocumento referencia = documentoArquivoStoragePort.armazenar(
                new DocumentoArquivoStoragePort.ConteudoArquivoDocumento(
                        nomeArquivo,
                        tipoConteudo,
                        command.tamanhoArquivo(),
                        command.conteudo()));
        try {
            return documentoMetadataPort.criarDocumentoAluno(
                    context.escolaId(),
                    new CriarDocumentoAlunoCommand(
                            command.alunoId(),
                            command.tipoDocumento().trim(),
                            nomeArquivo,
                            null,
                            limpar(command.numeroDocumento()),
                            referencia.referenciaArmazenamento(),
                            limpar(command.observacao()),
                            tipoConteudo,
                            command.tamanhoArquivo()));
        } catch (RuntimeException exception) {
            documentoArquivoStoragePort.excluir(referencia.referenciaArmazenamento());
            throw exception;
        }
    }

    @Override
    public DocumentoResponse criarDocumento(InternalRequestContext context, CriarDocumentoRequest request) {
        return documentoMetadataPort.criarDocumento(
                context.escolaId(),
                new CriarDocumentoCommand(
                        normalizarObrigatorio(request.entidadeTipo(), "Tipo da entidade obrigatorio"),
                        request.entidadeId(),
                        normalizarObrigatorio(request.tipoDocumento(), "Tipo do documento obrigatorio"),
                        null,
                        limpar(request.numeroDocumento()),
                        normalizarObrigatorio(request.caminhoArquivo(), "Referencia do arquivo obrigatoria"),
                        limpar(request.observacao()),
                        null,
                        null));
    }

    @Override
    public DocumentoResponse enviarDocumento(InternalRequestContext context, UploadDocumentoCommand command) {
        String nomeArquivo = validarNomeArquivo(command.nomeArquivo());
        String tipoConteudo = validarTipoConteudo(command.tipoConteudo());
        validarTamanhoArquivo(command.tamanhoArquivo());
        DocumentoArquivoStoragePort.ReferenciaArquivoDocumento referencia = documentoArquivoStoragePort.armazenar(
                new DocumentoArquivoStoragePort.ConteudoArquivoDocumento(
                        nomeArquivo,
                        tipoConteudo,
                        command.tamanhoArquivo(),
                        command.conteudo()));
        try {
            return documentoMetadataPort.criarDocumento(
                    context.escolaId(),
                    new CriarDocumentoCommand(
                            normalizarObrigatorio(command.entidadeTipo(), "Tipo da entidade obrigatorio"),
                            command.entidadeId(),
                            normalizarObrigatorio(command.tipoDocumento(), "Tipo do documento obrigatorio"),
                            nomeArquivo,
                            limpar(command.numeroDocumento()),
                            referencia.referenciaArmazenamento(),
                            limpar(command.observacao()),
                            tipoConteudo,
                            command.tamanhoArquivo()));
        } catch (RuntimeException exception) {
            documentoArquivoStoragePort.excluir(referencia.referenciaArmazenamento());
            throw exception;
        }
    }

    @Override
    public DocumentoArquivoDownload baixarDocumentoAluno(InternalRequestContext context, UUID documentoId) {
        return abrirDownload(documentoMetadataPort.buscarArquivoDocumentoAluno(context.escolaId(), documentoId));
    }

    @Override
    public DocumentoArquivoDownload baixarDocumento(InternalRequestContext context, UUID documentoId) {
        return abrirDownload(documentoMetadataPort.buscarArquivoDocumento(context.escolaId(), documentoId));
    }

    @Override
    @Transactional
    public void excluirDocumentoAluno(InternalRequestContext context, UUID documentoId) {
        excluirConteudoGerenciado(documentoMetadataPort.excluirDocumentoAluno(context.escolaId(), documentoId));
    }

    @Override
    @Transactional
    public void excluirDocumento(InternalRequestContext context, UUID documentoId) {
        excluirConteudoGerenciado(documentoMetadataPort.excluirDocumento(context.escolaId(), documentoId));
    }

    @Override
    public List<DocumentoAlunoResponse> listarDocumentosPorAluno(InternalRequestContext context, UUID alunoId) {
        return documentoMetadataPort.listarDocumentosPorAluno(context.escolaId(), alunoId);
    }

    @Override
    public DocumentoAlunoResponse buscarDocumentoAlunoPorId(InternalRequestContext context, UUID id) {
        return documentoMetadataPort.buscarDocumentoAlunoPorId(context.escolaId(), id);
    }

    @Override
    public List<DocumentoResponse> listarDocumentosPorEntidade(
            InternalRequestContext context,
            String entidadeTipo,
            UUID entidadeId) {
        return documentoMetadataPort.listarDocumentosPorEntidade(context.escolaId(), entidadeTipo, entidadeId);
    }

    private String primeiroTexto(String principal, String alternativo) {
        String normalizado = limpar(principal);
        if (normalizado != null) {
            return normalizado;
        }
        return limpar(alternativo);
    }

    private String limpar(String value) {
        if (value == null) {
            return null;
        }
        String normalizado = value.trim();
        return normalizado.isEmpty() ? null : normalizado;
    }

    private String validarNomeArquivo(String nomeArquivo) {
        String normalizado = limpar(nomeArquivo);
        if (normalizado == null || normalizado.contains("/") || normalizado.contains("\\")
                || normalizado.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException("Nome do arquivo invalido");
        }
        return normalizado;
    }

    private String validarTipoConteudo(String tipoConteudo) {
        String normalizado = limpar(tipoConteudo);
        if (normalizado == null) {
            throw new IllegalArgumentException("Tipo de conteudo obrigatorio");
        }
        String tipoSemParametros = normalizado.split(";", 2)[0].trim().toLowerCase(Locale.ROOT);
        if (!TIPOS_CONTEUDO_PERMITIDOS.contains(tipoSemParametros)) {
            throw new IllegalArgumentException("Tipo de conteudo nao permitido");
        }
        return tipoSemParametros;
    }

    private void validarTamanhoArquivo(long tamanhoArquivo) {
        if (tamanhoArquivo <= 0 || tamanhoArquivo > TAMANHO_MAXIMO_ARQUIVO) {
            throw new IllegalArgumentException("Tamanho do arquivo invalido");
        }
    }

    private String normalizarObrigatorio(String value, String mensagem) {
        String normalizado = limpar(value);
        if (normalizado == null) {
            throw new IllegalArgumentException(mensagem);
        }
        return normalizado;
    }

    private DocumentoArquivoDownload abrirDownload(DocumentoArquivoMetadata metadata) {
        if (metadata.referenciaArmazenamento() == null || metadata.referenciaArmazenamento().isBlank()) {
            throw new IllegalArgumentException("Documento sem conteudo armazenado");
        }
        String nomeArquivo = limpar(metadata.nomeArquivo());
        if (nomeArquivo == null) {
            nomeArquivo = "documento-" + metadata.documentoId();
        }
        String tipoConteudo = limpar(metadata.tipoConteudo());
        if (tipoConteudo == null) {
            tipoConteudo = "application/octet-stream";
        }
        return new DocumentoArquivoDownload(
                nomeArquivo,
                tipoConteudo,
                documentoArquivoStoragePort.abrirConteudo(metadata.referenciaArmazenamento()));
    }

    private void excluirConteudoGerenciado(DocumentoArquivoExclusao exclusao) {
        if (exclusao.excluidoAgora() && referenciaLocal(exclusao.referenciaArmazenamento())) {
            documentoArquivoStoragePort.excluir(exclusao.referenciaArmazenamento());
        }
    }

    private boolean referenciaLocal(String referenciaArmazenamento) {
        return referenciaArmazenamento != null && referenciaArmazenamento.matches("[0-9a-fA-F-]{36}");
    }
}
