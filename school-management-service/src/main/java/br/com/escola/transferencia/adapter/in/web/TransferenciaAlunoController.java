package br.com.escola.transferencia.adapter.in.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.transferencia.adapter.in.web.dto.TransferenciaAlunoRequest;
import br.com.escola.transferencia.adapter.in.web.dto.TransferenciaAlunoResponse;
import br.com.escola.transferencia.application.dto.internal.CriarTransferenciaAlunoSolicitacao;
import br.com.escola.transferencia.application.dto.internal.EscolaOrigemSolicitacao;
import br.com.escola.transferencia.application.dto.internal.TransferenciaAlunoResumo;
import br.com.escola.transferencia.application.service.TransferenciaAlunoService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transferencias")
public class TransferenciaAlunoController {

    private final TransferenciaAlunoService transferenciaAlunoService;

    public TransferenciaAlunoController(TransferenciaAlunoService transferenciaAlunoService) {
        this.transferenciaAlunoService = transferenciaAlunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria transferência de aluno")
    public TransferenciaAlunoResponse criar(@Valid @RequestBody TransferenciaAlunoRequest request) {
        return toResponse(transferenciaAlunoService.criarTransferencia(new CriarTransferenciaAlunoSolicitacao(
                request.alunoId(),
                request.escolaOrigemId(),
                toSolicitacao(request),
                request.serieOrigem(),
                request.anoLetivoOrigem(),
                request.dataTransferencia(),
                request.motivoTransferencia(),
                request.situacaoOrigem(),
                request.documentosEntregues(),
                request.tipoTransferencia(),
                request.statusTransferencia(),
                request.usuarioOperacao(),
                request.observacao())));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca transferência por ID")
    public TransferenciaAlunoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(transferenciaAlunoService.buscarTransferencia(id));
    }

    @GetMapping("/alunos/{alunoId}")
    @Operation(summary = "Lista transferências de um aluno")
    public List<TransferenciaAlunoResponse> listarPorAluno(@PathVariable @NonNull UUID alunoId) {
        return transferenciaAlunoService.listarPorAluno(alunoId).stream().map(this::toResponse).toList();
    }

    private TransferenciaAlunoResponse toResponse(TransferenciaAlunoResumo resumo) {
        return new TransferenciaAlunoResponse(
                resumo.id(),
                resumo.alunoId(),
                new br.com.escola.transferencia.adapter.in.web.dto.EscolaOrigemResponse(
                        resumo.escolaOrigem().id(),
                        resumo.escolaOrigem().nomeEscola(),
                        resumo.escolaOrigem().codigoInep(),
                        resumo.escolaOrigem().cnpj(),
                        resumo.escolaOrigem().cep(),
                        resumo.escolaOrigem().logradouro(),
                        resumo.escolaOrigem().numero(),
                        resumo.escolaOrigem().complemento(),
                        resumo.escolaOrigem().bairro(),
                        resumo.escolaOrigem().cidade(),
                        resumo.escolaOrigem().uf(),
                        resumo.escolaOrigem().createdAt()),
                resumo.serieOrigem(),
                resumo.anoLetivoOrigem(),
                resumo.dataTransferencia(),
                resumo.motivoTransferencia(),
                resumo.situacaoOrigem(),
                resumo.documentosEntregues(),
                resumo.observacao(),
                resumo.tipoTransferencia(),
                resumo.statusTransferencia(),
                resumo.usuarioOperacao(),
                resumo.dataHoraOperacao(),
                resumo.createdAt());
    }

    private EscolaOrigemSolicitacao toSolicitacao(TransferenciaAlunoRequest request) {
        if (request.escolaOrigem() == null) {
            return null;
        }
        return new EscolaOrigemSolicitacao(
                request.escolaOrigem().nomeEscola(),
                request.escolaOrigem().codigoInep(),
                request.escolaOrigem().cnpj(),
                request.escolaOrigem().cep(),
                request.escolaOrigem().logradouro(),
                request.escolaOrigem().numero(),
                request.escolaOrigem().complemento(),
                request.escolaOrigem().bairro(),
                request.escolaOrigem().cidade(),
                request.escolaOrigem().uf());
    }
}
