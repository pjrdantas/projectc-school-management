package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.state.PeopleReadModelSyncSummary;

@Component
public class PeopleReadModelSyncState {

    private final AtomicReference<PeopleReadModelSyncSummary> lastReport = new AtomicReference<>(
            new PeopleReadModelSyncSummary(
                    false,
                    false,
                    "disabled",
                    "operation-flags-disabled",
                    0,
                    9,
                    0,
                    0,
                    0,
                    0,
                    0,
                    false,
                    false,
                    List.of()));

    public PeopleReadModelSyncSummary currentReport() {
        return lastReport.get();
    }

    public void update(PeopleReadModelSyncSummary report) {
        lastReport.set(report);
    }
}

