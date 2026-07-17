package br.com.escola.professorservice.application.port.out;

import br.com.escola.professorservice.application.migration.MigracaoSnapshot;

public interface MigracaoDestinoPort {

    MigracaoSnapshot carregarSnapshot();

    void aplicar(MigracaoSnapshot snapshot);
}

