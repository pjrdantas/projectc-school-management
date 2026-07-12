package br.com.escola.transferencia.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.transferencia.application.dto.internal.EscolaOrigemResumo;
import br.com.escola.transferencia.application.dto.internal.TransferenciaAlunoResumo;

public interface TransferenciaAlunoGateway {

    boolean existsAlunoById(UUID alunoId);

    TransferenciaAlunoResumo save(
            UUID alunoId,
            EscolaOrigemResumo escolaOrigem,
            String serieOrigem,
            String anoLetivoOrigem,
            LocalDate dataTransferencia,
            String motivoTransferencia,
            String situacaoOrigem,
            String documentosEntregues,
            String tipoTransferencia,
            String statusTransferencia,
            String usuarioOperacao,
            String observacao);

    Optional<TransferenciaAlunoResumo> findById(UUID id);

    List<TransferenciaAlunoResumo> findByAlunoId(UUID alunoId);
}
