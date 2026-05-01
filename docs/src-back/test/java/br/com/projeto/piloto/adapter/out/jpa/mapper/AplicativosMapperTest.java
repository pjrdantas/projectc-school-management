package br.com.projeto.piloto.adapter.out.jpa.mapper;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import br.com.projeto.piloto.adapter.in.web.dto.AplicativosRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AplicativosResponseDTO;
import br.com.projeto.piloto.adapter.out.jpa.entity.Aplicativo;
import br.com.projeto.piloto.domain.model.AplicativosModel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.LocalDateTime;

class AplicativosMapperTest {

    @Test
    @DisplayName("Deve converter Entidade para Domínio com sucesso")
    void deveConverterEntidadeParaDominio() {
        LocalDateTime agora = LocalDateTime.now();
        Aplicativo entity = Aplicativo.builder()
                .id(1L).nmAplicativo("App").dsAplicativo("Desc").dsUrl("URL")
                .nmModulo("Mod").moduloExposto("Exp").dsRota("Rota").flAtivo("S")
                .criadoEm(agora).atualizadoEm(agora)
                .build();

        AplicativosModel model = AplicativosMapper.toDomain(entity);

        assertNotNull(model);
        assertEquals(entity.getId(), model.getId());
        assertEquals(entity.getNmAplicativo(), model.getNmAplicativo());
        assertEquals(agora, model.getCriadoEm());
        assertNull(AplicativosMapper.toDomain((Aplicativo) null));
    }

    @Test
    @DisplayName("Deve converter Domínio para Entidade com sucesso")
    void deveConverterDominioParaEntidade() {
        AplicativosModel model = AplicativosModel.builder()
                .id(1L).nmAplicativo("App").dsAplicativo("Desc").dsUrl("URL")
                .nmModulo("Mod").moduloExposto("Exp").dsRota("Rota").flAtivo("N")
                .build();

        Aplicativo entity = AplicativosMapper.toEntity(model);

        assertNotNull(entity);
        assertEquals(model.getId(), entity.getId());
        assertEquals(model.getFlAtivo(), entity.getFlAtivo());
        assertNull(AplicativosMapper.toEntity(null));
    }

    @Test
    @DisplayName("Deve converter Request DTO para Domínio e validar lógica do flAtivo")
    void deveConverterDtoParaDominio() {
        AplicativosRequestDTO dtoAtivo = new AplicativosRequestDTO(
                "Nome", "Desc", "URL", "Mod", "Exp", "Rota", "S");
        AplicativosRequestDTO dtoInativo = new AplicativosRequestDTO(
                "Nome", "Desc", "URL", "Mod", "Exp", "Rota", "X");

        AplicativosModel modelAtivo = AplicativosMapper.toDomain(dtoAtivo);
        AplicativosModel modelInativo = AplicativosMapper.toDomain(dtoInativo);

        assertEquals("S", modelAtivo.getFlAtivo());
        assertEquals("N", modelInativo.getFlAtivo()); 
        assertNull(AplicativosMapper.toDomain((AplicativosRequestDTO) null));
    }

    @Test
    @DisplayName("Deve converter Domínio para Response DTO")
    void deveConverterDominioParaResponse() {
        LocalDateTime agora = LocalDateTime.now();
        AplicativosModel model = AplicativosModel.builder()
                .id(1L).nmAplicativo("App").dsAplicativo("Desc")
                .flAtivo("S").criadoEm(agora).atualizadoEm(agora)
                .build();

        AplicativosResponseDTO response = AplicativosMapper.toResponse(model);

        assertNotNull(response);
        assertEquals(model.getId(), response.id());
        assertEquals(model.getNmAplicativo(), response.nome());
        assertNull(AplicativosMapper.toResponse(null));
    }
    
    @Test
    void deveCobrirTudo() {
        new AplicativosMapper(); 
        var dtoS = new AplicativosRequestDTO("A", "B", "C", "D", "E", "F", "S");
        assertEquals("S", AplicativosMapper.toDomain(dtoS).getFlAtivo());
        var dtoN = new AplicativosRequestDTO("A", "B", "C", "D", "E", "F", "N");
        assertEquals("N", AplicativosMapper.toDomain(dtoN).getFlAtivo());
    }

}
