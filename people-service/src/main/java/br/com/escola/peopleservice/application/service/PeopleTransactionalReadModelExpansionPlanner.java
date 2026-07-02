package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan;
import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan.TableExpansionDecision;

@Service
public class PeopleTransactionalReadModelExpansionPlanner {

    public PeopleTransactionalReadModelExpansionPlan planejarProximaFatiaTransacional() {
        return new PeopleTransactionalReadModelExpansionPlan(
                "pessoa_identity_local_read_prepared_with_mandatory_fallback",
                "close_phase_64_and_plan_next_people_service_scope",
                "pessoa_identity_read_model",
                true,
                true,
                true,
                List.of(
                        new TableExpansionDecision(
                                "pessoa",
                                "id_pessoa",
                                List.of(
                                        "id_pessoa",
                                        "id_escola",
                                        "nome_completo",
                                        "cpf",
                                        "rg",
                                        "orgao_emissor_rg",
                                        "uf_rg",
                                        "email",
                                        "telefone",
                                        "data_nascimento",
                                        "sexo",
                                        "nome_social",
                                        "nacionalidade",
                                        "naturalidade",
                                        "ativo",
                                        "created_at",
                                        "updated_at"),
                                List.of("catalogo_local_tipo_pessoa_reconciliado"),
                                List.of("buscarPorId"),
                                true,
                                true,
                                true,
                                true,
                                "local_identity_read_prepared_for_buscar_por_id_with_mandatory_fallback"),
                        new TableExpansionDecision(
                                "pessoa_tipo_pessoa",
                                "id_pessoa_tipo_pessoa",
                                List.of("id_pessoa_tipo_pessoa", "id_pessoa", "id_tipo_pessoa", "created_at"),
                                List.of("pessoa", "tipo_pessoa"),
                                List.of("buscarPorId"),
                                true,
                                true,
                                true,
                                true,
                                "local_identity_read_dependency_prepared_for_buscar_por_id"),
                        new TableExpansionDecision(
                                "endereco",
                                "id_endereco",
                                List.of(
                                        "id_endereco",
                                        "cep",
                                        "logradouro",
                                        "numero",
                                        "complemento",
                                        "bairro",
                                        "cidade",
                                        "uf",
                                        "created_at",
                                        "updated_at"),
                                List.of("pessoa_endereco", "tipo_endereco"),
                                List.of("consultarCadastro"),
                                false,
                                false,
                                false,
                                false,
                                "defer_until_identity_slice_is_reconciled"),
                        new TableExpansionDecision(
                                "pessoa_endereco",
                                "id_pessoa_endereco",
                                List.of(
                                        "id_pessoa_endereco",
                                        "id_pessoa",
                                        "id_endereco",
                                        "id_tipo_endereco",
                                        "principal",
                                        "created_at"),
                                List.of("pessoa", "endereco", "tipo_endereco"),
                                List.of("consultarCadastro"),
                                false,
                                false,
                                false,
                                false,
                                "defer_until_identity_slice_and_address_contract_are_defined")),
                List.of(
                        "run-schema-migration-only-with-explicit-opt-in",
                        "run-identity-backfill-only-with-explicit-opt-in",
                        "keep-read-model-cutover-behind-explicit-flag-and-green-reconciliation",
                        "keep-pii-read-model-without-public-exposure",
                        "keep-school-scope-as-copied-identifier-without-people-service-owning-tenant",
                        "define-reconciliation-by-id-cpf-and-role",
                        "keep-monolith-as-authority-for-all-writes"),
                List.of(
                        "aluno",
                        "responsavel",
                        "funcionario",
                        "professor",
                        "aluno_responsavel",
                        "pessoa_documento"),
                List.of(
                        "disable-people.shadow.local-persistence.migration-enabled",
                        "leave-pessoa-identity-read-model-unused-until-backfill-is-green",
                        "keep-read-model-cutover-disabled-for-transactional-routes",
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "keep-consultarCadastro-on-monolith-proxy",
                        "disable-people.shadow.local-persistence.enabled-if-operational-risk-appears"));
    }
}
