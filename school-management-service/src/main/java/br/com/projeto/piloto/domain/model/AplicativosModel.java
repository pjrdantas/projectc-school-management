package br.com.projeto.piloto.domain.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonPropertyOrder({ "id", "nmAplicativo", "dsAplicativo", "dsUrl", "nmModulo", "moduloExposto", "dsRota", "flAtivo", "criadoEm", "atualizadoEm" })
public class AplicativosModel {
    private Long id;
    private String nmAplicativo;    
    private String dsAplicativo;    
    private String dsUrl;           
    private String nmModulo;        
    private String moduloExposto;   
    private String dsRota;          
    private String flAtivo;         
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
}
