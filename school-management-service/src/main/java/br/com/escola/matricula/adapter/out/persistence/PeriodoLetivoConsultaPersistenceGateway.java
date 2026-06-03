package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.matricula.application.port.out.PeriodoLetivoConsultaGateway;

@Component
public class PeriodoLetivoConsultaPersistenceGateway implements PeriodoLetivoConsultaGateway {

    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;

    public PeriodoLetivoConsultaPersistenceGateway(PeriodoLetivoJpaRepository periodoLetivoJpaRepository) {
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return periodoLetivoJpaRepository.existsById(id);
    }
}
