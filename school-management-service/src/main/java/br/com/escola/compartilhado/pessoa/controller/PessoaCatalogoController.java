package br.com.escola.compartilhado.pessoa.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.compartilhado.pessoa.dto.CatalogoPessoaResponse;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaCatalogoResumo;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaConsultaPort;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/pessoas/catalogos")
public class PessoaCatalogoController {

    private final PessoaConsultaPort pessoaConsultaPort;

    public PessoaCatalogoController(PessoaConsultaPort pessoaConsultaPort) {
        this.pessoaConsultaPort = pessoaConsultaPort;
    }

    @GetMapping("/tipos-pessoa")
    @PreAuthorize("hasAnyAuthority('READ','READ_ALL','ADMIN')")
    @Operation(summary = "Lista tipos de pessoa")
    public List<CatalogoPessoaResponse> listarTiposPessoa() {
        return pessoaConsultaPort.listarTiposPessoa().stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/tipos-endereco")
    @PreAuthorize("hasAnyAuthority('READ','READ_ALL','ADMIN')")
    @Operation(summary = "Lista tipos de endereço")
    public List<CatalogoPessoaResponse> listarTiposEndereco() {
        return pessoaConsultaPort.listarTiposEndereco().stream()
                .map(this::toResponse)
                .toList();
    }

    private CatalogoPessoaResponse toResponse(PessoaCatalogoResumo resumo) {
        return new CatalogoPessoaResponse(resumo.id(), resumo.codigo(), resumo.descricao());
    }
}
