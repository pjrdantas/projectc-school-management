package br.com.escola.transfermanagement.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.escola.shared.viacep.ViaCepResponse;
import br.com.escola.shared.viacep.ViaCepService;
import br.com.escola.studentmanagement.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.transfermanagement.adapter.in.web.dto.EscolaOrigemRequest;
import br.com.escola.transfermanagement.adapter.in.web.dto.EscolaOrigemResponse;
import br.com.escola.transfermanagement.adapter.in.web.dto.TransferenciaAlunoRequest;
import br.com.escola.transfermanagement.adapter.in.web.dto.TransferenciaAlunoResponse;
import br.com.escola.transfermanagement.adapter.out.persistence.entity.EscolaOrigemEntity;
import br.com.escola.transfermanagement.adapter.out.persistence.entity.TransferenciaAlunoEntity;
import br.com.escola.transfermanagement.adapter.out.persistence.repository.EscolaOrigemJpaRepository;
import br.com.escola.transfermanagement.adapter.out.persistence.repository.TransferenciaAlunoJpaRepository;
import br.com.escola.transfermanagement.domain.exception.EscolaOrigemNaoEncontradaException;
import br.com.escola.transfermanagement.domain.exception.TransferenciaAlunoInvalidaException;
import br.com.escola.transfermanagement.domain.exception.TransferenciaAlunoNaoEncontradaException;

@Service
public class TransferenciaAlunoService {

    private final TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository;
    private final EscolaOrigemJpaRepository escolaOrigemJpaRepository;
    private final AlunoJpaRepository alunoJpaRepository;
    private final ViaCepService viaCepService;
    private final JdbcTemplate jdbcTemplate;

    public TransferenciaAlunoService(
            TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository,
            EscolaOrigemJpaRepository escolaOrigemJpaRepository,
            AlunoJpaRepository alunoJpaRepository,
            ViaCepService viaCepService,
            JdbcTemplate jdbcTemplate) {
        this.transferenciaAlunoJpaRepository = transferenciaAlunoJpaRepository;
        this.escolaOrigemJpaRepository = escolaOrigemJpaRepository;
        this.alunoJpaRepository = alunoJpaRepository;
        this.viaCepService = viaCepService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public EscolaOrigemResponse criarEscolaOrigem(EscolaOrigemRequest request) {
        return toEscolaResponse(escolaOrigemJpaRepository.save(toEscolaEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<EscolaOrigemResponse> listarEscolasOrigem() {
        return escolaOrigemJpaRepository.findAll().stream().map(this::toEscolaResponse).toList();
    }

    @Transactional(readOnly = true)
    public EscolaOrigemResponse buscarEscolaOrigem(UUID id) {
        return escolaOrigemJpaRepository.findById(id)
                .map(this::toEscolaResponse)
                .orElseThrow(() -> new EscolaOrigemNaoEncontradaException(id));
    }

    @Transactional
    public TransferenciaAlunoResponse criarTransferencia(TransferenciaAlunoRequest request) {
        var aluno = alunoJpaRepository.findById(request.alunoId())
                .orElseThrow(() -> new TransferenciaAlunoInvalidaException("Aluno não encontrado: " + request.alunoId()));
        EscolaOrigemEntity escolaOrigem = resolverEscolaOrigem(request);

        TransferenciaAlunoEntity entity = new TransferenciaAlunoEntity();
        entity.setAluno(aluno);
        entity.setEscolaOrigem(escolaOrigem);
        entity.setSerieOrigem(request.serieOrigem());
        entity.setAnoLetivoOrigem(request.anoLetivoOrigem());
        entity.setDataTransferencia(request.dataTransferencia());
        entity.setMotivoTransferencia(request.motivoTransferencia());
        entity.setSituacaoOrigem(request.situacaoOrigem());
        entity.setDocumentosEntregues(request.documentosEntregues());
        String tipoTransferencia = normalizarTipoTransferencia(request.tipoTransferencia());
        String statusTransferencia = normalizarStatusTransferencia(request.statusTransferencia());
        entity.setTipoTransferencia(tipoTransferencia);
        entity.setStatusTransferencia(statusTransferencia);
        entity.setTipoTransferenciaId(buscarIdCatalogo("tipo_transferencia", "id_tipo_transferencia", tipoTransferencia));
        entity.setStatusTransferenciaId(buscarIdCatalogo("status_transferencia", "id_status_transferencia", statusTransferencia));
        entity.setUsuarioOperacao(request.usuarioOperacao());
        entity.setObservacao(request.observacao());

        return toTransferenciaResponse(transferenciaAlunoJpaRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public TransferenciaAlunoResponse buscarTransferencia(UUID id) {
        return transferenciaAlunoJpaRepository.findById(id)
                .map(this::toTransferenciaResponse)
                .orElseThrow(() -> new TransferenciaAlunoNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaAlunoResponse> listarPorAluno(UUID alunoId) {
        if (!alunoJpaRepository.existsById(alunoId)) {
            throw new TransferenciaAlunoInvalidaException("Aluno não encontrado: " + alunoId);
        }
        return transferenciaAlunoJpaRepository.findByAluno_IdOrderByCreatedAtDesc(alunoId).stream()
                .map(this::toTransferenciaResponse)
                .toList();
    }

    private EscolaOrigemEntity resolverEscolaOrigem(TransferenciaAlunoRequest request) {
        if (request.escolaOrigemId() != null) {
            return escolaOrigemJpaRepository.findById(request.escolaOrigemId())
                    .orElseThrow(() -> new EscolaOrigemNaoEncontradaException(request.escolaOrigemId()));
        }
        if (request.escolaOrigem() == null) {
            throw new TransferenciaAlunoInvalidaException("escolaOrigemId ou escolaOrigem é obrigatório");
        }
        return escolaOrigemJpaRepository.save(toEscolaEntity(request.escolaOrigem()));
    }

    private EscolaOrigemEntity toEscolaEntity(EscolaOrigemRequest request) {
        EscolaOrigemEntity entity = new EscolaOrigemEntity();
        entity.setNomeEscola(request.nomeEscola());
        entity.setCodigoInep(request.codigoInep());
        entity.setCnpj(request.cnpj());
        entity.setCep(request.cep());
        entity.setLogradouro(request.logradouro());
        entity.setNumero(request.numero());
        entity.setComplemento(request.complemento());
        entity.setBairro(request.bairro());
        entity.setCidade(request.cidade());
        entity.setUf(request.uf());
        preencherEnderecoComViaCep(entity, request);
        return entity;
    }

    private void preencherEnderecoComViaCep(EscolaOrigemEntity entity, EscolaOrigemRequest request) {
        ViaCepResponse endereco = viaCepService.consultar(request.cep());
        if (endereco == null) {
            return;
        }
        if (request.numero() == null || request.numero().isBlank()) {
            throw new TransferenciaAlunoInvalidaException("numero é obrigatório quando cep é informado");
        }
        entity.setCep(viaCepService.normalizar(request.cep()));
        entity.setLogradouro(endereco.logradouro());
        entity.setBairro(endereco.bairro());
        entity.setCidade(endereco.localidade());
        entity.setUf(endereco.uf());
        entity.setNumero(request.numero());
        entity.setComplemento(request.complemento());
    }

    private TransferenciaAlunoResponse toTransferenciaResponse(TransferenciaAlunoEntity entity) {
        return new TransferenciaAlunoResponse(
                entity.getId(),
                entity.getAluno().getId(),
                toEscolaResponse(entity.getEscolaOrigem()),
                entity.getSerieOrigem(),
                entity.getAnoLetivoOrigem(),
                entity.getDataTransferencia(),
                entity.getMotivoTransferencia(),
                entity.getSituacaoOrigem(),
                entity.getDocumentosEntregues(),
                entity.getObservacao(),
                resolverCodigoCatalogo("tipo_transferencia", "id_tipo_transferencia", entity.getTipoTransferenciaId(), entity.getTipoTransferencia()),
                resolverCodigoCatalogo("status_transferencia", "id_status_transferencia", entity.getStatusTransferenciaId(), entity.getStatusTransferencia()),
                entity.getUsuarioOperacao(),
                entity.getDataHoraOperacao(),
                entity.getCreatedAt());
    }

    private String normalizarTipoTransferencia(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            return "ENTRADA";
        }
        String normalizado = tipo.trim().toUpperCase();
        if (!normalizado.equals("ENTRADA") && !normalizado.equals("SAIDA")) {
            throw new TransferenciaAlunoInvalidaException("Tipo de transferência inválido: " + tipo);
        }
        return normalizado;
    }

    private String normalizarStatusTransferencia(String status) {
        if (status == null || status.isBlank()) {
            return "EM_ANDAMENTO";
        }
        String normalizado = status.trim().toUpperCase();
        if (!normalizado.equals("EM_ANDAMENTO")
                && !normalizado.equals("CONFIRMADA")
                && !normalizado.equals("CANCELADA")) {
            throw new TransferenciaAlunoInvalidaException("Status de transferência inválido: " + status);
        }
        return normalizado;
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

    private EscolaOrigemResponse toEscolaResponse(EscolaOrigemEntity entity) {
        return new EscolaOrigemResponse(
                entity.getId(),
                entity.getNomeEscola(),
                entity.getCodigoInep(),
                entity.getCnpj(),
                entity.getCep(),
                entity.getLogradouro(),
                entity.getNumero(),
                entity.getComplemento(),
                entity.getBairro(),
                entity.getCidade(),
                entity.getUf(),
                entity.getCreatedAt());
    }
}
