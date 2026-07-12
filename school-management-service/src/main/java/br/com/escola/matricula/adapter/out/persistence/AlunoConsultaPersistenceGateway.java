package br.com.escola.matricula.adapter.out.persistence;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import br.com.escola.aluno.application.port.internal.AlunoMatriculaPort;
import br.com.escola.institucional.application.port.EscolaContextoPort;
import br.com.escola.matricula.application.port.out.AlunoConsultaGateway;

@Component
public class AlunoConsultaPersistenceGateway implements AlunoConsultaGateway {

    private final AlunoMatriculaPort alunoMatriculaPort;
    private final EscolaContextoPort escolaContextoPort;

    public AlunoConsultaPersistenceGateway(
            AlunoMatriculaPort alunoMatriculaPort,
            EscolaContextoPort escolaContextoPort) {
        this.alunoMatriculaPort = alunoMatriculaPort;
        this.escolaContextoPort = escolaContextoPort;
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return alunoMatriculaPort.existeAlunoPorIdEEscola(id, escolaContextoPort.obterContextoPadrao().escolaId());
    }
}
