package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.stereotype.Component;

import br.com.escola.peopleservice.application.state.LeituraModeloSyncSummary;

@Component
public class LeituraModeloSyncState {

    private final AtomicReference<LeituraModeloSyncSummary> lastReport = new AtomicReference<>(
            new LeituraModeloSyncSummary(
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

    public LeituraModeloSyncSummary currentReport() {
        return lastReport.get();
    }

    public void update(LeituraModeloSyncSummary report) {
        lastReport.set(report);
    }
}


