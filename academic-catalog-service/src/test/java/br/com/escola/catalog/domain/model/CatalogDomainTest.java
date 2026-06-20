package br.com.escola.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.catalog.domain.exception.CatalogDomainException;
import br.com.escola.catalog.domain.valueobject.EscolaId;

class CatalogDomainTest {

    private static final EscolaId ESCOLA = new EscolaId(UUID.randomUUID());

    @Test
    void deveRejeitarPeriodoComDatasInvertidas() {
        assertThatThrownBy(() -> new PeriodoLetivo(
                UUID.randomUUID(), ESCOLA, "2026", 2026,
                LocalDate.of(2026, 12, 1), LocalDate.of(2026, 1, 1),
                true, LocalDateTime.now()))
                .isInstanceOf(CatalogDomainException.class)
                .hasMessageContaining("dataFim");
    }

    @Test
    void deveNormalizarCodigoDoTurno() {
        Turno turno = new Turno(UUID.randomUUID(), " manha ", "Manha");

        assertThat(turno.codigo()).isEqualTo("MANHA");
    }

    @Test
    void deveRejeitarCargaHorariaInvalida() {
        assertThatThrownBy(() -> new Disciplina(
                UUID.randomUUID(), ESCOLA, "Matematica", 0, true, LocalDateTime.now()))
                .isInstanceOf(CatalogDomainException.class)
                .hasMessageContaining("cargaHoraria");
    }
}

