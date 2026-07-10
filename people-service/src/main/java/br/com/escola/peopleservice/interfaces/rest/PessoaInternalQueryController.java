package br.com.escola.peopleservice.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.peopleservice.application.context.InternalHeaders;
import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.port.in.PessoaQueryUseCase;

@RestController
@RequestMapping({ "/internal/v1", "/internal" })
public class PessoaInternalQueryController {

    private final PessoaQueryUseCase pessoaQueryUseCase;

    public PessoaInternalQueryController(PessoaQueryUseCase pessoaQueryUseCase) {
        this.pessoaQueryUseCase = pessoaQueryUseCase;
    }

    @GetMapping("/pessoas/catalogos/tipos-pessoa")
    public List<PessoaCatalogoResponse> listarTiposPessoa(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return pessoaQueryUseCase.listarTiposPessoa(authorization, context);
    }

    @GetMapping("/pessoas/catalogos/tipos-endereco")
    public List<PessoaCatalogoResponse> listarTiposEndereco(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context) {
        return pessoaQueryUseCase.listarTiposEndereco(authorization, context);
    }

    @GetMapping("/pessoas/{id}")
    public PessoaResumoResponse buscarPessoaPorId(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @PathVariable @NonNull UUID id) {
        return pessoaQueryUseCase.buscarPessoaPorId(authorization, context, id);
    }

    @GetMapping("/pessoas/consulta-cadastral")
    public PessoaConsultaCadastralPageResponse consultarCadastro(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorization,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestParam(name = "nomeAluno", required = false) String nomeAluno,
            @RequestParam(name = "cpfAluno", required = false) String cpfAluno,
            @RequestParam(name = "nomeResponsavel", required = false) String nomeResponsavel,
            @RequestParam(name = "cpfResponsavel", required = false) String cpfResponsavel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return pessoaQueryUseCase.consultarCadastro(
                authorization,
                context,
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
    }
}
