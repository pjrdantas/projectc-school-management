package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.state.PeopleReadModelMigrationSummary;

@Component
public class PeopleReadModelMigrationState {

    private final AtomicReference<PeopleReadModelMigrationSummary> lastReport = new AtomicReference<>(
            new PeopleReadModelMigrationSummary(
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
                            "pessoa_endereco",
                            "people_funcionario_read_model"),
                    0));

    public PeopleReadModelMigrationSummary currentReport() {
        return lastReport.get();
    }

    public void update(PeopleReadModelMigrationSummary report) {
        lastReport.set(report);
    }
}

