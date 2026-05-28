package br.com.escola.responsavelmanagement.adapter.in.web.consulta;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.responsavelmanagement.application.dto.consulta.ConsultaCadastralPageOutput;
import br.com.escola.responsavelmanagement.application.usecase.consulta.ConsultarCadastroAlunoResponsavelUseCase;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/consulta-cadastral")
public class ConsultaCadastralController {

    private final ConsultarCadastroAlunoResponsavelUseCase consultarCadastroAlunoResponsavelUseCase;

    public ConsultaCadastralController(ConsultarCadastroAlunoResponsavelUseCase consultarCadastroAlunoResponsavelUseCase) {
        this.consultarCadastroAlunoResponsavelUseCase = consultarCadastroAlunoResponsavelUseCase;
    }

    @GetMapping
    @Operation(summary = "Consulta cadastro de alunos e responsáveis")
    public ConsultaCadastralPageOutput consultar(
            @RequestParam(name = "nomeAluno", required = false) String nomeAluno,
            @RequestParam(name = "cpfAluno", required = false) String cpfAluno,
            @RequestParam(name = "nomeResponsavel", required = false) String nomeResponsavel,
            @RequestParam(name = "cpfResponsavel", required = false) String cpfResponsavel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        ConsultaCadastralPageOutput output = consultarCadastroAlunoResponsavelUseCase.executar(
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
        if (output.content() == null || output.content().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nenhum resultado encontrado para a consulta");
        }
        return output;
    }
}
