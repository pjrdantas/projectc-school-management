package br.com.projeto.piloto.adapter.in.web.exception;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo de resposta de erro da API")
public class ErrorResponse {

    @Schema(description = "Timestamp do erro")
    private LocalDateTime timestamp;

    @Schema(description = "Código HTTP do erro")
    private int status;

    @Schema(description = "Tipo do erro")
    private String error;

    @Schema(description = "Mensagem detalhada do erro")
    private String message;

    @Schema(description = "Caminho da requisição que gerou o erro")
    private String path;

    @Schema(description = "Detalhes de validação de campos (opcional)")
    private Map<String, String> validationErrors;
}
