package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.state.LeituraModeloSyncSummary;
import br.com.escola.peopleservice.infra.config.LeituraModeloProperties;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class OrigemLeituraPolicyTest {

    @Test
    void deveManterMonolitoQuandoRoteamentoDeLeituraEstaDesligado() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(false, false, false, false, false, false, 500, true),
                meterRegistry,
                new LeituraModeloSyncState());

        var decision = guard.registrarDecisao("buscarPorId");

        assertThat(decision.operation()).isEqualTo("buscarPorId");
        assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decision.localReadRequested()).isFalse();
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-read-routing-disabled");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "buscarPorId",
                "selected_source", "monolith_proxy",
                "reason", "local-read-routing-disabled").count()).isEqualTo(1.0d);
    }

    @Test
    void deveBloquearRoteamentoQuandoPersistenciaLocalEstaDesligada() {
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(false, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry(),
                new LeituraModeloSyncState());

        var decision = guard.avaliar("listarTiposPessoa");

        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decision.reason()).isEqualTo("local-persistence-disabled");
    }

    @Test
    void deveExigirFallbackObrigatorioParaRoteamentoControlado() {
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, false),
                new SimpleMeterRegistry(),
                new LeituraModeloSyncState());

        var decision = guard.avaliar("consultarCadastro");

        assertThat(decision.fallbackEnabled()).isFalse();
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("fallback-required");
    }

    @Test
    void deveBloquearQuandoReconciliacaoTemDivergencias() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        Counter.builder("people.readmodel.sync.divergences")
                .tag("tabela", "pessoa")
                .tag("tipo", "cpf")
                .register(meterRegistry)
                .increment();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.avaliar("buscarPorId");

        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("reconciliation-has-divergences");
    }

    @Test
    void deveLiberarCatalogosBuscarPorIdEConsultaCadastroQuandoRelatorioLocalEstaVerde() {
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry(),
                greenState());

        var decisions = guard.avaliarTodas();

        assertThat(decisions).containsKeys(
                "listarTiposPessoa",
                "listarTiposEndereco",
                "listarStatusAluno",
                "listarParentescos",
                "buscarPorId",
                "consultarCadastro");
        assertThat(decisions).doesNotContainKey("endereco");
        assertThat(decisions.get("listarTiposPessoa").selectedSource()).isEqualTo("people_read_model_catalog");
        assertThat(decisions.get("listarTiposPessoa").localReadEligible()).isTrue();
        assertThat(decisions.get("listarTiposPessoa").reason()).isEqualTo("local-catalog-read-eligible");
        assertThat(decisions.get("listarTiposEndereco").selectedSource()).isEqualTo("people_read_model_catalog");
        assertThat(decisions.get("listarTiposEndereco").localReadEligible()).isTrue();
        assertThat(decisions.get("listarStatusAluno").selectedSource()).isEqualTo("people_read_model_catalog");
        assertThat(decisions.get("listarStatusAluno").localReadEligible()).isTrue();
        assertThat(decisions.get("listarParentescos").selectedSource()).isEqualTo("people_read_model_catalog");
        assertThat(decisions.get("listarParentescos").localReadEligible()).isTrue();
        assertThat(decisions.get("buscarPorId").selectedSource()).isEqualTo("people_read_model_identity");
        assertThat(decisions.get("buscarPorId").localReadEligible()).isTrue();
        assertThat(decisions.get("buscarPorId").reason()).isEqualTo("local-identity-read-eligible");
        assertThat(decisions.get("consultarCadastro").selectedSource())
                .isEqualTo("people_read_model_student_responsible");
        assertThat(decisions.get("consultarCadastro").localReadEligible()).isTrue();
        assertThat(decisions.get("consultarCadastro").reason())
                .isEqualTo("local-student-responsible-read-eligible");
    }

    @Test
    void deveLiberarOperacaoEnderecoQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraEndereco();

        assertThat(decision.operation()).isEqualTo("endereco");
        assertThat(decision.route()).isEqualTo("internal-operation:PessoaEnderecoPort");
        assertThat(decision.candidateSource()).isEqualTo("people_read_model_address");
        assertThat(decision.selectedSource()).isEqualTo("people_read_model_address");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-address-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "endereco",
                "selected_source", "people_read_model_address",
                "reason", "local-address-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.address.read.routing.decisions",
                "selected_source", "people_read_model_address",
                "reason", "local-address-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLiberarOperacaoContatoQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraContato();

        assertThat(decision.operation()).isEqualTo("contato");
        assertThat(decision.route()).isEqualTo("internal-operation:PessoaContatoPort");
        assertThat(decision.candidateSource()).isEqualTo("pessoa");
        assertThat(decision.selectedSource()).isEqualTo("people_read_model_identity");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-contact-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "contato",
                "selected_source", "people_read_model_identity",
                "reason", "local-contact-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.contact.read.routing.decisions",
                "selected_source", "people_read_model_identity",
                "reason", "local-contact-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLiberarOperacaoDocumentoQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraDocumentoMetadata();

        assertThat(decision.operation()).isEqualTo("documentoMetadata");
        assertThat(decision.route()).isEqualTo("internal-operation:PessoaDocumentoMetadataPort");
        assertThat(decision.candidateSource()).isEqualTo("people_documento_read_model");
        assertThat(decision.selectedSource()).isEqualTo("people_documento_read_model");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-document-metadata-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "documentoMetadata",
                "selected_source", "people_documento_read_model",
                "reason", "local-document-metadata-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.document.read.routing.decisions",
                "selected_source", "people_documento_read_model",
                "reason", "local-document-metadata-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLiberarOperacaoAlunoQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraAlunoVinculo();

        assertThat(decision.operation()).isEqualTo("alunoVinculo");
        assertThat(decision.route()).isEqualTo("internal-operation:AlunoPessoaPort");
        assertThat(decision.candidateSource()).isEqualTo("aluno");
        assertThat(decision.selectedSource()).isEqualTo("people_read_model_student_responsible");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-student-link-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "alunoVinculo",
                "selected_source", "people_read_model_student_responsible",
                "reason", "local-student-link-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.student.read.routing.decisions",
                "selected_source", "people_read_model_student_responsible",
                "reason", "local-student-link-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLiberarOperacaoResponsavelQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraResponsavelVinculo();

        assertThat(decision.operation()).isEqualTo("responsavelVinculo");
        assertThat(decision.route()).isEqualTo("internal-operation:ResponsavelPessoaPort");
        assertThat(decision.candidateSource()).isEqualTo("responsavel");
        assertThat(decision.selectedSource()).isEqualTo("people_read_model_student_responsible");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-responsible-link-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "responsavelVinculo",
                "selected_source", "people_read_model_student_responsible",
                "reason", "local-responsible-link-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.responsible.read.routing.decisions",
                "selected_source", "people_read_model_student_responsible",
                "reason", "local-responsible-link-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLiberarOperacaoFuncionarioQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraFuncionarioResumo();

        assertThat(decision.operation()).isEqualTo("funcionarioResumo");
        assertThat(decision.route()).isEqualTo("internal-operation:PessoaFuncionarioResumoPort");
        assertThat(decision.candidateSource()).isEqualTo("people_funcionario_read_model");
        assertThat(decision.selectedSource()).isEqualTo("people_funcionario_read_model");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-funcionario-internal-summary-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "funcionarioResumo",
                "selected_source", "people_funcionario_read_model",
                "reason", "local-funcionario-internal-summary-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.funcionario.read.routing.decisions",
                "selected_source", "people_funcionario_read_model",
                "reason", "local-funcionario-internal-summary-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLiberarOperacaoProfessorQuandoRelatorioLocalEstaVerde() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                greenState());

        var decision = guard.registrarDecisaoLeituraProfessorResumo();

        assertThat(decision.operation()).isEqualTo("professorResumo");
        assertThat(decision.route()).isEqualTo("internal-operation:PessoaProfessorResumoPort");
        assertThat(decision.candidateSource()).isEqualTo("people_professor_read_model");
        assertThat(decision.selectedSource()).isEqualTo("people_professor_read_model");
        assertThat(decision.localReadRequested()).isTrue();
        assertThat(decision.localReadEligible()).isTrue();
        assertThat(decision.fallbackEnabled()).isTrue();
        assertThat(decision.writesEnabled()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-professor-internal-summary-read-eligible");
        assertThat(meterRegistry.counter(
                "people.read.routing.decisions",
                "operation", "professorResumo",
                "selected_source", "people_professor_read_model",
                "reason", "local-professor-internal-summary-read-eligible").count()).isEqualTo(1.0d);
        assertThat(meterRegistry.counter(
                "people.professor.read.routing.decisions",
                "selected_source", "people_professor_read_model",
                "reason", "local-professor-internal-summary-read-eligible").count()).isEqualTo(1.0d);
    }

    @Test
    void deveBloquearCatalogosQuandoRelatorioLocalAindaNaoEstaVerde() {
        OrigemLeituraPolicy guard = new OrigemLeituraPolicy(
                new LeituraModeloProperties(true, false, true, false, true, true, 500, true),
                new SimpleMeterRegistry(),
                new LeituraModeloSyncState());

        var decision = guard.avaliar("listarTiposPessoa");

        assertThat(decision.selectedSource()).isEqualTo("monolith_proxy");
        assertThat(decision.localReadEligible()).isFalse();
        assertThat(decision.reason()).isEqualTo("local-read-model-backfill-not-green");
    }

    private LeituraModeloSyncState greenState() {
        LeituraModeloSyncState state = new LeituraModeloSyncState();
        state.update(new LeituraModeloSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                7,
                7,
                15,
                15,
                15,
                0,
                false,
                false,
                List.of()));
        return state;
    }
}



