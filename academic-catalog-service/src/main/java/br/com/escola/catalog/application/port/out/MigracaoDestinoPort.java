package br.com.escola.catalog.application.port.out;

import br.com.escola.catalog.application.migration.MigracaoSnapshot;

public interface MigracaoDestinoPort {

    void aplicar(MigracaoSnapshot snapshot);

    MigracaoSnapshot carregarSnapshot();
}

