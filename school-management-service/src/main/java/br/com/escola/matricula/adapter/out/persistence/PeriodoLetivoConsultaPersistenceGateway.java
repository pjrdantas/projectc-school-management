package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.catalogo.adapter.out.persistence.repository.PeriodoLetivoJpaRepository;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.application.port.out.PeriodoLetivoConsultaGateway;

@Component
public class PeriodoLetivoConsultaPersistenceGateway implements PeriodoLetivoConsultaGateway {

    private final PeriodoLetivoJpaRepository periodoLetivoJpaRepository;
    private final EscolaContextoPort escolaContextoPort;

    public PeriodoLetivoConsultaPersistenceGateway(
            PeriodoLetivoJpaRepository periodoLetivoJpaRepository,
            EscolaContextoPort escolaContextoPort) {
        this.periodoLetivoJpaRepository = periodoLetivoJpaRepository;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return periodoLetivoJpaRepository.existsByIdAndEscola_Id(id, escolaContextoPort.obterContextoPadrao().escolaId());
    }
}
