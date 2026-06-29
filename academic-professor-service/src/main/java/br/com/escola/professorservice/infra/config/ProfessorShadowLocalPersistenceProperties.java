package br.com.escola.professorservice.infra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "professor.shadow.local-persistence")
public record ProfessorShadowLocalPersistenceProperties(
        boolean enabled,
        boolean failOnError,
        boolean buscarPorIdCutoverEnabled) {
}
