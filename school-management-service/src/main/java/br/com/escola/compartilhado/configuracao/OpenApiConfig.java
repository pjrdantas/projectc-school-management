package br.com.escola.compartilhado.configuracao;

import java.util.Locale;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

@Configuration
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class OpenApiConfig {

    @Bean
    OpenAPI schoolManagementOpenApi() {
        String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("School Management Service API")
                        .description("API para gerenciamento escolar")
                        .version("v1"))
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }

    @Bean
    OpenApiCustomizer removeControllerSuffixFromTags() {
        return openApi -> {
            if (openApi.getTags() != null) {
                openApi.getTags().forEach(tag -> tag.setName(normalizeTag(tag.getName())));
            }
            if (openApi.getPaths() != null) {
                openApi.getPaths().values().forEach(pathItem -> pathItem.readOperations().forEach(operation -> {
                    if (operation.getTags() != null) {
                        operation.setTags(operation.getTags().stream()
                                .map(this::normalizeTag)
                                .distinct()
                                .toList());
                    }
                }));
            }
        };
    }

    private String normalizeTag(String tag) {
        if (tag == null) {
            return null;
        }
        String suffix = "-controller";
        if (tag.toLowerCase(Locale.ROOT).endsWith(suffix)) {
            return tag.substring(0, tag.length() - suffix.length());
        }
        return tag;
    }
}
