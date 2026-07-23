package br.com.escola.enrollmentdocumentservice.infra.config;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("enrollment-document.document-storage")
public record DocumentoArquivoStorageProperties(String rootDirectory) {

    public DocumentoArquivoStorageProperties {
        if (rootDirectory == null || rootDirectory.isBlank()) {
            rootDirectory = Path.of(
                    System.getProperty("java.io.tmpdir"),
                    "enrollment-document-service",
                    "documents").toString();
        }
    }

    public Path rootPath() {
        return Path.of(rootDirectory).toAbsolutePath().normalize();
    }
}
