package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;
import br.com.escola.peopleservice.application.dto.PessoaProfessorResumoResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.port.out.PessoaCatalogoPort;
import br.com.escola.peopleservice.application.port.out.AlunoResponsavelPort;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;
import br.com.escola.peopleservice.infra.config.PeopleReadModelProperties;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

class PessoaQueryServiceTest {

    @Test
    void deveLerCatalogoLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID localId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakePessoaCatalogoPort(List.of(new PessoaCatalogoResponse(localId, "ALUNO", "Aluno")), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarTiposPessoa("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(localId, "ALUNO", "Aluno"));
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarTiposPessoa",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoLeituraLocalFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID monolithId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of(new PessoaCatalogoResponse(monolithId, "ALUNO", "Aluno"))),
                new FakePessoaCatalogoPort(List.of(), List.of(), true),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), true),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarTiposPessoa("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(monolithId, "ALUNO", "Aluno"));
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.catalog.reads",
                "operation", "listarTiposPessoa",
                "result", "fallback").count()).isEqualTo(1.0d);
    }

    @Test
    void deveLerPessoaPorIdDoReadModelLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        PessoaResumoResponse local = new PessoaResumoResponse(pessoaId, "Pessoa Local", escolaId, null, true);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.of(local), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarPessoaPorId("Bearer token", context(escolaId), pessoaId);

        assertThat(response).isEqualTo(local);
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.identity.reads",
                "operation", "buscarPorId",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoPessoaLocalNaoExiste() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        UUID pessoaId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        PessoaResumoResponse monolith = new PessoaResumoResponse(pessoaId, "Pessoa Monolito", escolaId, "Escola", true);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of(), Optional.of(monolith)),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarPessoaPorId("Bearer token", context(escolaId), pessoaId);

        assertThat(response).isEqualTo(monolith);
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.identity.reads",
                "operation", "buscarPorId",
                "result", "fallback_not_found").count()).isEqualTo(1.0d);
    }

    @Test
    void deveConsultarCadastroNoReadModelLocalQuandoGuardaPermite() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        var localPage = new PessoaConsultaCadastralPageResponse(List.of(), 3, 1, 15);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(localPage, false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.consultarCadastro(
                "Bearer token",
                context(),
                "Ana",
                null,
                "Rita",
                null,
                1,
                15);

        assertThat(response).isEqualTo(localPage);
        assertThat(monolithCalls).hasValue(0);
        assertThat(meterRegistry.counter(
                "people.studentresponsible.reads",
                "operation", "consultarCadastro",
                "result", "success").count()).isEqualTo(1.0d);
    }

    @Test
    void deveFazerFallbackParaMonolitoQuandoConsultaCadastroLocalFalha() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicInteger monolithCalls = new AtomicInteger();
        var monolithPage = new PessoaConsultaCadastralPageResponse(List.of(), 1, 0, 20);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(monolithCalls, List.of(), Optional.empty(), monolithPage),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), true),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.consultarCadastro(
                "Bearer token",
                context(),
                null,
                null,
                null,
                null,
                0,
                20);

        assertThat(response).isEqualTo(monolithPage);
        assertThat(monolithCalls).hasValue(1);
        assertThat(meterRegistry.counter(
                "people.studentresponsible.reads",
                "operation", "consultarCadastro",
                "result", "fallback_error").count()).isEqualTo(1.0d);
    }

    @Test
    void deveExporStatusAlunoPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID statusId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(
                                List.of(),
                                List.of(),
                                List.of(new PessoaCatalogoResponse(statusId, "ATIVO", "Ativo")),
                                List.of(),
                                false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarStatusAluno("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(statusId, "ATIVO", "Ativo"));
    }

    @Test
    void deveExporParentescosPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID parentescoId = UUID.randomUUID();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(
                                List.of(),
                                List.of(),
                                List.of(),
                                List.of(new PessoaCatalogoResponse(parentescoId, "MAE", "Mae")),
                                false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarParentescos("Bearer token", context());

        assertThat(response).containsExactly(new PessoaCatalogoResponse(parentescoId, "MAE", "Mae"));
    }

    @Test
    void deveExporEnderecoPrincipalPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID pessoaId = UUID.randomUUID();
        PessoaEnderecoResponse endereco = endereco(pessoaId, "01001000");
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.of(endereco), List.of(endereco)), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarEnderecoPrincipalPorPessoa("Bearer token", context(), pessoaId);

        assertThat(response).isEqualTo(endereco);
    }

    @Test
    void deveListarEnderecosPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID pessoaId = UUID.randomUUID();
        PessoaEnderecoResponse endereco = endereco(pessoaId, "01001000");
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.of(endereco), List.of(endereco)), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarEnderecosPorPessoa("Bearer token", context(), pessoaId);

        assertThat(response).containsExactly(endereco);
    }

    @Test
    void deveExporContatoPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID pessoaId = UUID.randomUUID();
        PessoaContatoResponse contato = new PessoaContatoResponse(
                pessoaId,
                UUID.randomUUID(),
                "ana.aluna@example.com",
                "11999999999",
                true);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(
                        new FakePessoaCatalogoPort(List.of(), List.of(), false),
                        greenGuard(meterRegistry),
                        meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.of(contato)), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarContatoPorPessoa("Bearer token", context(), pessoaId);

        assertThat(response).isEqualTo(contato);
    }

    @Test
    void deveExporDocumentoMetadataPorIdPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID pessoaId = UUID.randomUUID();
        PessoaDocumentoMetadataResponse documento = documento(pessoaId);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(new FakePessoaCatalogoPort(List.of(), List.of(), false), greenGuard(meterRegistry), meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.of(documento), List.of(documento)), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarDocumentoPorId("Bearer token", context(), documento.documentoId());

        assertThat(response).isEqualTo(documento);
    }

    @Test
    void deveListarDocumentosPorPessoaPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        UUID pessoaId = UUID.randomUUID();
        PessoaDocumentoMetadataResponse documento = documento(pessoaId);
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(new FakePessoaCatalogoPort(List.of(), List.of(), false), greenGuard(meterRegistry), meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.of(documento), List.of(documento)), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarDocumentosPorPessoa("Bearer token", context(), pessoaId);

        assertThat(response).containsExactly(documento);
    }

    @Test
    void deveExporFuncionarioResumoPorIdPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaFuncionarioResumoResponse funcionario = funcionario();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(new FakePessoaCatalogoPort(List.of(), List.of(), false), greenGuard(meterRegistry), meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.of(funcionario), List.of(funcionario)), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarFuncionarioPorId("Bearer token", context(), funcionario.funcionarioId());

        assertThat(response).isEqualTo(funcionario);
    }

    @Test
    void deveListarFuncionariosAtivosPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaFuncionarioResumoResponse funcionario = funcionario();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(new FakePessoaCatalogoPort(List.of(), List.of(), false), greenGuard(meterRegistry), meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.of(funcionario), List.of(funcionario)), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarFuncionariosAtivosPorEscola("Bearer token", context());

        assertThat(response).containsExactly(funcionario);
    }

    @Test
    void deveExporProfessorResumoPorIdPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorResumoResponse professor = professor();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(new FakePessoaCatalogoPort(List.of(), List.of(), false), greenGuard(meterRegistry), meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.of(professor), List.of(professor)), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.buscarProfessorPorId("Bearer token", context(), professor.professorId());

        assertThat(response).isEqualTo(professor);
    }

    @Test
    void deveListarProfessoresPeloContratoInternoLocal() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        PessoaProfessorResumoResponse professor = professor();
        PessoaQueryService service = new PessoaQueryService(
                new FakePessoaReadPort(new AtomicInteger(), List.of()),
                new FakePessoaCatalogoPort(List.of(), List.of(), false),
                new FakePessoaPort(Optional.empty(), false),
                new FakeAlunoResponsavelPort(new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20), false),
                catalogoAlunoResponsavelService(new FakePessoaCatalogoPort(List.of(), List.of(), false), greenGuard(meterRegistry), meterRegistry),
                enderecoService(new FakePessoaEnderecoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                contatoService(new FakePessoaContatoPort(Optional.empty()), greenGuard(meterRegistry), meterRegistry),
                documentoMetadataService(new FakePessoaDocumentoMetadataPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                funcionarioResumoService(new FakePessoaFuncionarioResumoPort(Optional.empty(), List.of()), greenGuard(meterRegistry), meterRegistry),
                professorResumoService(new FakePessoaProfessorResumoPort(Optional.of(professor), List.of(professor)), greenGuard(meterRegistry), meterRegistry),
                greenGuard(meterRegistry),
                meterRegistry);

        var response = service.listarProfessoresPorEscola("Bearer token", context());

        assertThat(response).containsExactly(professor);
    }

    private PessoaAlunoResponsavelCatalogoService catalogoAlunoResponsavelService(
            PessoaCatalogoPort catalogoPort,
            PeopleReadSourcePolicy readRoutingPolicy,
            SimpleMeterRegistry meterRegistry) {
        ObjectProvider<PessoaCatalogoPort> provider = new ObjectProvider<>() {
            @Override
            public PessoaCatalogoPort getObject(Object... args) {
                return catalogoPort;
            }

            @Override
            public PessoaCatalogoPort getIfAvailable() {
                return catalogoPort;
            }

            @Override
            public PessoaCatalogoPort getIfUnique() {
                return catalogoPort;
            }

            @Override
            public PessoaCatalogoPort getObject() {
                return catalogoPort;
            }
        };
        return new PessoaAlunoResponsavelCatalogoService(provider, readRoutingPolicy, meterRegistry);
    }

    private PessoaEnderecoService enderecoService(
            FakePessoaEnderecoPort enderecoPort,
            PeopleReadSourcePolicy readRoutingPolicy,
            SimpleMeterRegistry meterRegistry) {
        return new PessoaEnderecoService(enderecoPort, readRoutingPolicy, meterRegistry);
    }

    private PessoaContatoService contatoService(
            FakePessoaContatoPort contatoPort,
            PeopleReadSourcePolicy readRoutingPolicy,
            SimpleMeterRegistry meterRegistry) {
        ObjectProvider<br.com.escola.peopleservice.application.port.out.PessoaContatoPort> provider = new ObjectProvider<>() {
            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaContatoPort getObject(Object... args) {
                return contatoPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaContatoPort getIfAvailable() {
                return contatoPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaContatoPort getIfUnique() {
                return contatoPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaContatoPort getObject() {
                return contatoPort;
            }
        };
        return new PessoaContatoService(provider, readRoutingPolicy, meterRegistry);
    }

    private PessoaDocumentoMetadataService documentoMetadataService(
            FakePessoaDocumentoMetadataPort documentoPort,
            PeopleReadSourcePolicy readRoutingPolicy,
            SimpleMeterRegistry meterRegistry) {
        ObjectProvider<br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort> provider = new ObjectProvider<>() {
            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort getObject(Object... args) {
                return documentoPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort getIfAvailable() {
                return documentoPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort getIfUnique() {
                return documentoPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort getObject() {
                return documentoPort;
            }
        };
        return new PessoaDocumentoMetadataService(provider, readRoutingPolicy, meterRegistry);
    }

    private PessoaFuncionarioResumoService funcionarioResumoService(
            FakePessoaFuncionarioResumoPort funcionarioPort,
            PeopleReadSourcePolicy readRoutingPolicy,
            SimpleMeterRegistry meterRegistry) {
        ObjectProvider<br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort> provider = new ObjectProvider<>() {
            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort getObject(Object... args) {
                return funcionarioPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort getIfAvailable() {
                return funcionarioPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort getIfUnique() {
                return funcionarioPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort getObject() {
                return funcionarioPort;
            }
        };
        return new PessoaFuncionarioResumoService(provider, readRoutingPolicy, meterRegistry);
    }

    private PessoaProfessorResumoService professorResumoService(
            FakePessoaProfessorResumoPort professorPort,
            PeopleReadSourcePolicy readRoutingPolicy,
            SimpleMeterRegistry meterRegistry) {
        ObjectProvider<br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort> provider = new ObjectProvider<>() {
            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort getObject(Object... args) {
                return professorPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort getIfAvailable() {
                return professorPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort getIfUnique() {
                return professorPort;
            }

            @Override
            public br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort getObject() {
                return professorPort;
            }
        };
        return new PessoaProfessorResumoService(provider, readRoutingPolicy, meterRegistry);
    }

    private PessoaEnderecoResponse endereco(UUID pessoaId, String cep) {
        return new PessoaEnderecoResponse(
                UUID.randomUUID(),
                pessoaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "RESIDENCIAL",
                "Residencial",
                true,
                cep,
                "Praca da Se",
                "100",
                null,
                "Se",
                "Sao Paulo",
                "SP");
    }

    private PessoaDocumentoMetadataResponse documento(UUID pessoaId) {
        return new PessoaDocumentoMetadataResponse(
                UUID.randomUUID(),
                pessoaId,
                UUID.randomUUID(),
                UUID.randomUUID(),
                "CPF",
                "CPF",
                "12345678900",
                "/tmp/cpf.pdf",
                "Documento principal",
                java.time.OffsetDateTime.parse("2026-01-02T10:15:30Z"));
    }

    private PessoaFuncionarioResumoResponse funcionario() {
        return new PessoaFuncionarioResumoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Funcionario Interno",
                "Secretaria",
                true);
    }

    private PessoaProfessorResumoResponse professor() {
        return new PessoaProfessorResumoResponse(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Professor Interno",
                true);
    }

    private PeopleReadSourcePolicy greenGuard(SimpleMeterRegistry meterRegistry) {
        PeopleReadModelSyncState state = new PeopleReadModelSyncState();
        state.update(new PeopleReadModelSyncSummary(
                true,
                true,
                "completed",
                "local-read-model-backfill-and-reconciliation-completed",
                500,
                4,
                4,
                12,
                12,
                12,
                0,
                false,
                false,
                List.of()));
        return new PeopleReadSourcePolicy(
                new PeopleReadModelProperties(true, false, true, false, true, true, 500, true),
                meterRegistry,
                state);
    }

    private InternalRequestContext context() {
        return context(UUID.randomUUID());
    }

    private InternalRequestContext context(UUID escolaId) {
        return new InternalRequestContext("corr", UUID.randomUUID(), escolaId);
    }

    private record FakePessoaCatalogoPort(
            List<PessoaCatalogoResponse> tiposPessoa,
            List<PessoaCatalogoResponse> tiposEndereco,
            List<PessoaCatalogoResponse> statusAluno,
            List<PessoaCatalogoResponse> parentescos,
            boolean fail) implements PessoaCatalogoPort {

        private FakePessoaCatalogoPort(
                List<PessoaCatalogoResponse> tiposPessoa,
                List<PessoaCatalogoResponse> tiposEndereco,
                boolean fail) {
            this(tiposPessoa, tiposEndereco, List.of(), List.of(), fail);
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposPessoa() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return tiposPessoa;
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposEndereco() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return tiposEndereco;
        }

        @Override
        public List<PessoaCatalogoResponse> listarStatusAluno() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return statusAluno;
        }

        @Override
        public List<PessoaCatalogoResponse> listarParentescos() {
            if (fail) {
                throw new IllegalStateException("local-failed");
            }
            return parentescos;
        }
    }

    private record FakePessoaPort(
            Optional<PessoaResumoResponse> response,
            boolean fail) implements br.com.escola.peopleservice.application.port.out.PessoaPort {

        @Override
        public Optional<PessoaResumoResponse> buscarPessoaPorId(UUID pessoaId, UUID escolaId) {
            if (fail) {
                throw new IllegalStateException("identity-local-failed");
            }
            return response;
        }
    }

    private record FakeAlunoResponsavelPort(
            PessoaConsultaCadastralPageResponse response,
            boolean fail) implements AlunoResponsavelPort {

        @Override
        public PessoaConsultaCadastralPageResponse consultarCadastro(
                String nomeAluno,
                String cpfAluno,
                String nomeResponsavel,
                String cpfResponsavel,
                int page,
                int size) {
            if (fail) {
                throw new IllegalStateException("student-responsible-local-failed");
            }
            return response;
        }
    }

    private record FakePessoaEnderecoPort(
            Optional<PessoaEnderecoResponse> principal,
            List<PessoaEnderecoResponse> enderecos) implements br.com.escola.peopleservice.application.port.out.PessoaEnderecoPort {

        @Override
        public Optional<PessoaEnderecoResponse> buscarEnderecoPrincipalPorPessoa(UUID pessoaId, UUID escolaId) {
            return principal;
        }

        @Override
        public List<PessoaEnderecoResponse> listarEnderecosPorPessoa(UUID pessoaId, UUID escolaId) {
            return enderecos;
        }
    }

    private record FakePessoaContatoPort(
            Optional<PessoaContatoResponse> contato) implements br.com.escola.peopleservice.application.port.out.PessoaContatoPort {

        @Override
        public Optional<PessoaContatoResponse> buscarContatoPorPessoa(UUID pessoaId, UUID escolaId) {
            return contato;
        }
    }

    private record FakePessoaDocumentoMetadataPort(
            Optional<PessoaDocumentoMetadataResponse> documento,
            List<PessoaDocumentoMetadataResponse> documentos) implements br.com.escola.peopleservice.application.port.out.PessoaDocumentoMetadataPort {

        @Override
        public Optional<PessoaDocumentoMetadataResponse> buscarDocumentoPorId(UUID documentoId, UUID escolaId) {
            return documento;
        }

        @Override
        public List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(UUID pessoaId, UUID escolaId) {
            return documentos;
        }
    }

    private record FakePessoaFuncionarioResumoPort(
            Optional<PessoaFuncionarioResumoResponse> funcionario,
            List<PessoaFuncionarioResumoResponse> funcionarios) implements br.com.escola.peopleservice.application.port.out.PessoaFuncionarioResumoPort {

        @Override
        public Optional<PessoaFuncionarioResumoResponse> buscarFuncionarioPorId(UUID funcionarioId, UUID escolaId) {
            return funcionario;
        }

        @Override
        public List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(UUID escolaId) {
            return funcionarios;
        }
    }

    private record FakePessoaProfessorResumoPort(
            Optional<PessoaProfessorResumoResponse> professor,
            List<PessoaProfessorResumoResponse> professores) implements br.com.escola.peopleservice.application.port.out.PessoaProfessorResumoPort {

        @Override
        public Optional<PessoaProfessorResumoResponse> buscarProfessorPorId(UUID professorId, UUID escolaId) {
            return professor;
        }

        @Override
        public List<PessoaProfessorResumoResponse> listarProfessoresPorEscola(UUID escolaId) {
            return professores;
        }
    }

    private record FakePessoaReadPort(
            AtomicInteger catalogCalls,
            List<PessoaCatalogoResponse> tiposPessoa,
            Optional<PessoaResumoResponse> pessoa,
            PessoaConsultaCadastralPageResponse consultaCadastroResponse) implements PessoaReadPort {

        private FakePessoaReadPort(AtomicInteger catalogCalls, List<PessoaCatalogoResponse> tiposPessoa) {
            this(catalogCalls, tiposPessoa, Optional.empty(), new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20));
        }

        private FakePessoaReadPort(
                AtomicInteger catalogCalls,
                List<PessoaCatalogoResponse> tiposPessoa,
                Optional<PessoaResumoResponse> pessoa) {
            this(catalogCalls, tiposPessoa, pessoa, new PessoaConsultaCadastralPageResponse(List.of(), 0, 0, 20));
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context) {
            catalogCalls.incrementAndGet();
            return tiposPessoa;
        }

        @Override
        public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
            catalogCalls.incrementAndGet();
            return List.of();
        }

        @Override
        public Optional<PessoaResumoResponse> buscarPessoaPorId(
                String authorization,
                InternalRequestContext context,
                UUID pessoaId) {
            catalogCalls.incrementAndGet();
            return pessoa;
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
            catalogCalls.incrementAndGet();
            return consultaCadastroResponse;
        }
    }
}


