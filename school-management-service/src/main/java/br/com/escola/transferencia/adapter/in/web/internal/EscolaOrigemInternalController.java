package br.com.escola.transferencia.adapter.in.web.internal;

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

import br.com.escola.transferencia.adapter.in.web.dto.EscolaOrigemRequest;
import br.com.escola.transferencia.adapter.in.web.dto.EscolaOrigemResponse;
import br.com.escola.transferencia.application.dto.internal.EscolaOrigemResumo;
import br.com.escola.transferencia.application.dto.internal.EscolaOrigemSolicitacao;
import br.com.escola.transferencia.application.service.TransferenciaAlunoService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/internal/escolas-origem")
public class EscolaOrigemInternalController {

    private final TransferenciaAlunoService transferenciaAlunoService;

    public EscolaOrigemInternalController(TransferenciaAlunoService transferenciaAlunoService) {
        this.transferenciaAlunoService = transferenciaAlunoService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EscolaOrigemResponse criar(@Valid @RequestBody EscolaOrigemRequest request) {
        return toResponse(transferenciaAlunoService.criarEscolaOrigem(new EscolaOrigemSolicitacao(
                request.nomeEscola(),
                request.codigoInep(),
                request.cnpj(),
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.complemento(),
                request.bairro(),
                request.cidade(),
                request.uf())));
    }

    @GetMapping
    public List<EscolaOrigemResponse> listar() {
        return transferenciaAlunoService.listarEscolasOrigem().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public EscolaOrigemResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(transferenciaAlunoService.buscarEscolaOrigem(id));
    }

    private EscolaOrigemResponse toResponse(EscolaOrigemResumo resumo) {
        return new EscolaOrigemResponse(
                resumo.id(),
                resumo.nomeEscola(),
                resumo.codigoInep(),
                resumo.cnpj(),
                resumo.cep(),
                resumo.logradouro(),
                resumo.numero(),
                resumo.complemento(),
                resumo.bairro(),
                resumo.cidade(),
                resumo.uf(),
                resumo.createdAt());
    }
}
