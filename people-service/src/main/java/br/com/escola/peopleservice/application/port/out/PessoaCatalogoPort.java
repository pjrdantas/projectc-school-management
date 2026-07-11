package br.com.escola.peopleservice.application.port.out;

import java.util.List;

import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;

public interface PessoaCatalogoPort {

    List<PessoaCatalogoResponse> listarTiposPessoa();

    List<PessoaCatalogoResponse> listarTiposEndereco();
}
