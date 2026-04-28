package br.com.escola.studentmanagement.adapter.in.web;

import java.util.UUID;

import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.studentmanagement.application.dto.AlunoInput;
import br.com.escola.studentmanagement.application.dto.AlunoOutput;
import br.com.escola.studentmanagement.application.usecase.AtualizarAlunoUseCase;
import br.com.escola.studentmanagement.application.usecase.BuscarAlunoPorIdUseCase;
import br.com.escola.studentmanagement.application.usecase.CriarAlunoUseCase;
import br.com.escola.studentmanagement.application.usecase.ExcluirAlunoUseCase;
import br.com.escola.studentmanagement.application.usecase.ListarAlunosUseCase;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final CriarAlunoUseCase criarAlunoUseCase;
    private final BuscarAlunoPorIdUseCase buscarAlunoPorIdUseCase;
    private final ListarAlunosUseCase listarAlunosUseCase;
    private final AtualizarAlunoUseCase atualizarAlunoUseCase;
    private final ExcluirAlunoUseCase excluirAlunoUseCase;

    public AlunoController(
            CriarAlunoUseCase criarAlunoUseCase,
            BuscarAlunoPorIdUseCase buscarAlunoPorIdUseCase,
            ListarAlunosUseCase listarAlunosUseCase,
            AtualizarAlunoUseCase atualizarAlunoUseCase,
            ExcluirAlunoUseCase excluirAlunoUseCase) {
        this.criarAlunoUseCase = criarAlunoUseCase;
        this.buscarAlunoPorIdUseCase = buscarAlunoPorIdUseCase;
        this.listarAlunosUseCase = listarAlunosUseCase;
        this.atualizarAlunoUseCase = atualizarAlunoUseCase;
        this.excluirAlunoUseCase = excluirAlunoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AlunoResponse criar(@Valid @RequestBody AlunoRequest request) {
        AlunoOutput output = criarAlunoUseCase.executar(
                new AlunoInput(request.nomeCompleto(), request.cpf(), request.email(), request.telefone(), request.dataNascimento()));
        return toResponse(output);
    }

    @GetMapping
    public List<AlunoResponse> listar(@RequestParam(required = false) String nome) {
        List<AlunoOutput> alunos = listarAlunosUseCase.executar();

        if (nome != null && !nome.isBlank()) {
            String filtro = nome.trim().toLowerCase();
            alunos = alunos.stream()
                    .filter(aluno -> aluno.nomeCompleto().toLowerCase().contains(filtro))
                    .toList();
        }

        return alunos.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public AlunoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarAlunoPorIdUseCase.executar(id));
    }

    @PutMapping("/{id}")
    public AlunoResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody AlunoRequest request) {
        AlunoOutput output = atualizarAlunoUseCase.executar(
                id,
                new AlunoInput(request.nomeCompleto(), request.cpf(), request.email(), request.telefone(), request.dataNascimento()));
        return toResponse(output);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable @NonNull UUID id) {
        excluirAlunoUseCase.executar(id);
    }

    private AlunoResponse toResponse(AlunoOutput output) {
        return new AlunoResponse(
                output.id(),
                output.nomeCompleto(),
                output.cpf(),
                output.email(),
                output.telefone(),
                output.dataNascimento(),
                output.createdAt());
    }
}
