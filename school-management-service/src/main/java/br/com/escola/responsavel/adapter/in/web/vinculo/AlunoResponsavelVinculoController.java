package br.com.escola.responsavel.adapter.in.web.vinculo;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.responsavel.application.dto.ResponsavelVinculadoOutput;
import br.com.escola.responsavel.application.dto.VinculoAlunoResponsavelInput;
import br.com.escola.responsavel.application.usecase.DesvincularResponsavelDoAlunoUseCase;
import br.com.escola.responsavel.application.usecase.ListarResponsaveisPorAlunoUseCase;
import br.com.escola.responsavel.application.usecase.VincularResponsavelAoAlunoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/alunos/{idAluno}/responsaveis")
public class AlunoResponsavelVinculoController {

    private final VincularResponsavelAoAlunoUseCase vincularResponsavelAoAlunoUseCase;
    private final DesvincularResponsavelDoAlunoUseCase desvincularResponsavelDoAlunoUseCase;
    private final ListarResponsaveisPorAlunoUseCase listarResponsaveisPorAlunoUseCase;

    public AlunoResponsavelVinculoController(
            VincularResponsavelAoAlunoUseCase vincularResponsavelAoAlunoUseCase,
            DesvincularResponsavelDoAlunoUseCase desvincularResponsavelDoAlunoUseCase,
            ListarResponsaveisPorAlunoUseCase listarResponsaveisPorAlunoUseCase) {
        this.vincularResponsavelAoAlunoUseCase = vincularResponsavelAoAlunoUseCase;
        this.desvincularResponsavelDoAlunoUseCase = desvincularResponsavelDoAlunoUseCase;
        this.listarResponsaveisPorAlunoUseCase = listarResponsaveisPorAlunoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Vincula responsável ao aluno")
    public void vincular(
            @PathVariable @NonNull UUID idAluno,
            @Valid @RequestBody VinculoResponsavelRequest request) {
        vincularResponsavelAoAlunoUseCase.executar(toInput(idAluno, request));
    }


    @PostMapping("/{idResponsavel}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Vincula responsável ao aluno por ID")
    public void vincularPorPath(
            @PathVariable @NonNull UUID idAluno,
            @PathVariable @NonNull UUID idResponsavel) {
        vincularResponsavelAoAlunoUseCase.executar(idAluno, idResponsavel);
    }

    @GetMapping
    @Operation(summary = "Lista responsáveis vinculados ao aluno")
    public List<ResponsavelVinculadoResponse> listar(@PathVariable @NonNull UUID idAluno) {
        return listarResponsaveisPorAlunoUseCase.executar(idAluno).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{idResponsavel}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Desvincula responsável do aluno")
    public void desvincular(
            @PathVariable @NonNull UUID idAluno,
            @PathVariable @NonNull UUID idResponsavel) {
        desvincularResponsavelDoAlunoUseCase.executar(idAluno, idResponsavel);
    }

    private ResponsavelVinculadoResponse toResponse(ResponsavelVinculadoOutput output) {
        return new ResponsavelVinculadoResponse(
                output.id(),
                output.nomeCompleto(),
                output.cpf(),
                output.email(),
                output.telefone(),
                output.rg(),
                output.cep(),
                output.logradouro(),
                output.numero(),
                output.complemento(),
                output.bairro(),
                output.cidade(),
                output.uf(),
                output.parentesco(),
                output.responsavelFinanceiro(),
                output.responsavelPedagogico(),
                output.autorizadoRetirar(),
                output.createdAt());
    }

    private VinculoAlunoResponsavelInput toInput(UUID idAluno, VinculoResponsavelRequest request) {
        return new VinculoAlunoResponsavelInput(
                idAluno,
                request.idResponsavel(),
                request.parentesco(),
                request.responsavelFinanceiro(),
                request.responsavelPedagogico(),
                request.autorizadoRetirar());
    }
}
