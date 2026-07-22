package br.com.escola.enrollmentdocumentservice.infra.migration;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import br.com.escola.enrollmentdocumentservice.infra.config.DocumentoBackfillProperties;
import br.com.escola.enrollmentdocumentservice.infra.persistence.DocumentoBackfillExecutor;

@Component
@ConditionalOnBean(DocumentoBackfillExecutor.class)
@Order(2)
public class DocumentoBackfillRunner implements ApplicationRunner {

    private final DocumentoBackfillExecutor executor;
    private final DocumentoBackfillProperties properties;

    public DocumentoBackfillRunner(DocumentoBackfillExecutor executor, DocumentoBackfillProperties properties) {
        this.executor = executor;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.failOnMismatch() && !executor.execute().reconciled()) {
            throw new IllegalStateException("document-backfill-not-reconciled");
        }
    }
}
