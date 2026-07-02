package br.com.escola.documento.adapter.out.storage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "documento.storage.s3")
public class DocumentoS3StorageProperties {

    private String bucket = "school-documents";
    private String prefix = "documentos";
    private String region = "us-east-1";
    private String endpoint;
    private String accessKey;
    private String secretKey;
    private boolean pathStyleAccess = true;

    public String getBucket() {
        return bucket;
    }

    public void setBucket(String bucket) {
        if (bucket != null && !bucket.isBlank()) {
            this.bucket = bucket.trim();
        }
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix.trim();
    }

    public String normalizedPrefix() {
        return prefix.replace('\\', '/').replaceAll("^/+", "").replaceAll("/+$", "");
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        if (region != null && !region.isBlank()) {
            this.region = region.trim();
        }
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint == null || endpoint.isBlank() ? null : endpoint.trim();
    }

    public boolean hasEndpoint() {
        return endpoint != null && !endpoint.isBlank();
    }

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public boolean hasStaticCredentials() {
        return accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank();
    }

    public boolean isPathStyleAccess() {
        return pathStyleAccess;
    }

    public void setPathStyleAccess(boolean pathStyleAccess) {
        this.pathStyleAccess = pathStyleAccess;
    }
}
