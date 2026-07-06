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
                "monolith_http_write_contract_missing_adapter_blocked",
                "define_monolith_internal_address_write_http_contract_before_adapter",
                "monolith_internal_address_write_contract_no_people_adapter",
                false,
                false,
                false,
                false,
                List.of(
                        new AdapterCandidateOperation(
                                "create-or-update-principal-address",
                                "PessoaEnderecoWriteCommand",
                                "PUT /internal/pessoas/{pessoaId}/endereco-principal",
                                "school-management-service:PessoaFoundationService.atualizarPessoaEEndereco",
                                false,
                                "monolith-has-java-port-but-no-internal-http-write-contract"),
                        new AdapterCandidateOperation(
                                "cleanup-person-address-links-and-orphans",
                                "PessoaEnderecoCleanupCommand",
                                "DELETE /internal/pessoas/{pessoaId}/enderecos",
                                "school-management-service:PessoaEnderecoPort.removerEnderecosDaPessoaRemovendoOrfaos",
                                false,
                                "orphan-cleanup-needs-explicit-http-contract-and-reference-count-safeguard")),
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
                        "monolith route keeps PessoaFoundationService and PessoaEnderecoPort as the only write authorities",
                        "adapter feature flag defaults to disabled",
                        "adapter failure returns fallback-required result without local persistence",
                        "write/read-model reconciliation remains green before any future activation"),
                List.of(
                        "address-write-is-still-coupled-to-person-update-transaction",
                        "create-person-with-address-cannot-be-adapted-without moving student/responsible creation flow",
                        "cleanup-can-delete-orphan-address-if-reference-count-safeguard-is-wrong",
                        "people-service-local-read-model-is-not-write-authority",
                        "viacep-is-input-enrichment-not-write-authority"),
                List.of(
                        "keep-PeopleAddressWriteShadowService-returning-monolith_proxy",
                        "do-not-create-people-service-write-route",
                        "do-not-write-to-local-address-tables",
                        "disable-future-monolith-address-write-adapter-flag",
                        "keep-student-and-responsible-flows-on-school-management-service"),
                List.of(
                        "implement-people-service-monolith-write-client",
                        "create-people-service-address-write-rest-route",
                        "bff-route-change",
                        "frontend-change",
                        "local-address-write-persistence",
                        "remove-monolith-address-write-path",
                        "move-create-person-with-address-flow"));
    }
}
