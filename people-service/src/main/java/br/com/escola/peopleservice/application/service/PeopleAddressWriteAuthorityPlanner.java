package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleAddressWriteAuthorityPlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressWriteAuthorityPlan.WriteAuthorityDecision;

@Service
public class PeopleAddressWriteAuthorityPlanner {

    public PeopleAddressWriteAuthorityPlan planejarAutoridadeEscritaEndereco() {
        return new PeopleAddressWriteAuthorityPlan(
                "Fase 71",
                "endereco_write_authority",
                "diagnostic_started_no_write_cutover",
                "define_address_write_command_contract_no_external_route",
                "address_write_command_contract_diagnostic_no_persistence_change",
                false,
                false,
                false,
                true,
                List.of(
                        new WriteAuthorityDecision(
                                "create-person-with-principal-address",
                                "school-management-service:PessoaFoundationService.criarPessoaComTipoEEndereco",
                                "people-service:future PeopleAddressWritePort",
                                List.of("CriarAlunoUseCase", "CriarResponsavelUseCase"),
                                List.of("pessoa", "pessoa_tipo_pessoa", "endereco", "pessoa_endereco"),
                                false,
                                "person-and-address-are-created-in-single-monolith-transaction"),
                        new WriteAuthorityDecision(
                                "update-person-principal-address",
                                "school-management-service:PessoaFoundationService.atualizarPessoaEEndereco",
                                "people-service:future PeopleAddressWritePort",
                                List.of("AtualizarAlunoUseCase", "AtualizarResponsavelUseCase"),
                                List.of("pessoa", "endereco", "pessoa_endereco"),
                                false,
                                "principal-address-update-still-depends-on-monolith-person-transaction"),
                        new WriteAuthorityDecision(
                                "remove-person-address-links-and-orphans",
                                "school-management-service:PessoaEnderecoPort.removerEnderecosDaPessoaRemovendoOrfaos",
                                "people-service:future PeopleAddressCleanupPort",
                                List.of("AlunoPersistenceGateway.removeById", "ResponsavelPersistenceGateway.removeById"),
                                List.of("pessoa_endereco", "endereco"),
                                false,
                                "orphan-cleanup-needs-cross-person-reference-count-safeguard"),
                        new WriteAuthorityDecision(
                                "cep-lookup-for-address-input",
                                "school-management-service:ViaCepService",
                                "people-service:future external CepLookupPort",
                                List.of(
                                        "EnderecoCepController",
                                        "CriarAlunoUseCase",
                                        "AtualizarAlunoUseCase",
                                        "CriarResponsavelUseCase",
                                        "AtualizarResponsavelUseCase",
                                        "TransferenciaAlunoService"),
                                List.of(),
                                false,
                                "viacep-is-input-enrichment-not-write-authority")),
                List.of(
                        "PessoaFoundationService.criarPessoaComTipoEEndereco",
                        "PessoaFoundationService.atualizarPessoaEEndereco",
                        "PessoaEnderecoPort.removerEnderecosDaPessoaRemovendoOrfaos",
                        "EnderecoJpaRepository",
                        "PessoaEnderecoJpaRepository",
                        "TipoEnderecoJpaRepository",
                        "ViaCepService"),
                List.of(
                        "PeopleAddressWritePort command payload without JPA entities",
                        "idempotency key for pessoa_endereco write attempts",
                        "principal address invariant: at most one principal address per person",
                        "orphan cleanup contract with shared-address safeguard",
                        "monolith fallback contract before any write routing",
                        "reconciliation report for writes before and after activation"),
                List.of(
                        "address-write-is-coupled-to-person-create-update-transaction",
                        "student-and-responsible-use-cases-own-input-flow-but-not-address-persistence",
                        "orphan-address-removal-can-delete-shared-data-if-reference-count-is-wrong",
                        "viacep-failure-must-not-create-partial-person-address-state",
                        "local-read-model-is-not-write-authority"),
                List.of(
                        "keep-address-writes-on-school-management-service",
                        "do-not-enable-people-service-address-write-route",
                        "disable-future-address-write-routing-flag",
                        "keep-address-local-read-guard-independent-from-write-authority",
                        "rerun-address-backfill-and-reconciliation-after-any-failed-write-pilot"),
                List.of(
                        "create-address-write-rest-route",
                        "bff-route-change",
                        "frontend-change",
                        "write-to-local-address-tables",
                        "remove-monolith-address-write-path",
                        "change-consultarCadastro-payload"));
    }
}
