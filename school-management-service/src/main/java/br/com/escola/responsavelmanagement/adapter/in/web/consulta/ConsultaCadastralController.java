package br.com.escola.responsavelmanagement.adapter.in.web.consulta;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.responsavelmanagement.application.dto.consulta.ConsultaCadastralPageOutput;
import br.com.escola.responsavelmanagement.application.usecase.consulta.ConsultarCadastroAlunoResponsavelUseCase;

@RestController
@RequestMapping("/api/consulta-cadastral")
public class ConsultaCadastralController {

    private final ConsultarCadastroAlunoResponsavelUseCase consultarCadastroAlunoResponsavelUseCase;

    public ConsultaCadastralController(ConsultarCadastroAlunoResponsavelUseCase consultarCadastroAlunoResponsavelUseCase) {
        this.consultarCadastroAlunoResponsavelUseCase = consultarCadastroAlunoResponsavelUseCase;
    }

    @GetMapping
    public ConsultaCadastralPageOutput consultar(
            @RequestParam(name = "nomeAluno", required = false) String nomeAluno,
            @RequestParam(name = "cpfAluno", required = false) String cpfAluno,
            @RequestParam(name = "nomeResponsavel", required = false) String nomeResponsavel,
            @RequestParam(name = "cpfResponsavel", required = false) String cpfResponsavel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return consultarCadastroAlunoResponsavelUseCase.executar(
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
    }
}
