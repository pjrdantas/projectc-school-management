package br.com.escola.pedagogicalservice.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import br.com.escola.pedagogicalservice.infra.database.entity.AulaJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.BoletimJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.DiarioClasseJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.entity.HistoricoEscolarJpaEntity;
import br.com.escola.pedagogicalservice.infra.database.repository.AulaJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.AvaliacaoJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.BoletimJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.DiarioClasseJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.FrequenciaAlunoJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.FrequenciaDocenteJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.HistoricoEscolarJpaRepository;
import br.com.escola.pedagogicalservice.infra.database.repository.NotaAlunoJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
class EnsinoInternalControllerIntegrationTest {

    private static final UUID ESCOLA_ID = UUID.fromString("00000000-0000-0000-0000-000000000047");
    private static final String AUTHORIZATION = "Bearer pedagogical-user-token";
    private static final String INTERNAL_TOKEN = "pedagogical-internal-local-token";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BoletimJpaRepository boletimRepository;

    @Autowired
    private AulaJpaRepository aulaRepository;

    @Autowired
    private FrequenciaDocenteJpaRepository frequenciaDocenteRepository;

    @Autowired
    private FrequenciaAlunoJpaRepository frequenciaAlunoRepository;

    @Autowired
    private AvaliacaoJpaRepository avaliacaoRepository;

    @Autowired
    private NotaAlunoJpaRepository notaAlunoRepository;

    @Autowired
    private DiarioClasseJpaRepository diarioClasseRepository;

    @Autowired
    private HistoricoEscolarJpaRepository historicoEscolarRepository;

    @BeforeEach
    void setUp() {
        boletimRepository.deleteAll();
        frequenciaAlunoRepository.deleteAll();
        frequenciaDocenteRepository.deleteAll();
        notaAlunoRepository.deleteAll();
        aulaRepository.deleteAll();
        avaliacaoRepository.deleteAll();
        diarioClasseRepository.deleteAll();
        historicoEscolarRepository.deleteAll();
    }

    @Test
    void deveExigirTokenInternoValido() throws Exception {
        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/boletim", UUID.randomUUID())
                        .header("X-Correlation-Id", "corr-pedagogical-unauthorized")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INTERNAL_UNAUTHORIZED"));
    }

    @Test
    void deveConsultarBoletimNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        UUID boletimId = UUID.randomUUID();
        boletimRepository.save(new BoletimJpaEntity(
                boletimId,
                ESCOLA_ID,
                matriculaId,
                false,
                """
                        {
                          "boletimId":"%s",
                          "matriculaId":"%s",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "alunoNome":"Aluno Pedagogico",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"6A",
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                          "periodoLetivoNome":"2026",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "dataGeracao":"2026-07-12",
                          "periodoReferencia":"2BIM",
                          "dataFechamento":"2026-07-12",
                          "observacao":"Boletim integrado",
                          "persistido":true,
                          "indicadores":{"totalDisciplinas":1,"mediaGeral":8.50,"frequenciaGeralPercentual":95.00,"resultadoGeral":"APROVADO"},
                          "itens":[{"disciplinaId":"00000000-0000-0000-0000-000000000081","disciplinaNome":"Matematica","media":8.50,"frequenciaPercentual":95.00,"totalAvaliacoes":4,"totalFrequencias":20,"resultado":"APROVADO"}]
                        }
                        """.formatted(boletimId, matriculaId),
                LocalDateTime.now()));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/boletim", matriculaId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Pedagogico"))
                .andExpect(jsonPath("$.itens[0].disciplinaNome").value("Matematica"));
    }

    @Test
    void deveListarFechamentosNoContratoInterno() throws Exception {
        UUID matriculaId = UUID.randomUUID();
        boletimRepository.save(new BoletimJpaEntity(
                UUID.randomUUID(),
                ESCOLA_ID,
                matriculaId,
                true,
                """
                        {
                          "boletimId":"00000000-0000-0000-0000-000000000061",
                          "matriculaId":"%s",
                          "alunoId":"00000000-0000-0000-0000-000000000021",
                          "alunoNome":"Aluno Pedagogico",
                          "turmaId":"00000000-0000-0000-0000-000000000071",
                          "turmaNome":"6A",
                          "periodoLetivoId":"00000000-0000-0000-0000-000000000091",
                          "periodoLetivoNome":"2026",
                          "escolaId":"00000000-0000-0000-0000-000000000047",
                          "escolaNome":"Escola padrao",
                          "dataGeracao":"2026-07-12",
                          "periodoReferencia":"1BIM",
                          "dataFechamento":"2026-07-10",
                          "observacao":"Fechamento integrado",
                          "persistido":true,
                          "indicadores":{"totalDisciplinas":1,"mediaGeral":8.50,"frequenciaGeralPercentual":95.00,"resultadoGeral":"APROVADO"},
                          "itens":[]
                        }
                        """.formatted(matriculaId),
                LocalDateTime.now()));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/boletim/fechamentos", matriculaId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value(matriculaId.toString()))
                .andExpect(jsonPath("$[0].periodoReferencia").value("1BIM"));
    }

    @Test
    void deveCriarEListarAulaNoContratoInterno() throws Exception {
        UUID alocacaoId = UUID.randomUUID();

        mockMvc.perform(post("/internal/v1/aulas")
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId":"%s",
                                  "professorId":"00000000-0000-0000-0000-000000000101",
                                  "professorNome":"Professor Aula",
                                  "turmaId":"00000000-0000-0000-0000-000000000071",
                                  "turmaNome":"Turma Aula",
                                  "disciplinaId":"00000000-0000-0000-0000-000000000081",
                                  "disciplinaNome":"Matematica",
                                  "dataAula":"2038-03-10",
                                  "horarioInicio":"07:30:00",
                                  "horarioFim":"08:20:00",
                                  "conteudoMinistrado":"Conteudo",
                                  "observacao":"Observacao",
                                  "realizada":true
                                }
                                """.formatted(alocacaoId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-aula-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andExpect(jsonPath("$.turmaNome").value("Turma Aula"));

        mockMvc.perform(get("/internal/v1/aulas")
                        .param("professorTurmaDisciplinaId", alocacaoId.toString())
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-aula-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].professorTurmaDisciplinaId").value(alocacaoId.toString()));
    }

    @Test
    void deveRegistrarEListarFrequenciasDaAula() throws Exception {
        UUID aulaId = UUID.randomUUID();
        aulaRepository.save(new AulaJpaEntity(
                aulaId,
                ESCOLA_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                """
                        {"id":"%s","professorTurmaDisciplinaId":"00000000-0000-0000-0000-000000000111","professorId":"00000000-0000-0000-0000-000000000101","professorNome":"Professor Aula","turmaId":"00000000-0000-0000-0000-000000000071","turmaNome":"Turma Aula","escolaId":"00000000-0000-0000-0000-000000000047","escolaNome":"Escola padrao","disciplinaId":"00000000-0000-0000-0000-000000000081","disciplinaNome":"Matematica","dataAula":"2038-03-10","horarioInicio":"07:30:00","horarioFim":"08:20:00","conteudoMinistrado":"Conteudo","observacao":"Observacao","realizada":true,"createdAt":"2038-03-10T07:00:00"}
                        """.formatted(aulaId),
                LocalDateTime.now()));

        mockMvc.perform(post("/internal/v1/aulas/{id}/frequencia-professor", aulaId)
                        .contentType("application/json")
                        .content("""
                                {"professorId":"00000000-0000-0000-0000-000000000101","professorNome":"Professor Aula","presente":true,"justificativa":"Presenca confirmada"}
                                """)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-aula-freq-prof-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.professorNome").value("Professor Aula"));

        mockMvc.perform(post("/internal/v1/aulas/{id}/frequencias-alunos", aulaId)
                        .contentType("application/json")
                        .content("""
                                {"matriculaId":"00000000-0000-0000-0000-000000000031","alunoId":"00000000-0000-0000-0000-000000000021","alunoNome":"Aluno Aula","situacao":"PRESENTE","justificativa":"Participou"}
                                """)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-aula-freq-aluno-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Aula"));

        mockMvc.perform(get("/internal/v1/aulas/{id}/frequencia-professor", aulaId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-aula-freq-prof-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aulaId").value(aulaId.toString()));

        mockMvc.perform(get("/internal/v1/aulas/{id}/frequencias-alunos", aulaId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-aula-freq-aluno-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].aulaId").value(aulaId.toString()))
                .andExpect(jsonPath("$[0].matriculaId").value("00000000-0000-0000-0000-000000000031"));
    }

    @Test
    void deveCriarEConsultarAvaliacaoENotas() throws Exception {
        UUID alocacaoId = UUID.randomUUID();

        String avaliacaoBody = mockMvc.perform(post("/internal/v1/avaliacoes")
                        .contentType("application/json")
                        .content("""
                                {
                                  "professorTurmaDisciplinaId":"%s",
                                  "professorId":"00000000-0000-0000-0000-000000000101",
                                  "professorNome":"Professor Avaliacao",
                                  "turmaId":"00000000-0000-0000-0000-000000000071",
                                  "turmaNome":"Turma Avaliacao",
                                  "disciplinaId":"00000000-0000-0000-0000-000000000081",
                                  "disciplinaNome":"Matematica",
                                  "titulo":"Prova fase 130",
                                  "descricao":"Avaliacao integrada",
                                  "dataAplicacao":"2039-04-15",
                                  "valorMaximo":10.00,
                                  "peso":1.00,
                                  "tipoAvaliacao":"PROVA"
                                }
                                """.formatted(alocacaoId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.professorTurmaDisciplinaId").value(alocacaoId.toString()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String avaliacaoId = avaliacaoBody.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/internal/v1/avaliacoes/{id}/notas", avaliacaoId)
                        .contentType("application/json")
                        .content("""
                                {
                                  "matriculaId":"00000000-0000-0000-0000-000000000031",
                                  "alunoId":"00000000-0000-0000-0000-000000000021",
                                  "alunoNome":"Aluno Nota",
                                  "nota":8.75,
                                  "observacao":"Bom desempenho"
                                }
                                """)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-nota-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.avaliacaoId").value(avaliacaoId))
                .andExpect(jsonPath("$.alunoNome").value("Aluno Nota"));

        mockMvc.perform(get("/internal/v1/avaliacoes/{id}", avaliacaoId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(avaliacaoId));

        mockMvc.perform(get("/internal/v1/avaliacoes/{id}/notas", avaliacaoId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].avaliacaoId").value(avaliacaoId));

        mockMvc.perform(get("/internal/v1/matriculas/{matriculaId}/notas", "00000000-0000-0000-0000-000000000031")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-avaliacao-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].matriculaId").value("00000000-0000-0000-0000-000000000031"));
    }

    @Test
    void deveCarregarESalvarDiarioClasse() throws Exception {
        UUID professorId = UUID.randomUUID();
        UUID turmaId = UUID.randomUUID();
        UUID disciplinaId = UUID.randomUUID();
        diarioClasseRepository.save(new DiarioClasseJpaEntity(
                "diario-2058-06-x",
                ESCOLA_ID,
                professorId,
                turmaId,
                disciplinaId,
                2058,
                6,
                java.time.LocalDate.parse("2058-06-26"),
                """
                        {
                          "cabecalho":{"idProfessor":"%s","idTurma":"%s","idDisciplina":"%s","anoLetivo":2058,"mes":6},
                          "alunos":[{"nome":"Aluno Diario","frequencias":{"12":"P"}}],
                          "conteudosPlanejados":[{"periodo":"Aula 1","descricao":"Conteudo planejado"}],
                          "observacoes":["Observacao interna"],
                          "avaliacoes":[{"descricao":"Prova mensal","valor":"0 a 10"}],
                          "assinatura":{"nomeProfessor":"","dataAssinatura":""},
                          "bloqueado":false
                        }
                        """.formatted(professorId, turmaId, disciplinaId),
                "{\"idDiarioClasse\":\"diario-2058-06-x\",\"status\":\"SALVO\"}",
                LocalDateTime.now()));

        mockMvc.perform(get("/internal/v1/diarios-classe")
                        .param("idProfessor", professorId.toString())
                        .param("idTurma", turmaId.toString())
                        .param("idDisciplina", disciplinaId.toString())
                        .param("anoLetivo", "2058")
                        .param("mes", "6")
                        .param("dataReferencia", "2058-06-26")
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-diario-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cabecalho.idProfessor").value(professorId.toString()))
                .andExpect(jsonPath("$.alunos[0].nome").value("Aluno Diario"));

        mockMvc.perform(put("/internal/v1/diarios-classe/{idDiarioClasse}", "diario-2058-06-x")
                        .contentType("application/json")
                        .content("""
                                {
                                  "idDiarioClasse":"diario-2058-06-x",
                                  "dataLancamento":"2058-06-26",
                                  "frequencias":[{"idAluno":"00000000-0000-0000-0000-000000000021","situacao":"PRESENTE"}]
                                }
                                """)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-diario-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDiarioClasse").value("diario-2058-06-x"))
                .andExpect(jsonPath("$.status").value("SALVO"));
    }

    @Test
    void deveCarregarCriarEAtualizarHistoricoEscolar() throws Exception {
        UUID historicoId = UUID.randomUUID();
        UUID alunoId = UUID.randomUUID();
        UUID matriculaId = UUID.randomUUID();
        historicoEscolarRepository.save(new HistoricoEscolarJpaEntity(
                historicoId,
                ESCOLA_ID,
                alunoId,
                matriculaId,
                "EDICAO",
                """
                        {
                          "contexto":{"idHistoricoEscolar":"%s","idAluno":"%s","idMatricula":"%s","modo":"EDICAO","status":"RASCUNHO","serieMatriculaAtual":6,"serieConcluidaOrigem":5,"escolaOrigem":"Escola Origem","dataTransferencia":"2026-07-12","bloqueado":false},
                          "cabecalho":null,
                          "aluno":null,
                          "periodos":[],
                          "baseComum":[],
                          "parteDiversificada":[],
                          "totais":null,
                          "estudosRealizados":[],
                          "observacoes":"Observacoes",
                          "certificado":null,
                          "pendencias":[]
                        }
                        """.formatted(historicoId, alunoId, matriculaId),
                "{\"id\":\"" + historicoId + "\"}",
                LocalDateTime.now()));

        mockMvc.perform(get("/internal/v1/historicos-escolares/novo")
                        .param("idAluno", UUID.randomUUID().toString())
                        .param("idMatricula", UUID.randomUUID().toString())
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-historico-1")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.modo").value("CADASTRO"));

        mockMvc.perform(get("/internal/v1/historicos-escolares/{id}/carregamento", historicoId)
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-historico-2")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contexto.idHistoricoEscolar").value(historicoId.toString()))
                .andExpect(jsonPath("$.contexto.modo").value("EDICAO"));

        mockMvc.perform(post("/internal/v1/historicos-escolares")
                        .contentType("application/json")
                        .content("""
                                {"nomeAluno":"Aluno Pedagogico","alunoId":"%s","componentesCurriculares":[]}
                                """.formatted(alunoId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-historico-3")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.alunoId").value(alunoId.toString()))
                .andExpect(jsonPath("$.nomeAluno").value("Aluno Pedagogico"));

        mockMvc.perform(put("/internal/v1/historicos-escolares/{id}", historicoId)
                        .contentType("application/json")
                        .content("""
                                {"nomeAluno":"Aluno Atualizado","alunoId":"%s","componentesCurriculares":[]}
                                """.formatted(alunoId))
                        .header("X-Internal-Token", INTERNAL_TOKEN)
                        .header("X-Correlation-Id", "corr-pedagogical-historico-4")
                        .header("X-Usuario-Id", UUID.randomUUID())
                        .header("X-Escola-Id", ESCOLA_ID)
                        .header("Authorization", AUTHORIZATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(historicoId.toString()))
                .andExpect(jsonPath("$.nomeAluno").value("Aluno Atualizado"));
    }
}
