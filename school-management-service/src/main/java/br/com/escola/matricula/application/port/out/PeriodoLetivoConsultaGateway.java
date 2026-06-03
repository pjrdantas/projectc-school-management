package br.com.escola.matricula.application.port.out;

import java.util.UUID;

import org.springframework.lang.NonNull;

public interface PeriodoLetivoConsultaGateway {

    boolean existsById(@NonNull UUID id);
}
