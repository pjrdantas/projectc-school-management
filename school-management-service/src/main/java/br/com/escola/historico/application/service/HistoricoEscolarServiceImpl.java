package br.com.escola.historico.application.service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarItemRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.historico.application.mapper.HistoricoEscolarMapper;
import br.com.escola.historico.domain.exception.HistoricoEscolarInvalidoException;
import br.com.escola.historico.domain.exception.HistoricoEscolarNaoEncontradoException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HistoricoEscolarServiceImpl implements HistoricoEscolarService {

    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final HistoricoEscolarMapper historicoEscolarMapper;

    @Override
    @Transactional
    public HistoricoEscolarResponse criar(HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarMapper.toEntity(request);
        return historicoEscolarMapper.toResponse(historicoEscolarJpaRepository.save(historico));
    }

    @Override
    @Transactional
    public HistoricoEscolarResponse atualizar(UUID id, HistoricoEscolarRequest request) {
        validarRequest(request);
        var historico = historicoEscolarJpaRepository.findWithComponentesCurricularesById(id)
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
        historicoEscolarMapper.copyToEntity(request, historico);
        return historicoEscolarMapper.toResponse(historicoEscolarJpaRepository.save(historico));
    }

    @Override
    @Transactional
    public void excluir(UUID id) {
        if (!historicoEscolarJpaRepository.existsById(id)) {
            throw new HistoricoEscolarNaoEncontradoException(id);
        }
        historicoEscolarJpaRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public HistoricoEscolarResponse buscarPorId(UUID id) {
        return historicoEscolarJpaRepository.findWithComponentesCurricularesById(id)
                .map(historicoEscolarMapper::toResponse)
                .orElseThrow(() -> new HistoricoEscolarNaoEncontradoException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HistoricoEscolarResponse> listar(Pageable pageable) {
        return historicoEscolarJpaRepository.findAll(pageable)
                .map(historicoEscolarMapper::toResponse);
    }

    private void validarRequest(HistoricoEscolarRequest request) {
        if (request.componentesCurriculares() == null || request.componentesCurriculares().isEmpty()) {
            throw new HistoricoEscolarInvalidoException("Informe ao menos um componente curricular no histórico escolar");
        }
        validarComponentesDuplicados(request.componentesCurriculares());
    }

    private void validarComponentesDuplicados(List<HistoricoEscolarItemRequest> itens) {
        var chaves = new HashSet<String>();
        for (HistoricoEscolarItemRequest item : itens) {
            String chave = normalizar(item.componenteCurricular())
                    + "|" + (item.anoLetivo() == null ? "" : item.anoLetivo())
                    + "|" + normalizar(item.serie());
            if (!chaves.add(chave)) {
                throw new HistoricoEscolarInvalidoException(
                        "Componente curricular duplicado no histórico: %s, ano letivo %s, série %s"
                                .formatted(
                                        item.componenteCurricular(),
                                        item.anoLetivo() == null ? "não informado" : item.anoLetivo(),
                                        item.serie() == null || item.serie().isBlank() ? "não informada" : item.serie()));
            }
        }
    }

    private String normalizar(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }
}
