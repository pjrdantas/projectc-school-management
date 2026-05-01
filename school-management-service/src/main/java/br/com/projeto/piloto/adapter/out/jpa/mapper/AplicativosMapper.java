package br.com.projeto.piloto.adapter.out.jpa.mapper;

import br.com.projeto.piloto.adapter.in.web.dto.AplicativosRequestDTO;
import br.com.projeto.piloto.adapter.in.web.dto.AplicativosResponseDTO;
import br.com.projeto.piloto.adapter.out.jpa.entity.Aplicativo;
import br.com.projeto.piloto.domain.model.AplicativosModel;

public class AplicativosMapper {

    public static AplicativosModel toDomain(Aplicativo entity) {
        if (entity == null) return null;
        return AplicativosModel.builder()
                .id(entity.getId())
                .nmAplicativo(entity.getNmAplicativo())
                .dsAplicativo(entity.getDsAplicativo())
                .dsUrl(entity.getDsUrl())
                .nmModulo(entity.getNmModulo())
                .moduloExposto(entity.getModuloExposto())
                .dsRota(entity.getDsRota())
                .flAtivo(entity.getFlAtivo())
                .criadoEm(entity.getCriadoEm())
                .atualizadoEm(entity.getAtualizadoEm())
                .build();
    }

    public static Aplicativo toEntity(AplicativosModel domain) {
        if (domain == null) return null;
        return Aplicativo.builder()
                .id(domain.getId())
                .nmAplicativo(domain.getNmAplicativo())
                .dsAplicativo(domain.getDsAplicativo())
                .dsUrl(domain.getDsUrl())
                .nmModulo(domain.getNmModulo())
                .moduloExposto(domain.getModuloExposto())
                .dsRota(domain.getDsRota())
                .flAtivo(domain.getFlAtivo())
                .build();
    }

    public static AplicativosModel toDomain(AplicativosRequestDTO dto) {
        if (dto == null) return null;
        return AplicativosModel.builder()
                .nmAplicativo(dto.nome()) 
                .dsAplicativo(dto.descricao())
                .dsUrl(dto.url())
                .nmModulo(dto.moduleName())
                .moduloExposto(dto.exposedModule())
                .dsRota(dto.routePath())
                .flAtivo("S".equalsIgnoreCase(dto.ativo()) ? "S" : "N")
                .build();
    }

    public static AplicativosResponseDTO toResponse(AplicativosModel domain) {
        if (domain == null) return null;
        return new AplicativosResponseDTO(
                domain.getId(),
                domain.getNmAplicativo(),
                domain.getDsAplicativo(),
                domain.getDsUrl(),
                domain.getNmModulo(),
                domain.getModuloExposto(),
                domain.getDsRota(),
                domain.getFlAtivo(),
                domain.getCriadoEm(),
                domain.getAtualizadoEm()
        );
    }
}
 