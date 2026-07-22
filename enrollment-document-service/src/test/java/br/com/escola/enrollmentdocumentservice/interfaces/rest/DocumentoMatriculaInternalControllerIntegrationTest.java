package br.com.escola.enrollmentdocumentservice.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;

import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAdministrativoJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.DocumentoAlunoJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.EscolaOrigemJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaEtapaJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.entity.MatriculaJpaEntity;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.DocumentoAdministrativoJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.DocumentoAlunoJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.EscolaOrigemJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.MatriculaEtapaJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.MatriculaJpaRepository;
import br.com.escola.enrollmentdocumentservice.infra.database.repository.TransferenciaJpaRepository;
import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoArquivoStoragePort;

@SpringBootTest
@AutoConfigureMockMvc
class DocumentoMatriculaInternalControllerIntegrationTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final String AUTHORIZATION = "Bearer enrollment-user-token";
    private static final String INTERNAL_TOKEN = "enrollment-document-internal-local-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EscolaOrigemJpaRepository escolaOrigemRepository;

    @Autowired
    private TransferenciaJpaRepository transferenciaRepository;

    @Autowired
    private DocumentoAlunoJpaRepository documentoAlunoRepository;

    @Autowired
    private DocumentoAdministrativoJpaRepository documentoAdministrativoRepository;

    @Autowired
    private MatriculaJpaRepository matriculaRepository;

    @Autowired
    private MatriculaEtapaJpaRepository matriculaEtapaRepository;

    @Autowired
    private DocumentoArquivoStoragePort documentoArquivoStoragePort;

    @BeforeEach
    void setUp() {
        matriculaEtapaRepository.deleteAll();
        matriculaRepository.deleteAll();
        documentoAdministrativoRepository.deleteAll();
        documentoAlunoRepository.deleteAll();
        transferenciaRepository.deleteAll();
        escolaOrigemRepository.deleteAll();
    }

    @Test
    void deveCriarTransferenciaNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID escolaOrigemId = UUID.randomUUID();
        escolaOrigemRepository.save(new EscolaOrigemJpaEntity(
                escolaOrigemId,
                ESCOLA_ID,
                "Escola Origem Interna",
                "123",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 12, 10, 0)));

        mockMvc.perform(post("/internal/v1/transferencias")
                        .contentType("application/json")
                        .content("""
                                {
                                  "alunoId": "%s",
                                  "escolaOrigemId": "%s",
                                  "serieOrigem": "5A",
                                  "anoLetivoOrigem": "2026",
                                  "dataTransferencia": "2026-07-01",
                                  "motivoTransferencia": "Mudanca",
                                  "situacaoOrigem": "ATIVO",
                                  "documentosEntregues": "SIM",
                                  "tipoTransferencia": "ENTRADA",
                                  "statusTransferencia": "EM_ANDAMENTO",
                                  "usuarioOperacao": "tester",
                                  "observacao": "obs"
                                }
                                """.formatted(alunoId, escolaOrigemId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.escolaOrigem.id").value(escolaOrigemId.toString()))
                .andExpect(jsonPath("$.escolaOrigem.nomeEscola").value("Escola Origem Interna"))
                .andExpect(jsonPath("$.tipoTransferencia").value("ENTRADA"));
    }

    @Test
    void deveListarEscolasOrigemNoContratoInterno() throws Exception {
        escolaOrigemRepository.save(new EscolaOrigemJpaEntity(
                UUID.randomUUID(),
                ESCOLA_ID,
                "Escola Origem A",
                "123",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.of(2026, 7, 12, 10, 0)));

        mockMvc.perform(get("/internal/v1/escolas-origem")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nomeEscola").value("Escola Origem A"));
    }

    @Test
    void deveListarDocumentosPorAlunoNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        documentoAlunoRepository.save(new DocumentoAlunoJpaEntity(
                documentoId,
                ESCOLA_ID,
                alunoId,
                "HISTORICO_ESCOLAR",
                "historico.pdf",
                "s3://bucket/historico.pdf",
                "historico.pdf",
                "s3://bucket/historico.pdf",
                LocalDateTime.of(2026, 7, 12, 10, 0),
                "Documento escolar"));

        mockMvc.perform(get("/internal/v1/documentos-alunos/alunos/{alunoId}", alunoId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentoId.toString()))
                .andExpect(jsonPath("$[0].alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$[0].tipoDocumento").value("HISTORICO_ESCOLAR"));
    }

    @Test
    void deveCriarDocumentoDoAlunoNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();

        mockMvc.perform(post("/internal/v1/documentos-alunos")
                        .contentType("application/json")
                        .content("""
                                {
                                  "alunoId": "%s",
                                  "tipoDocumento": "RG",
                                  "nomeArquivo": "rg.pdf",
                                  "numeroDocumento": "123",
                                  "caminhoArquivo": "arquivos/alunos/rg.pdf",
                                  "observacao": "Documento informado"
                                }
                                """.formatted(alunoId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-documento-create")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("RG"))
                .andExpect(jsonPath("$.caminhoArquivo").value("arquivos/alunos/rg.pdf"));

        DocumentoAlunoJpaEntity entity = documentoAlunoRepository.findAll().getFirst();
        org.assertj.core.api.Assertions.assertThat(entity.getSchoolId()).isEqualTo(ESCOLA_ID);
        org.assertj.core.api.Assertions.assertThat(entity.getReferenciaArmazenamento())
                .isEqualTo("arquivos/alunos/rg.pdf");
    }

    @Test
    void deveReceberUploadDeDocumentoDoAlunoNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        byte[] conteudo = "conteudo-pdf".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        mockMvc.perform(multipart("/internal/v1/documentos-alunos/upload")
                        .file(new MockMultipartFile("arquivo", "rg.pdf", "application/pdf", conteudo))
                        .param("alunoId", alunoId.toString())
                        .param("tipoDocumento", "RG")
                        .param("numeroDocumento", "123")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-documento-upload")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.nomeArquivo").value("rg.pdf"))
                .andExpect(jsonPath("$.caminhoArquivo").isNotEmpty());

        DocumentoAlunoJpaEntity entity = documentoAlunoRepository.findAll().getFirst();
        assertThat(entity.getTipoConteudo()).isEqualTo("application/pdf");
        assertThat(entity.getTamanhoArquivo()).isEqualTo((long) conteudo.length);
        try (var input = documentoArquivoStoragePort.abrirConteudo(entity.getReferenciaArmazenamento())) {
            assertThat(input.readAllBytes()).isEqualTo(conteudo);
            mockMvc.perform(get("/internal/v1/documentos-alunos/{id}/conteudo", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-download")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/pdf"))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("rg.pdf")))
                    .andExpect(content().bytes(conteudo));

            mockMvc.perform(get("/internal/v1/documentos-alunos/{id}/conteudo", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-download-other-school")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", UUID.randomUUID())
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/internal/v1/documentos-alunos/{id}", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-delete")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/internal/v1/documentos-alunos/{id}", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-delete-repeat")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/internal/v1/documentos-alunos/{id}", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-after-delete")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNotFound());
            assertThat(documentoAlunoRepository.findById(entity.getId()).orElseThrow().getDataExclusao()).isNotNull();
        } finally {
            documentoArquivoStoragePort.excluir(entity.getReferenciaArmazenamento());
        }
    }

    @Test
    void deveCriarDocumentoDeEntidadeNoContratoInterno() throws Exception {
        UUID entidadeId = UUID.randomUUID();

        mockMvc.perform(post("/internal/v1/documentos")
                        .contentType("application/json")
                        .content("""
                                {
                                  "entidadeTipo": "MATRICULA",
                                  "entidadeId": "%s",
                                  "tipoDocumento": "CPF",
                                  "numeroDocumento": "123",
                                  "caminhoArquivo": "arquivos/matriculas/cpf.pdf",
                                  "observacao": "Documento administrativo"
                                }
                                """.formatted(entidadeId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-documento-entity-create")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entidadeTipo").value("MATRICULA"))
                .andExpect(jsonPath("$.entidadeId").value(entidadeId.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("CPF"));

        DocumentoAdministrativoJpaEntity entity = documentoAdministrativoRepository.findAll().getFirst();
        assertThat(entity.getSchoolId()).isEqualTo(ESCOLA_ID);
        assertThat(entity.getReferenciaArmazenamento()).isEqualTo("arquivos/matriculas/cpf.pdf");
    }

    @Test
    void deveReceberUploadDeDocumentoDeEntidadeNoContratoInterno() throws Exception {
        UUID entidadeId = UUID.randomUUID();
        byte[] conteudo = "conteudo-administrativo".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        mockMvc.perform(multipart("/internal/v1/documentos/upload")
                        .file(new MockMultipartFile("arquivo", "cpf.pdf", "application/pdf", conteudo))
                        .param("entidadeTipo", "MATRICULA")
                        .param("entidadeId", entidadeId.toString())
                        .param("tipoDocumento", "CPF")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-documento-entity-upload")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.entidadeTipo").value("MATRICULA"))
                .andExpect(jsonPath("$.entidadeId").value(entidadeId.toString()))
                .andExpect(jsonPath("$.caminhoArquivo").isNotEmpty());

        DocumentoAdministrativoJpaEntity entity = documentoAdministrativoRepository.findAll().getFirst();
        assertThat(entity.getTipoConteudo()).isEqualTo("application/pdf");
        assertThat(entity.getTamanhoArquivo()).isEqualTo((long) conteudo.length);
        try (var input = documentoArquivoStoragePort.abrirConteudo(entity.getReferenciaArmazenamento())) {
            assertThat(input.readAllBytes()).isEqualTo(conteudo);
            mockMvc.perform(get("/internal/v1/documentos/{id}/conteudo", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-entity-download")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/pdf"))
                    .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("cpf.pdf")))
                    .andExpect(content().bytes(conteudo));

            mockMvc.perform(get("/internal/v1/documentos/{id}/conteudo", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-entity-download-other-school")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", UUID.randomUUID())
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNotFound());

            mockMvc.perform(delete("/internal/v1/documentos/{id}", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-entity-delete")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNoContent());

            mockMvc.perform(delete("/internal/v1/documentos/{id}", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-entity-delete-repeat")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNoContent());

            mockMvc.perform(get("/internal/v1/documentos/{id}/conteudo", entity.getId())
                            .header("X-Internal-Token", INTERNAL_TOKEN)
                            .header("X-Correlation-Id", "corr-documento-entity-after-delete")
                            .header("X-Usuario-Id", UUID.randomUUID())
                            .header("X-Escola-Id", ESCOLA_ID)
                            .header("Authorization", AUTHORIZATION))
                    .andExpect(status().isNotFound());
            assertThat(documentoAdministrativoRepository.findById(entity.getId()).orElseThrow().getDataExclusao()).isNotNull();
        } finally {
            documentoArquivoStoragePort.excluir(entity.getReferenciaArmazenamento());
        }
    }

    @Test
    void deveBuscarDocumentoAlunoPorIdNoContratoInterno() throws Exception {
        UUID alunoId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        documentoAlunoRepository.save(new DocumentoAlunoJpaEntity(
                documentoId,
                ESCOLA_ID,
                alunoId,
                "HISTORICO_ESCOLAR",
                "historico.pdf",
                "s3://bucket/historico.pdf",
                "historico.pdf",
                "s3://bucket/historico.pdf",
                LocalDateTime.of(2026, 7, 12, 10, 0),
                "Documento escolar"));

        mockMvc.perform(get("/internal/v1/documentos-alunos/{id}", documentoId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-5")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(documentoId.toString()))
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.tipoDocumento").value("HISTORICO_ESCOLAR"));
    }

    @Test
    void deveListarMatriculasNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();
        matriculaRepository.save(new MatriculaJpaEntity(
                matriculaId,
                ESCOLA_ID,
                alunoId,
                UUID.fromString("00000000-0000-0000-0000-000000000071"),
                "Escola padrao",
                UUID.fromString("00000000-0000-0000-0000-000000000081"),
                "6 Ano",
                UUID.fromString("00000000-0000-0000-0000-000000000091"),
                "EM_ANDAMENTO",
                "PRIMEIRA_MATRICULA",
                LocalDate.of(2026, 7, 12),
                "Matricula interna",
                LocalDateTime.of(2026, 7, 12, 10, 0)));
        matriculaEtapaRepository.save(new MatriculaEtapaJpaEntity(
                etapaId,
                matriculaId,
                "Analise documental",
                1,
                "PENDENTE",
                LocalDateTime.of(2026, 7, 12, 10, 0),
                null,
                "Aguardando conferencia"));

        mockMvc.perform(get("/internal/v1/matriculas")
                        .param("alunoId", alunoId.toString())
                        .param("status", "EM_ANDAMENTO")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-6")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$[0].etapas[0].id").value(etapaId.toString()))
                .andExpect(jsonPath("$[0].etapas[0].descricao").value("Analise documental"));
    }

    @Test
    void deveBuscarDetalheDaMatriculaNoTenantDoContexto() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID etapaId = UUID.randomUUID();
        matriculaRepository.save(new MatriculaJpaEntity(
                matriculaId,
                ESCOLA_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escola padrao",
                UUID.randomUUID(),
                "6 Ano",
                UUID.randomUUID(),
                "EFETIVADA",
                "PRIMEIRA_MATRICULA",
                LocalDate.of(2026, 7, 21),
                "Matricula detalhada",
                LocalDateTime.of(2026, 7, 21, 10, 0)));
        matriculaEtapaRepository.save(new MatriculaEtapaJpaEntity(
                etapaId,
                matriculaId,
                "Documentos validados",
                1,
                "CONCLUIDA",
                LocalDateTime.of(2026, 7, 21, 10, 0),
                LocalDateTime.of(2026, 7, 21, 11, 0),
                "Sem pendencias"));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}", matriculaId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-detail")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(matriculaId.toString()))
                .andExpect(jsonPath("$.status").value("EFETIVADA"))
                .andExpect(jsonPath("$.etapas[0].id").value(etapaId.toString()))
                .andExpect(jsonPath("$.etapas[0].observacao").value("Sem pendencias"));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}", matriculaId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-detail-other-school")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", UUID.randomUUID())
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isNotFound());
    }

    @Test
    void deveAtualizarStatusECancelarMatriculaNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        matriculaRepository.save(new MatriculaJpaEntity(
                matriculaId,
                ESCOLA_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Escola padrao",
                UUID.randomUUID(),
                "6 Ano",
                UUID.randomUUID(),
                "PENDENTE",
                "PRIMEIRA_MATRICULA",
                LocalDate.of(2026, 7, 21),
                "Matricula interna",
                LocalDateTime.of(2026, 7, 21, 10, 0)));

        mockMvc.perform(patch("/internal/v1/matriculas/{matriculaId}/status", matriculaId)
                        .contentType("application/json")
                        .content("""
                                { "status": "EFETIVADA", "observacao": "Documentos validados" }
                                """)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-status")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EFETIVADA"))
                .andExpect(jsonPath("$.observacao").value("Documentos validados"));

        mockMvc.perform(post("/internal/v1/matriculas/{matriculaId}/cancelamento", matriculaId)
                        .contentType("application/json")
                        .content("""
                                { "motivo": "Desistencia formalizada" }
                                """)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-cancel")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        MatriculaJpaEntity cancelled = matriculaRepository.findById(matriculaId).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(cancelled.getCancellationReason()).isEqualTo("Desistencia formalizada");
        org.assertj.core.api.Assertions.assertThat(cancelled.getCancelledAt()).isNotNull();
    }

    @Test
    void deveListarDocumentosPorEntidadeNoContratoInterno() throws Exception {
        UUID entidadeId = UUID.randomUUID();
        UUID documentoId = UUID.randomUUID();
        documentoAdministrativoRepository.save(new DocumentoAdministrativoJpaEntity(
                documentoId,
                ESCOLA_ID,
                "Escola padrao",
                "MATRICULA",
                entidadeId,
                "CPF",
                null,
                "12345678900",
                "s3://bucket/documento.pdf",
                LocalDateTime.of(2026, 7, 12, 10, 0),
                "Documento administrativo"));

        mockMvc.perform(get("/internal/v1/documentos")
                        .param("entidadeTipo", "MATRICULA")
                        .param("entidadeId", entidadeId.toString())
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-enrollment-7")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(documentoId.toString()))
                .andExpect(jsonPath("$[0].entidadeTipo").value("MATRICULA"))
                .andExpect(jsonPath("$[0].entidadeId").value(entidadeId.toString()))
                .andExpect(jsonPath("$[0].tipoDocumento").value("CPF"));
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/escolas-origem")
                        .header("X-Correlation-Id", "corr-enrollment-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }
}
