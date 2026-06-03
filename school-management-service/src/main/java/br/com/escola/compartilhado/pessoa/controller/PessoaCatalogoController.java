package br.com.escola.compartilhado.pessoa.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.compartilhado.pessoa.dto.CatalogoPessoaResponse;
import br.com.escola.compartilhado.pessoa.service.PessoaFoundationService;
import io.swagger.v3.oas.annotations.Operation;

@RestController
@RequestMapping("/api/pessoas/catalogos")
public class PessoaCatalogoController {

    private final PessoaFoundationService pessoaFoundationService;

    public PessoaCatalogoController(PessoaFoundationService pessoaFoundationService) {
        this.pessoaFoundationService = pessoaFoundationService;
    }

    @GetMapping("/tipos-pessoa")
    @PreAuthorize("hasAnyAuthority('READ','READ_ALL','ADMIN')")
    @Operation(summary = "Lista tipos de pessoa")
    public List<CatalogoPessoaResponse> listarTiposPessoa() {
        return pessoaFoundationService.listarTiposPessoa();
    }

    @GetMapping("/tipos-endereco")
    @PreAuthorize("hasAnyAuthority('READ','READ_ALL','ADMIN')")
    @Operation(summary = "Lista tipos de endereço")
    public List<CatalogoPessoaResponse> listarTiposEndereco() {
        return pessoaFoundationService.listarTiposEndereco();
    }
}
