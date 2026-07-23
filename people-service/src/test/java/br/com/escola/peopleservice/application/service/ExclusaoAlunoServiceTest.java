package br.com.escola.peopleservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import br.com.escola.peopleservice.application.context.InternalRequestContext;

class ExclusaoAlunoServiceTest {

    @Test
    void deveExcluirAlunoSomenteNaEscolaDoContexto() {
        UUID alunoId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        AtomicReference<UUID> alunoRecebido = new AtomicReference<>();
        AtomicReference<UUID> escolaRecebida = new AtomicReference<>();
        ExclusaoAlunoService service = new ExclusaoAlunoService((id, escola) -> {
            alunoRecebido.set(id);
            escolaRecebida.set(escola);
        });

        service.excluir(
                alunoId,
                new InternalRequestContext("corr-delete", UUID.randomUUID(), escolaId));

        assertThat(alunoRecebido.get()).isEqualTo(alunoId);
        assertThat(escolaRecebida.get()).isEqualTo(escolaId);
    }
}
