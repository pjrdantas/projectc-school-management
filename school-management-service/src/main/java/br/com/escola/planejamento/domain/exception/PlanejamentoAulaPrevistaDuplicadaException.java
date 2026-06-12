package br.com.escola.planejamento.domain.exception;

public class PlanejamentoAulaPrevistaDuplicadaException extends RuntimeException {

    public PlanejamentoAulaPrevistaDuplicadaException() {
        super("Já existe aula prevista com este número no planejamento bimestral.");
    }
}
