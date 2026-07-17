package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.state.LeituraModeloMigrationSummary;

@Component
public class LeituraModeloMigrationState {

    private final AtomicReference<LeituraModeloMigrationSummary> lastReport = new AtomicReference<>(
            new LeituraModeloMigrationSummary(
                    false,
                    false,
                    true,
                    "disabled",
                    "schema-migration-disabled",
                    List.of("classpath:db/people-readmodel/migration"),
                    List.of(
                            "tipo_pessoa",
                            "tipo_endereco",
                            "status_aluno",
                            "parentesco",
                            "pessoa",
                            "pessoa_tipo_pessoa",
                            "aluno",
                            "responsavel",
                            "aluno_responsavel",
                            "endereco",
                            "pessoa_endereco",
                            "people_funcionario_read_model",
                            "people_professor_read_model"),
                    0));

    public LeituraModeloMigrationSummary currentReport() {
        return lastReport.get();
    }

    public void update(LeituraModeloMigrationSummary report) {
        lastReport.set(report);
    }
}


