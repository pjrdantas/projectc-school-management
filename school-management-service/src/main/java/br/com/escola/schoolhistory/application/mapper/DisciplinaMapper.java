package br.com.escola.schoolhistory.application.mapper;

import org.springframework.stereotype.Component;

import br.com.escola.schoolhistory.adapter.in.web.dto.DisciplinaRequest;
import br.com.escola.schoolhistory.adapter.in.web.dto.DisciplinaResponse;
import br.com.escola.schoolhistory.adapter.out.persistence.entity.DisciplinaEntity;

@Component
public class DisciplinaMapper {

    public DisciplinaEntity toEntity(DisciplinaRequest request) {
        DisciplinaEntity entity = new DisciplinaEntity();
        updateEntity(entity, request);
        return entity;
    }

    public void updateEntity(DisciplinaEntity entity, DisciplinaRequest request) {
        entity.setNome(request.nome());
        entity.setCargaHoraria(request.cargaHoraria());
        entity.setStatus(request.status() == null || request.status().isBlank() ? "ATIVA" : request.status());
    }

    public DisciplinaResponse toResponse(DisciplinaEntity entity) {
        return new DisciplinaResponse(
                entity.getId(),
                entity.getNome(),
                entity.getCargaHoraria(),
                entity.getStatus(),
                entity.getCreatedAt());
    }
}
