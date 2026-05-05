package br.com.escola.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth") 
public class AuthProperties {
    
    private Jwt jwt = new Jwt();

    @Data
    public static class Jwt {
        private String secret = "bWluaGEtY2hhdmUtc3VwZXItc2VjcmV0YQ==";
        private Long expirationMs = 1_800_000L;
        private Long refreshExpirationMs = 604_800_000L;
    }
}