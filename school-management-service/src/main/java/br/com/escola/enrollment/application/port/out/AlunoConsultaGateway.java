package br.com.escola.enrollment.application.port.out;

import org.springframework.lang.NonNull;

public interface AlunoConsultaGateway {

    boolean existsById(@NonNull Long id);
}
