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
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Service
public class DisciplinaService {

    private final DisciplinaJpaRepository disciplinaJpaRepository;
    private final DisciplinaMapper disciplinaMapper;
    private final EscolaJpaRepository escolaJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public DisciplinaService(
            DisciplinaJpaRepository disciplinaJpaRepository,
            DisciplinaMapper disciplinaMapper,
            EscolaJpaRepository escolaJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.disciplinaJpaRepository = disciplinaJpaRepository;
        this.disciplinaMapper = disciplinaMapper;
        this.escolaJpaRepository = escolaJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Transactional
    public DisciplinaResponse criar(DisciplinaRequest request) {
        DisciplinaEntity entity = disciplinaMapper.toEntity(request);
        entity.setEscola(resolverEscola(request.escolaId()));
        return disciplinaMapper.toResponse(disciplinaJpaRepository.save(entity));
    }

    @Transactional
    public DisciplinaResponse atualizar(UUID id, DisciplinaRequest request) {
        EscolaEntity escola = resolverEscola(request.escolaId());
        DisciplinaEntity entity = disciplinaJpaRepository.findByIdAndEscola_Id(id, escola.getId())
                .orElseThrow(() -> new DisciplinaNaoEncontradaException(id));
        disciplinaMapper.updateEntity(entity, request);
        entity.setEscola(escola);
        return disciplinaMapper.toResponse(disciplinaJpaRepository.save(entity));
    }

    @Transactional
    public void excluir(UUID id) {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        if (!disciplinaJpaRepository.existsByIdAndEscola_Id(id, escolaId)) {
            throw new DisciplinaNaoEncontradaException(id);
        }
        disciplinaJpaRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public DisciplinaResponse buscarPorId(UUID id) {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        return disciplinaJpaRepository.findByIdAndEscola_Id(id, escolaId)
                .map(disciplinaMapper::toResponse)
                .orElseThrow(() -> new DisciplinaNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<DisciplinaResponse> listar() {
        UUID escolaId = escolaTenantService.obterOuCriarEscolaPadrao().getId();
        return disciplinaJpaRepository.findAllByEscola_Id(escolaId).stream()
                .map(disciplinaMapper::toResponse)
                .toList();
    }

    private EscolaEntity resolverEscola(UUID escolaId) {
        if (escolaId == null) {
            return escolaTenantService.obterOuCriarEscolaPadrao();
        }
        return escolaJpaRepository.findById(escolaId)
                .orElseThrow(() -> new IllegalArgumentException("Escola não encontrada."));
    }
}
