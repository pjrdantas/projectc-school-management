package br.com.escola.transferencia.adapter.out.persistence;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.aluno.application.port.internal.AlunoMatriculaPort;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.transferencia.adapter.out.persistence.entity.TransferenciaAlunoEntity;
import br.com.escola.transferencia.adapter.out.persistence.repository.TransferenciaAlunoJpaRepository;
import br.com.escola.transferencia.application.dto.internal.EscolaOrigemResumo;
import br.com.escola.transferencia.application.dto.internal.TransferenciaAlunoResumo;
import br.com.escola.transferencia.application.port.out.TransferenciaAlunoGateway;
import br.com.escola.transferencia.domain.exception.TransferenciaAlunoInvalidaException;
import jakarta.persistence.EntityManager;

@Component
public class TransferenciaAlunoPersistenceGateway implements TransferenciaAlunoGateway {

    private final TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository;
    private final AlunoMatriculaPort alunoMatriculaPort;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;
    private final EscolaTenantService escolaTenantService;

    public TransferenciaAlunoPersistenceGateway(
            TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository,
            AlunoMatriculaPort alunoMatriculaPort,
            JdbcTemplate jdbcTemplate,
            EntityManager entityManager,
            EscolaTenantService escolaTenantService) {
        this.transferenciaAlunoJpaRepository = transferenciaAlunoJpaRepository;
        this.alunoMatriculaPort = alunoMatriculaPort;
        this.jdbcTemplate = jdbcTemplate;
        this.entityManager = entityManager;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsAlunoById(UUID alunoId) {
        return alunoMatriculaPort.existeAlunoPorIdEEscola(alunoId, escolaId());
    }

    @Override
    @Transactional
    public TransferenciaAlunoResumo save(
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
            String observacao) {
        TransferenciaAlunoEntity entity = new TransferenciaAlunoEntity();
        entity.setAluno(entityManager.getReference(AlunoEntity.class, alunoId));
        entity.setEscolaOrigem(entityManager.getReference(EscolaEntity.class, escolaOrigem.id()));
        entity.setSerieOrigem(serieOrigem);
        entity.setAnoLetivoOrigem(anoLetivoOrigem);
        entity.setDataTransferencia(dataTransferencia);
        entity.setMotivoTransferencia(motivoTransferencia);
        entity.setSituacaoOrigem(situacaoOrigem);
        entity.setDocumentosEntregues(documentosEntregues);
        entity.setTipoTransferencia(tipoTransferencia);
        entity.setStatusTransferencia(statusTransferencia);
        entity.setTipoTransferenciaId(buscarIdCatalogo("tipo_transferencia", "id_tipo_transferencia", tipoTransferencia));
        entity.setStatusTransferenciaId(buscarIdCatalogo("status_transferencia", "id_status_transferencia", statusTransferencia));
        entity.setUsuarioOperacao(usuarioOperacao);
        entity.setObservacao(observacao);
        entity.setDataHoraOperacao(LocalDateTime.now());
        return toResumo(transferenciaAlunoJpaRepository.save(entity));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TransferenciaAlunoResumo> findById(UUID id) {
        return transferenciaAlunoJpaRepository.findById(id).map(this::toResumo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransferenciaAlunoResumo> findByAlunoId(UUID alunoId) {
        return transferenciaAlunoJpaRepository.findByAluno_IdOrderByCreatedAtDesc(alunoId).stream()
                .map(this::toResumo)
                .toList();
    }

    private UUID buscarIdCatalogo(String tabela, String colunaId, String codigo) {
        List<UUID> ids = jdbcTemplate.query(
                "SELECT " + colunaId + " FROM " + tabela + " WHERE codigo = ?",
                (rs, rowNum) -> rs.getObject(colunaId, UUID.class),
                codigo);
        if (ids.isEmpty()) {
            throw new TransferenciaAlunoInvalidaException("Catálogo não cadastrado: " + tabela + "." + codigo);
        }
        return ids.getFirst();
    }

    private String resolverCodigoCatalogo(String tabela, String colunaId, UUID id, String valorTransient) {
        if (valorTransient != null && !valorTransient.isBlank()) {
            return valorTransient;
        }
        if (id == null) {
            return null;
        }
        List<String> codigos = jdbcTemplate.query(
                "SELECT codigo FROM " + tabela + " WHERE " + colunaId + " = ?",
                (rs, rowNum) -> rs.getString("codigo"),
                id);
        return codigos.isEmpty() ? null : codigos.getFirst();
    }

    private TransferenciaAlunoResumo toResumo(TransferenciaAlunoEntity entity) {
        return new TransferenciaAlunoResumo(
                entity.getId(),
                entity.getAluno().getId(),
                new EscolaOrigemResumo(
                        entity.getEscolaOrigem().getId(),
                        entity.getEscolaOrigem().getNomeEscola(),
                        entity.getEscolaOrigem().getCodigoInep(),
                        entity.getEscolaOrigem().getCnpj(),
                        entity.getEscolaOrigem().getCep(),
                        entity.getEscolaOrigem().getLogradouro(),
                        entity.getEscolaOrigem().getNumero(),
                        entity.getEscolaOrigem().getComplemento(),
                        entity.getEscolaOrigem().getBairro(),
                        entity.getEscolaOrigem().getCidade(),
                        entity.getEscolaOrigem().getUf(),
                        entity.getEscolaOrigem().getCreatedAt()),
                entity.getSerieOrigem(),
                entity.getAnoLetivoOrigem(),
                entity.getDataTransferencia(),
                entity.getMotivoTransferencia(),
                entity.getSituacaoOrigem(),
                entity.getDocumentosEntregues(),
                entity.getObservacao(),
                resolverCodigoCatalogo(
                        "tipo_transferencia",
                        "id_tipo_transferencia",
                        entity.getTipoTransferenciaId(),
                        entity.getTipoTransferencia()),
                resolverCodigoCatalogo(
                        "status_transferencia",
                        "id_status_transferencia",
                        entity.getStatusTransferenciaId(),
                        entity.getStatusTransferencia()),
                entity.getUsuarioOperacao(),
                entity.getDataHoraOperacao(),
                entity.getCreatedAt());
    }

    private UUID escolaId() {
        return escolaTenantService.obterOuCriarEscolaPadrao().getId();
    }
}
