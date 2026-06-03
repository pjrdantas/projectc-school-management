package br.com.escola.responsavel.adapter.in.web;

import java.util.List;
import java.util.UUID;

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

import br.com.escola.responsavel.application.dto.ResponsavelInput;
import br.com.escola.responsavel.application.dto.ResponsavelOutput;
import br.com.escola.responsavel.application.usecase.AtualizarResponsavelUseCase;
import br.com.escola.responsavel.application.usecase.BuscarResponsavelPorIdUseCase;
import br.com.escola.responsavel.application.usecase.CriarResponsavelUseCase;
import br.com.escola.responsavel.application.usecase.ExcluirResponsavelUseCase;
import br.com.escola.responsavel.application.usecase.ListarResponsaveisUseCase;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/responsaveis")
public class ResponsavelController {

    private final CriarResponsavelUseCase criarResponsavelUseCase;
    private final BuscarResponsavelPorIdUseCase buscarResponsavelPorIdUseCase;
    private final ListarResponsaveisUseCase listarResponsaveisUseCase;
    private final AtualizarResponsavelUseCase atualizarResponsavelUseCase;
    private final ExcluirResponsavelUseCase excluirResponsavelUseCase;

    public ResponsavelController(
            CriarResponsavelUseCase criarResponsavelUseCase,
            BuscarResponsavelPorIdUseCase buscarResponsavelPorIdUseCase,
            ListarResponsaveisUseCase listarResponsaveisUseCase,
            AtualizarResponsavelUseCase atualizarResponsavelUseCase,
            ExcluirResponsavelUseCase excluirResponsavelUseCase) {
        this.criarResponsavelUseCase = criarResponsavelUseCase;
        this.buscarResponsavelPorIdUseCase = buscarResponsavelPorIdUseCase;
        this.listarResponsaveisUseCase = listarResponsaveisUseCase;
        this.atualizarResponsavelUseCase = atualizarResponsavelUseCase;
        this.excluirResponsavelUseCase = excluirResponsavelUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um responsável")
    public ResponsavelResponse criar(@Valid @RequestBody ResponsavelRequest request) {
        ResponsavelOutput output = criarResponsavelUseCase.executar(toInput(request));
        return toResponse(output);
    }

    @GetMapping
    @Operation(summary = "Lista responsáveis")
    public List<ResponsavelResponse> listar(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String cpf) {
        List<ResponsavelOutput> responsaveis = listarResponsaveisUseCase.executar();

        if (nome != null && !nome.isBlank()) {
            String filtroNome = nome.trim().toLowerCase();
            responsaveis = responsaveis.stream()
                    .filter(r -> r.nomeCompleto().toLowerCase().contains(filtroNome))
                    .toList();
        }

        if (cpf != null && !cpf.isBlank()) {
            String filtroCpf = cpf.trim();
            responsaveis = responsaveis.stream()
                    .filter(r -> r.cpf().equals(filtroCpf))
                    .toList();
        }

        return responsaveis.stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca responsável por ID")
    public ResponsavelResponse buscarPorId(@PathVariable @NonNull UUID id) {
        return toResponse(buscarResponsavelPorIdUseCase.executar(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualiza um responsável")
    public ResponsavelResponse atualizar(@PathVariable @NonNull UUID id, @Valid @RequestBody ResponsavelRequest request) {
        ResponsavelOutput output = atualizarResponsavelUseCase.executar(id, toInput(request));
        return toResponse(output);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Exclui um responsável")
    public void excluir(@PathVariable @NonNull UUID id) {
        excluirResponsavelUseCase.executar(id);
    }

    private ResponsavelInput toInput(ResponsavelRequest request) {
        return new ResponsavelInput(
                request.nomeCompleto(),
                request.cpf(),
                request.email(),
                request.telefone(),
                request.rg(),
                request.cep(),
                request.logradouro(),
                request.numero(),
                request.complemento(),
                request.bairro(),
                request.cidade(),
                request.uf());
    }

    private ResponsavelResponse toResponse(ResponsavelOutput output) {
        return new ResponsavelResponse(
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
                output.createdAt());
    }
}
