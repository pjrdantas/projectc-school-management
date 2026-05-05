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
        private String secret = "MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=";
        private Long expirationMs = 1_800_000L;
        private Long refreshExpirationMs = 604_800_000L;
    }
}