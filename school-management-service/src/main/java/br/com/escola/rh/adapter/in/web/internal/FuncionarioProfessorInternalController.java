package br.com.escola.rh.adapter.in.web.internal;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.professor.domain.exception.ProfessorNaoEncontradoException;
import br.com.escola.rh.adapter.in.web.dto.internal.FuncionarioProfessorInternalResponse;
import br.com.escola.rh.application.dto.internal.FuncionarioProfessorResumo;
import br.com.escola.rh.application.port.internal.FuncionarioProfessorPort;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/internal/funcionarios")
public class FuncionarioProfessorInternalController {

    private final FuncionarioProfessorPort funcionarioProfessorPort;

    public FuncionarioProfessorInternalController(
            @Qualifier("funcionarioProfessorService") FuncionarioProfessorPort funcionarioProfessorPort) {
        this.funcionarioProfessorPort = funcionarioProfessorPort;
    }

    @GetMapping("/{id}/professor")
    @Operation(summary = "Busca funcionario por id para uso interno no fluxo de professor")
    public FuncionarioProfessorInternalResponse buscarFuncionarioParaProfessor(
            @RequestHeader("X-Escola-Id") UUID escolaId,
            @PathVariable @NonNull UUID id) {
        return funcionarioProfessorPort.buscarFuncionarioParaProfessor(escolaId, id)
                .map(this::toResponse)
                .orElseThrow(() -> new ProfessorNaoEncontradoException("Funcionário não encontrado."));
    }

    @GetMapping("/professor-elegiveis")
    @Operation(summary = "Lista funcionarios elegiveis para cadastro de professor em uso interno")
    public List<FuncionarioProfessorInternalResponse> listarFuncionariosElegiveis(
            @RequestHeader("X-Escola-Id") UUID escolaId) {
        return funcionarioProfessorPort.listarFuncionariosElegiveisParaProfessor(escolaId).stream()
                .map(this::toResponse)
                .toList();
    }

    private FuncionarioProfessorInternalResponse toResponse(FuncionarioProfessorResumo resumo) {
        return new FuncionarioProfessorInternalResponse(
                resumo.funcionarioId(),
                resumo.pessoaId(),
                resumo.nomeCompleto(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.cargo(),
                resumo.ativo(),
                resumo.elegivelProfessor());
    }
}
