package br.com.escola.enrollmentdocumentservice.infra.config;

import java.nio.file.Path;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.util.StringUtils;

import br.com.escola.enrollmentdocumentservice.application.port.out.DocumentoArquivoStoragePort;
import br.com.escola.enrollmentdocumentservice.infra.persistence.DocumentoBackfillExecutor;

@Configuration
public class DocumentoBackfillConfiguration {

    @Bean
    @ConditionalOnProperty(name = "enrollment-document.document-backfill.enabled", havingValue = "true")
    DocumentoBackfillExecutor documentoBackfillExecutor(
            DocumentoBackfillProperties properties,
            DataSource targetDataSource,
            DocumentoArquivoStoragePort documentoArquivoStoragePort) {
        if (!StringUtils.hasText(properties.sourceUrl())) {
            throw new IllegalStateException("document-backfill-source-url-required");
        }
        if (!StringUtils.hasText(properties.sourceStorageRoot())) {
            throw new IllegalStateException("document-backfill-source-storage-root-required");
        }
        return new DocumentoBackfillExecutor(
                new JdbcTemplate(new DriverManagerDataSource(
                        properties.sourceUrl(), properties.sourceUsername(), properties.sourcePassword())),
                new JdbcTemplate(targetDataSource),
                documentoArquivoStoragePort,
                Path.of(properties.sourceStorageRoot()),
                properties.batchSize());
    }
}
