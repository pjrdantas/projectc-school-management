package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.dto.PeopleLocalPersistenceOperationReport;

@Component
public class PeopleLocalPersistenceOperationState {

    private final AtomicReference<PeopleLocalPersistenceOperationReport> lastReport = new AtomicReference<>(
            new PeopleLocalPersistenceOperationReport(
                    false,
                    false,
                    "disabled",
                    "operation-flags-disabled",
                    0,
                    2,
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    false,
                    List.of()));

    public PeopleLocalPersistenceOperationReport currentReport() {
        return lastReport.get();
    }

    public void update(PeopleLocalPersistenceOperationReport report) {
        lastReport.set(report);
    }
}
