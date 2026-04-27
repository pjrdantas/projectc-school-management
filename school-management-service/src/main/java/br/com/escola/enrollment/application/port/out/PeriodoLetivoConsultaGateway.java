package br.com.escola.enrollment.application.port.out;

import org.springframework.lang.NonNull;

public interface PeriodoLetivoConsultaGateway {

    boolean existsById(@NonNull Long id);
}
