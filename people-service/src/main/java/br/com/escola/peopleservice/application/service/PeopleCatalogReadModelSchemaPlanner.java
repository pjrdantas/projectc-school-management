package br.com.escola.peopleservice.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.dto.PeopleCatalogReadModelSchemaPlan;
import br.com.escola.peopleservice.application.dto.PeopleCatalogReadModelSchemaPlan.TableSchemaDecision;

@Service
public class PeopleCatalogReadModelSchemaPlanner {

    public PeopleCatalogReadModelSchemaPlan planejarSchemaCatalogo() {
        return new PeopleCatalogReadModelSchemaPlan(
                "local_catalog_read_adapter_prepared",
                "close_phase_63_and_plan_next_people_service_scope",
                true,
                true,
                true,
                false,
                false,
                List.of(
                        new TableSchemaDecision(
                                "tipo_pessoa",
                                "id_tipo_pessoa",
                                "codigo",
                                List.of("id_tipo_pessoa", "codigo", "descricao", "created_at"),
                                "monolith.tipo_pessoa",
                                "listarTiposPessoa",
                                true,
                                "global_catalog_without_tenant_or_transactional_write"),
                        new TableSchemaDecision(
                                "tipo_endereco",
                                "id_tipo_endereco",
                                "codigo",
                                List.of("id_tipo_endereco", "codigo", "descricao"),
                                "monolith.tipo_endereco",
                                "listarTiposEndereco",
                                true,
                                "global_catalog_without_tenant_or_transactional_write")),
                List.of(
                        "pessoa",
                        "pessoa_tipo_pessoa",
                        "endereco",
                        "pessoa_endereco",
                        "aluno",
                        "responsavel",
                        "funcionario",
                        "professor",
                        "aluno_responsavel",
                        "pessoa_documento"),
                List.of(
                        "catalog-backfill-and-reconciliation-must-run-green-before-local-read",
                        "fallback-to-monolith-remains-mandatory",
                        "read-cutover-must-keep-monolith-fallback"),
                List.of(
                        "disable-people.shadow.local-persistence.read-model-cutover-enabled",
                        "disable-people.shadow.local-persistence.backfill-enabled",
                        "disable-people.shadow.local-persistence.reconciliation-enabled",
                        "disable-people.shadow.local-persistence.migration-enabled",
                        "do-not-enable-read-model-cutover",
                        "keep-pessoa-read-port-on-monolith-proxy"));
    }
}
