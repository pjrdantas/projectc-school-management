package br.com.escola.documento.adapter.out.storage;

import java.net.URI;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

@Configuration
public class DocumentoS3StorageConfig {

    @Bean
    @ConditionalOnProperty(prefix = "documento.storage", name = "backend", havingValue = "s3")
    S3Client documentoS3Client(DocumentoS3StorageProperties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(properties.isPathStyleAccess())
                        .build());

        if (properties.hasEndpoint()) {
            builder.endpointOverride(URI.create(properties.getEndpoint()));
        }
        if (properties.hasStaticCredentials()) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())));
        }

        return builder.build();
    }
}
