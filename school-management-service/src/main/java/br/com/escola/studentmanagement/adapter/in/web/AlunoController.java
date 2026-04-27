package br.com.escola.studentmanagement.adapter.in.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.usecase.BuscarAlunoPorIdUseCase;
import br.com.escola.studentmanagement.application.usecase.CriarAlunoUseCase;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final CriarAlunoUseCase criarAlunoUseCase;
    private final BuscarAlunoPorIdUseCase buscarAlunoPorIdUseCase;

    public AlunoController(CriarAlunoUseCase criarAlunoUseCase, BuscarAlunoPorIdUseCase buscarAlunoPorIdUseCase) {
        this.criarAlunoUseCase = criarAlunoUseCase;
        this.buscarAlunoPorIdUseCase = buscarAlunoPorIdUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse criar(@Valid @RequestBody AlunoRequest request) {
        AlunoOutput output = criarAlunoUseCase.executar(
                new AlunoInput(request.nomeCompleto(), request.cpf(), request.email(), request.dataNascimento()));
        return toResponse(output);
    }

    @GetMapping("/{id}")
    public AlunoResponse buscarPorId(@PathVariable @NonNull Long id) {
        return toResponse(buscarAlunoPorIdUseCase.executar(id));
    }

    private AlunoResponse toResponse(AlunoOutput output) {
        return new AlunoResponse(
                output.id(),
                output.nomeCompleto(),
                output.cpf(),
                output.email(),
                output.dataNascimento(),
                output.createdAt());
    }
}
