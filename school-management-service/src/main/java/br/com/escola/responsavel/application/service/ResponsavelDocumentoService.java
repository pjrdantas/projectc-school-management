package br.com.escola.responsavel.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.responsavel.adapter.out.persistence.entity.ResponsavelEntity;
import br.com.escola.responsavel.adapter.out.persistence.repository.ResponsavelJpaRepository;
import br.com.escola.responsavel.application.port.internal.ResponsavelDocumentoPort;

@Service
@Primary
public class ResponsavelDocumentoService implements ResponsavelDocumentoPort {

    private final ResponsavelJpaRepository responsavelJpaRepository;

    public ResponsavelDocumentoService(ResponsavelJpaRepository responsavelJpaRepository) {
        this.responsavelJpaRepository = responsavelJpaRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ResponsavelEntity> buscarResponsavelPorIdEEscola(UUID responsavelId, UUID escolaId) {
        if (responsavelId == null || escolaId == null) {
            return Optional.empty();
        }
        return responsavelJpaRepository.findByIdAndPessoa_Escola_Id(responsavelId, escolaId);
    }
}
