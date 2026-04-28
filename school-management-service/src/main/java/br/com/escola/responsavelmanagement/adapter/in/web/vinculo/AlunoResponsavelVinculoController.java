package br.com.escola.responsavelmanagement.adapter.in.web.vinculo;

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

import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.usecase.DesvincularResponsavelDoAlunoUseCase;
import br.com.escola.responsavelmanagement.application.usecase.ListarResponsaveisPorAlunoUseCase;
import br.com.escola.responsavelmanagement.application.usecase.VincularResponsavelAoAlunoUseCase;
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
    public void vincular(
            @PathVariable @NonNull UUID idAluno,
            @Valid @RequestBody VinculoResponsavelRequest request) {
        vincularResponsavelAoAlunoUseCase.executar(idAluno, request.idResponsavel());
    }


    @PostMapping("/{idResponsavel}")
    @ResponseStatus(HttpStatus.CREATED)
    public void vincularPorPath(
            @PathVariable @NonNull UUID idAluno,
            @PathVariable @NonNull UUID idResponsavel) {
        vincularResponsavelAoAlunoUseCase.executar(idAluno, idResponsavel);
    }

    @GetMapping
    public List<ResponsavelVinculadoResponse> listar(@PathVariable @NonNull UUID idAluno) {
        return listarResponsaveisPorAlunoUseCase.executar(idAluno).stream()
                .map(this::toResponse)
                .toList();
    }

    @DeleteMapping("/{idResponsavel}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desvincular(
            @PathVariable @NonNull UUID idAluno,
            @PathVariable @NonNull UUID idResponsavel) {
        desvincularResponsavelDoAlunoUseCase.executar(idAluno, idResponsavel);
    }

    private ResponsavelVinculadoResponse toResponse(ResponsavelOutput output) {
        return new ResponsavelVinculadoResponse(
                output.id(),
                output.nomeCompleto(),
                output.cpf(),
                output.email(),
                output.telefone(),
                output.createdAt());
    }
}
