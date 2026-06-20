package br.com.escola.catalog.domain.repository;

import java.util.List;
import java.util.Optional;

import br.com.escola.catalog.domain.model.Turno;

public interface TurnoRepository {

    Turno salvar(Turno turno);

    Optional<Turno> buscarTurnoPorCodigo(String codigo);

    List<Turno> listarTurnos();
}

