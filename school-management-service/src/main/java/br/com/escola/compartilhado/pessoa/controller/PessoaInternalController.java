package br.com.escola.compartilhado.pessoa.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaAlunoResponsaveisInternalResponse;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaAlunoResponsaveisResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaCatalogoInternalResponse;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaCatalogoResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaConsultaCadastralPage;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaConsultaCadastralPageInternalResponse;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResponsavelResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResponsavelResumoInternalResponse;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaResumoInternalResponse;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaConsultaPort;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/internal/pessoas")
public class PessoaInternalController {

    private static final String ESCOLA_HEADER = "X-Escola-Id";

    private final PessoaConsultaPort pessoaConsultaPort;

    public PessoaInternalController(PessoaConsultaPort pessoaConsultaPort) {
        this.pessoaConsultaPort = pessoaConsultaPort;
    }

    @GetMapping("/catalogos/tipos-pessoa")
    @Operation(summary = "Lista tipos de pessoa para uso interno entre backends")
    public List<PessoaCatalogoInternalResponse> listarTiposPessoa() {
        return pessoaConsultaPort.listarTiposPessoa().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/catalogos/tipos-endereco")
    @Operation(summary = "Lista tipos de endereco para uso interno entre backends")
    public List<PessoaCatalogoInternalResponse> listarTiposEndereco() {
        return pessoaConsultaPort.listarTiposEndereco().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca pessoa por id e escola para uso interno entre backends")
    public PessoaResumoInternalResponse buscarPessoaPorId(
            @RequestHeader(ESCOLA_HEADER) UUID escolaId,
            @PathVariable @NonNull UUID id) {
        return pessoaConsultaPort.buscarPessoaPorIdEEscola(id, escolaId)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pessoa nao encontrada."));
    }

    @GetMapping("/consulta-cadastral")
    @Operation(summary = "Consulta cadastro de alunos e responsaveis para uso interno entre backends")
    public PessoaConsultaCadastralPageInternalResponse consultarCadastro(
            @RequestParam(name = "nomeAluno", required = false) String nomeAluno,
            @RequestParam(name = "cpfAluno", required = false) String cpfAluno,
            @RequestParam(name = "nomeResponsavel", required = false) String nomeResponsavel,
            @RequestParam(name = "cpfResponsavel", required = false) String cpfResponsavel,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        return toResponse(pessoaConsultaPort.consultarCadastroAlunoResponsavel(
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size));
    }

    private PessoaCatalogoInternalResponse toResponse(PessoaCatalogoResumo resumo) {
        return new PessoaCatalogoInternalResponse(resumo.id(), resumo.codigo(), resumo.descricao());
    }

    private PessoaResumoInternalResponse toResponse(PessoaResumo resumo) {
        return new PessoaResumoInternalResponse(
                resumo.id(),
                resumo.nomeCompleto(),
                resumo.escolaId(),
                resumo.escolaNome(),
                resumo.ativo());
    }

    private PessoaConsultaCadastralPageInternalResponse toResponse(PessoaConsultaCadastralPage page) {
        return new PessoaConsultaCadastralPageInternalResponse(
                page.content().stream().map(this::toResponse).toList(),
                page.totalElements(),
                page.page(),
                page.size());
    }

    private PessoaAlunoResponsaveisInternalResponse toResponse(PessoaAlunoResponsaveisResumo resumo) {
        return new PessoaAlunoResponsaveisInternalResponse(
                resumo.idAluno(),
                resumo.nomeCompleto(),
                resumo.cpf(),
                resumo.email(),
                resumo.telefone(),
                resumo.dataNascimento(),
                resumo.createdAt(),
                resumo.responsaveis().stream().map(this::toResponse).toList());
    }

    private PessoaResponsavelResumoInternalResponse toResponse(PessoaResponsavelResumo resumo) {
        return new PessoaResponsavelResumoInternalResponse(
                resumo.id(),
                resumo.nomeCompleto(),
                resumo.cpf(),
                resumo.email(),
                resumo.telefone(),
                resumo.createdAt());
    }
}
