package br.com.escola.catalogo.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.catalogo.adapter.in.web.dto.DisciplinaRequest;
import br.com.escola.catalogo.adapter.in.web.dto.DisciplinaResponse;
import br.com.escola.catalogo.adapter.out.persistence.entity.DisciplinaEntity;
import br.com.escola.catalogo.adapter.out.persistence.repository.DisciplinaJpaRepository;
import br.com.escola.catalogo.application.mapper.DisciplinaMapper;
import br.com.escola.catalogo.domain.exception.DisciplinaNaoEncontradaException;

@Service
public class DisciplinaService {

    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final DisciplinaMapper disciplinaMapper;

    public DisciplinaService(DisciplinaJpaRepository disciplinaJpaRepository, DisciplinaMapper disciplinaMapper) {
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.disciplinaMapper = disciplinaMapper;
    }

    @Transactional
    public DisciplinaResponse criar(DisciplinaRequest request) {
        return disciplinaMapper.toResponse(disciplinaJpaRepository.save(disciplinaMapper.toEntity(request)));
    }

    @Transactional
    public DisciplinaResponse atualizar(UUID id, DisciplinaRequest request) {
        DisciplinaEntity entity = disciplinaJpaRepository.findById(id)
                .orElseThrow(() -> new DisciplinaNaoEncontradaException(id));
        disciplinaMapper.updateEntity(entity, request);
        return disciplinaMapper.toResponse(disciplinaJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public DisciplinaResponse buscarPorId(UUID id) {
        return disciplinaJpaRepository.findById(id)
                .map(disciplinaMapper::toResponse)
                .orElseThrow(() -> new DisciplinaNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<DisciplinaResponse> listar() {
        return disciplinaJpaRepository.findAll().stream()
                .map(disciplinaMapper::toResponse)
                .toList();
    }
}
