package br.com.escola.aluno.adapter.in.web;

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

import br.com.escola.aluno.application.dto.AlunoInput;
import br.com.escola.aluno.application.dto.AlunoOutput;
import br.com.escola.aluno.application.usecase.AtualizarAlunoUseCase;
import br.com.escola.aluno.application.usecase.BuscarAlunoPorIdUseCase;
import br.com.escola.aluno.application.usecase.CriarAlunoUseCase;
import br.com.escola.aluno.application.usecase.ExcluirAlunoUseCase;
import br.com.escola.aluno.application.usecase.ListarAlunosUseCase;
import br.com.escola.responsavel.adapter.in.web.vinculo.ResponsavelVinculadoResponse;
import br.com.escola.responsavel.application.dto.ResponsavelVinculadoOutput;
import br.com.escola.responsavel.application.usecase.ListarResponsaveisPorAlunoUseCase;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/alunos")
public class AlunoController {

    private final CriarAlunoUseCase criarAlunoUseCase;
    private final BuscarAlunoPorIdUseCase buscarAlunoPorIdUseCase;
    private final ListarAlunosUseCase listarAlunosUseCase;
    private final AtualizarAlunoUseCase atualizarAlunoUseCase;
    private final ExcluirAlunoUseCase excluirAlunoUseCase;
    private final ListarResponsaveisPorAlunoUseCase listarResponsaveisPorAlunoUseCase;

    public AlunoController(
            CriarAlunoUseCase criarAlunoUseCase,
            BuscarAlunoPorIdUseCase buscarAlunoPorIdUseCase,
            ListarAlunosUseCase listarAlunosUseCase,
            AtualizarAlunoUseCase atualizarAlunoUseCase,
            ExcluirAlunoUseCase excluirAlunoUseCase,
            ListarResponsaveisPorAlunoUseCase listarResponsaveisPorAlunoUseCase) {
        this.criarAlunoUseCase = criarAlunoUseCase;
        this.buscarAlunoPorIdUseCase = buscarAlunoPorIdUseCase;
        this.listarAlunosUseCase = listarAlunosUseCase;
        this.atualizarAlunoUseCase = atualizarAlunoUseCase;
        this.excluirAlunoUseCase = excluirAlunoUseCase;
        this.listarResponsaveisPorAlunoUseCase = listarResponsaveisPorAlunoUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um aluno")
    public AlunoResponse criar(@Valid @RequestBody AlunoRequest request) {
        AlunoOutput output = criarAlunoUseCase.executar(toInput(request));
        return toResponse(output);
    }

    @GetMapping
    @Operation(summary = "Lista alunos")
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
    @Operation(summary = "Busca aluno por ID")
    public AlunoResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarAlunoPorIdUseCase.executar(id));
    }

    @GetMapping("/{id}/ficha")
    @Operation(summary = "Busca ficha consolidada do aluno")
    public AlunoFichaResponse buscarFicha(@PathVariable @NonNull UUID id) {
        AlunoResponse aluno = toResponse(buscarAlunoPorIdUseCase.executar(id));
        List<ResponsavelVinculadoResponse> responsaveis = listarResponsaveisPorAlunoUseCase.executar(id).stream()
                .map(this::toResponsavelVinculadoResponse)
                .toList();
        return new AlunoFichaResponse(aluno, responsaveis);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um aluno")
    public AlunoResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody AlunoRequest request) {
        AlunoOutput output = atualizarAlunoUseCase.executar(
                id,
                toInput(request));
        return toResponse(output);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um aluno")
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
                output.rg(),
                output.orgaoEmissorRg(),
                output.ufRg(),
                output.nacionalidade(),
                output.naturalidade(),
                output.sexo(),
                output.nomeSocial(),
                output.cep(),
                output.logradouro(),
                output.numero(),
                output.complemento(),
                output.bairro(),
                output.cidade(),
                output.uf(),
                output.statusAluno(),
                output.createdAt());
    }

    private ResponsavelVinculadoResponse toResponsavelVinculadoResponse(ResponsavelVinculadoOutput output) {
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

    private AlunoInput toInput(AlunoRequest request) {
        return new AlunoInput(
                request.nomeCompleto(),
                request.cpf(),
                request.email(),
                request.telefone(),
                request.dataNascimento(),
                request.rg(),
                request.orgaoEmissorRg(),
                request.ufRg(),
                request.nacionalidade(),
                request.naturalidade(),
                request.sexo(),
                request.nomeSocial(),
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.complemento(),
                request.bairro(),
                request.cidade(),
                request.uf(),
                request.statusAluno());
    }
}
