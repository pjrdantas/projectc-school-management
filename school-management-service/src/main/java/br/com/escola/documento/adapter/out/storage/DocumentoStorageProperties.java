package br.com.escola.documento.adapter.out.storage;

import java.nio.file.Path;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "documento.storage.local")
public class DocumentoStorageProperties {

    private Path root = Path.of("uploads", "documentos");

    public Path getRoot() {
        return root;
    }

    public void setRoot(Path root) {
        if (root != null) {
            this.root = root;
        }
    }

    public Path normalizedRoot() {
        return root.normalize();
    }
}
