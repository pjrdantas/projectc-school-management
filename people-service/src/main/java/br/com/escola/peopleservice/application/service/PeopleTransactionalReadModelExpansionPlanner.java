package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan;
import br.com.escola.peopleservice.application.dto.PeopleTransactionalReadModelExpansionPlan.TableExpansionDecision;

@Service
public class PeopleTransactionalReadModelExpansionPlanner {

    public PeopleTransactionalReadModelExpansionPlan planejarProximaFatiaTransacional() {
        return new PeopleTransactionalReadModelExpansionPlan(
                "pessoa_address_diagnostic_prepared_without_cutover",
                "prepare_address_schema_opt_in_for_consultar_cadastro",
                "pessoa_address_read_model",
                false,
                false,
                false,
                List.of(
                        new TableExpansionDecision(
                                "pessoa",
                                "id_pessoa",
                                List.of(
                                        "id_pessoa",
                                        "id_escola",
                                        "escola_nome",
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
                                false,
                                true,
                                true,
                                true,
                                "identity_local_read_already_prepared_with_mandatory_fallback"),
                        new TableExpansionDecision(
                                "pessoa_tipo_pessoa",
                                "id_pessoa_tipo_pessoa",
                                List.of("id_pessoa_tipo_pessoa", "id_pessoa", "id_tipo_pessoa", "created_at"),
                                List.of("pessoa", "tipo_pessoa"),
                                List.of("buscarPorId"),
                                false,
                                true,
                                true,
                                true,
                                "identity_local_read_dependency_already_prepared"),
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
                                true,
                                false,
                                false,
                                false,
                                "requires_address_contract_mapping_before_schema"),
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
                                true,
                                false,
                                false,
                                false,
                                "requires_endereco_schema_and_backfill_before_consultar_cadastro")),
                List.of(
                        "keep-current-identity-local-read-behind-green-reconciliation",
                        "map-consultarCadastro-response-before-schema",
                        "define-address-read-model-without-owning-address-writes",
                        "keep-address-schema-migration-for-next-subphase",
                        "keep-consultarCadastro-on-monolith-until-address-backfill-is-green",
                        "keep-pii-read-model-without-public-exposure",
                        "keep-school-scope-as-copied-identifier-without-people-service-owning-tenant",
                        "define-reconciliation-by-address-id-and-person-address-link",
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
                        "keep-pessoa-identity-local-read-on-monolith-fallback",
                        "keep-read-model-cutover-disabled-for-transactional-routes",
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "keep-consultarCadastro-on-monolith-proxy",
                        "disable-people.shadow.local-persistence.enabled-if-operational-risk-appears"));
    }
}
