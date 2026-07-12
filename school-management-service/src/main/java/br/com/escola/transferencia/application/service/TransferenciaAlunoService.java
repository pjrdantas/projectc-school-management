package br.com.escola.transferencia.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.compartilhado.viacep.ViaCepResponse;
import br.com.escola.compartilhado.viacep.ViaCepService;
import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.transferencia.adapter.out.persistence.repository.EscolaOrigemJpaRepository;
import br.com.escola.transferencia.application.dto.internal.CriarTransferenciaAlunoSolicitacao;
import br.com.escola.transferencia.application.dto.internal.EscolaOrigemResumo;
import br.com.escola.transferencia.application.dto.internal.EscolaOrigemSolicitacao;
import br.com.escola.transferencia.application.dto.internal.TransferenciaAlunoResumo;
import br.com.escola.transferencia.application.port.out.TransferenciaAlunoGateway;
import br.com.escola.transferencia.domain.exception.EscolaOrigemNaoEncontradaException;
import br.com.escola.transferencia.domain.exception.TransferenciaAlunoInvalidaException;
import br.com.escola.transferencia.domain.exception.TransferenciaAlunoNaoEncontradaException;

@Service
public class TransferenciaAlunoService {

    private final EscolaOrigemJpaRepository escolaOrigemJpaRepository;
    private final TransferenciaAlunoGateway transferenciaAlunoGateway;
    private final ViaCepService viaCepService;

    public TransferenciaAlunoService(
            EscolaOrigemJpaRepository escolaOrigemJpaRepository,
            TransferenciaAlunoGateway transferenciaAlunoGateway,
            ViaCepService viaCepService) {
        this.escolaOrigemJpaRepository = escolaOrigemJpaRepository;
        this.transferenciaAlunoGateway = transferenciaAlunoGateway;
        this.viaCepService = viaCepService;
    }

    @Transactional
    public EscolaOrigemResumo criarEscolaOrigem(EscolaOrigemSolicitacao request) {
        return toEscolaResumo(escolaOrigemJpaRepository.save(toEscolaEntity(request)));
    }

    @Transactional(readOnly = true)
    public List<EscolaOrigemResumo> listarEscolasOrigem() {
        return escolaOrigemJpaRepository.findAll().stream().map(this::toEscolaResumo).toList();
    }

    @Transactional(readOnly = true)
    public EscolaOrigemResumo buscarEscolaOrigem(UUID id) {
        return escolaOrigemJpaRepository.findById(id)
                .map(this::toEscolaResumo)
                .orElseThrow(() -> new EscolaOrigemNaoEncontradaException(id));
    }

    @Transactional
    public TransferenciaAlunoResumo criarTransferencia(CriarTransferenciaAlunoSolicitacao request) {
        if (!transferenciaAlunoGateway.existsAlunoById(request.alunoId())) {
            throw new TransferenciaAlunoInvalidaException("Aluno não encontrado: " + request.alunoId());
        }
        EscolaEntity escolaOrigem = resolverEscolaOrigem(request);
        String tipoTransferencia = normalizarTipoTransferencia(request.tipoTransferencia());
        String statusTransferencia = normalizarStatusTransferencia(request.statusTransferencia());
        return transferenciaAlunoGateway.save(
                request.alunoId(),
                toEscolaResumo(escolaOrigem),
                request.serieOrigem(),
                request.anoLetivoOrigem(),
                request.dataTransferencia(),
                request.motivoTransferencia(),
                request.situacaoOrigem(),
                request.documentosEntregues(),
                tipoTransferencia,
                statusTransferencia,
                request.usuarioOperacao(),
                request.observacao());
    }

    @Transactional(readOnly = true)
    public TransferenciaAlunoResumo buscarTransferencia(UUID id) {
        return transferenciaAlunoGateway.findById(id)
                .orElseThrow(() -> new TransferenciaAlunoNaoEncontradaException(id));
    }

    @Transactional(readOnly = true)
    public List<TransferenciaAlunoResumo> listarPorAluno(UUID alunoId) {
        if (!transferenciaAlunoGateway.existsAlunoById(alunoId)) {
            throw new TransferenciaAlunoInvalidaException("Aluno não encontrado: " + alunoId);
        }
        return transferenciaAlunoGateway.findByAlunoId(alunoId);
    }

    private EscolaEntity resolverEscolaOrigem(CriarTransferenciaAlunoSolicitacao request) {
        if (request.escolaOrigemId() != null) {
            return escolaOrigemJpaRepository.findById(request.escolaOrigemId())
                    .orElseThrow(() -> new EscolaOrigemNaoEncontradaException(request.escolaOrigemId()));
        }
        if (request.escolaOrigem() == null) {
            throw new TransferenciaAlunoInvalidaException("escolaOrigemId ou escolaOrigem é obrigatório");
        }
        return escolaOrigemJpaRepository.save(toEscolaEntity(request.escolaOrigem()));
    }

    private EscolaEntity toEscolaEntity(EscolaOrigemSolicitacao request) {
        EscolaEntity entity = new EscolaEntity();
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

    private void preencherEnderecoComViaCep(EscolaEntity entity, EscolaOrigemSolicitacao request) {
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

    private EscolaOrigemResumo toEscolaResumo(EscolaEntity entity) {
        return new EscolaOrigemResumo(
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
