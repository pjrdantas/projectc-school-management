package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.dto.PeopleLocalReadModelSchemaMigrationReport;

@Component
public class PeopleLocalReadModelSchemaMigrationState {

    private final AtomicReference<PeopleLocalReadModelSchemaMigrationReport> lastReport = new AtomicReference<>(
            new PeopleLocalReadModelSchemaMigrationReport(
                    false,
                    false,
                    true,
                    "disabled",
                    "schema-migration-disabled",
                    List.of("classpath:db/people-readmodel/migration"),
                    List.of(
                            "tipo_pessoa",
                            "tipo_endereco",
                            "pessoa",
                            "pessoa_tipo_pessoa",
                            "aluno",
                            "responsavel",
                            "aluno_responsavel",
                            "endereco",
                            "pessoa_endereco"),
                    0));

    public PeopleLocalReadModelSchemaMigrationReport currentReport() {
        return lastReport.get();
    }

    public void update(PeopleLocalReadModelSchemaMigrationReport report) {
        lastReport.set(report);
    }
}
