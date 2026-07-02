package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan;
import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan.TableExpansionDecision;

@Service
public class PeopleTransactionalReadModelExpansionPlanner {

    public PeopleTransactionalReadModelExpansionPlan planejarProximaFatiaTransacional() {
        return new PeopleTransactionalReadModelExpansionPlan(
                "diagnostic_transactional_slice_not_ready_for_physical_schema",
                "prepare_pessoa_identity_slice_contract_before_migration",
                "pessoa_identity_read_model",
                false,
                false,
                false,
                List.of(
                        new TableExpansionDecision(
                                "pessoa",
                                "id_pessoa",
                                List.of(
                                        "id_pessoa",
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
                                false,
                                false,
                                false,
                                "first_transactional_candidate_but_contains_pii_and_requires_identity_contract"),
                        new TableExpansionDecision(
                                "pessoa_tipo_pessoa",
                                "id_pessoa_tipo_pessoa",
                                List.of("id_pessoa_tipo_pessoa", "id_pessoa", "id_tipo_pessoa", "created_at"),
                                List.of("pessoa", "tipo_pessoa"),
                                List.of("buscarPorId"),
                                true,
                                false,
                                false,
                                false,
                                "required_to_preserve_person_roles_with_catalog_foreign_key"),
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
                        "define-pii-field-contract-and-masking-policy",
                        "define-school-scope-for-person-read-model-without-people-service-owning-tenant",
                        "define-idempotent-backfill-order-pessoa-before-pessoa_tipo_pessoa",
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
                        "do-not-create-transactional-migration-in-this-diagnostic-phase",
                        "keep-read-model-cutover-disabled-for-transactional-routes",
                        "keep-buscarPorId-and-consultarCadastro-on-monolith-proxy",
                        "disable-people.shadow.local-persistence.enabled-if-operational-risk-appears"));
    }
}
