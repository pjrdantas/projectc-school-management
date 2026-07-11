package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleAddressWriteMonolithAdapterPlan;
import br.com.escola.peopleservice.application.dto.PeopleAddressWriteMonolithAdapterPlan.AdapterCandidateOperation;

@Service
public class PeopleAddressWriteMonolithAdapterPlanner {

    public PeopleAddressWriteMonolithAdapterPlan planejarAdapterEscritaMonolito() {
        return new PeopleAddressWriteMonolithAdapterPlan(
                "Fase 72",
                "address_write_monolith_adapter_diagnostic",
                "monolith_write_adapter_prepared_guard_disabled_no_cutover",
                "close_phase_72_and_plan_next_people_backend_scope",
                "phase_72_closure_no_write_cutover",
                true,
                true,
                false,
                false,
                List.of(
                        new AdapterCandidateOperation(
                                "create-or-update-principal-address",
                                "PessoaEnderecoWriteCommand",
                                "PUT /internal/pessoas/{pessoaId}/endereco-principal",
                                "school-management-service:PessoaEnderecoPort.atualizarEnderecoPrincipalDaPessoa",
                                false,
                                "adapter-implemented-but-guard-disabled-by-default"),
                        new AdapterCandidateOperation(
                                "cleanup-person-address-links-and-orphans",
                                "PessoaEnderecoCleanupCommand",
                                "DELETE /internal/pessoas/{pessoaId}/enderecos",
                                "school-management-service:PessoaEnderecoPort.removerEnderecosDaPessoaRemovendoOrfaos",
                                false,
                                "adapter-implemented-but-guard-disabled-by-default")),
                List.of(
                        "PUT /internal/pessoas/{pessoaId}/endereco-principal",
                        "DELETE /internal/pessoas/{pessoaId}/enderecos",
                        "Idempotency-Key header required",
                        "X-Correlation-Id, X-Usuario-Id and X-Escola-Id propagation required",
                        "response must include commandId, pessoaId, enderecoId, pessoaEnderecoId and fallback status",
                        "error contract for missing pessoa, invalid tipoEndereco and shared-address cleanup conflict"),
                List.of(
                        "people-service shadow command already validates commandId, pessoaId, escolaId and idempotencyKey",
                        "monolith internal write route exists and is covered by integration tests",
                        "MonolithPessoaAddressWriteClient implemented with HTTP PUT and DELETE contracts",
                        "monolith route keeps PessoaFoundationService and PessoaEnderecoPort as the only write authorities",
                        "people.shadow.monolith.address-write-adapter-enabled defaults to false",
                        "adapter failure returns fallback-required result without local persistence",
                        "write/read-model reconciliation remains green before any future activation"),
                List.of(
                        "address-write-is-still-coupled-to-person-update-transaction",
                        "create-person-with-address-cannot-be-adapted-without moving student/responsible creation flow",
                        "cleanup-can-delete-orphan-address-if-reference-count-safeguard-is-wrong",
                        "people-service-local-read-model-is-not-write-authority",
                        "viacep-is-input-enrichment-not-write-authority"),
                List.of(
                        "keep-PeopleAddressWriteFallbackService-returning-monolith_proxy",
                        "do-not-create-people-service-write-route",
                        "do-not-write-to-local-address-tables",
                        "disable-people.shadow.monolith.address-write-adapter-enabled",
                        "keep-student-and-responsible-flows-on-school-management-service"),
                List.of(
                        "activate-address-write-cutover",
                        "create-people-service-address-write-rest-route",
                        "bff-route-change",
                        "frontend-change",
                        "local-address-write-persistence",
                        "remove-monolith-address-write-path",
                        "move-create-person-with-address-flow"));
    }
}
