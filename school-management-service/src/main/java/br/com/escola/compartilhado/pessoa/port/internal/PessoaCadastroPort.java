package br.com.escola.compartilhado.pessoa.port.internal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.escola.compartilhado.pessoa.dto.CatalogoPessoaResponse;
import br.com.escola.compartilhado.pessoa.dto.EnderecoDados;
import br.com.escola.compartilhado.pessoa.dto.PessoaCriada;
import br.com.escola.compartilhado.pessoa.dto.PessoaDados;
import br.com.escola.compartilhado.pessoa.entity.PessoaEntity;

public interface PessoaCadastroPort {

    PessoaCriada criarPessoaComTipoEEndereco(
            PessoaDados pessoaDados,
            String tipoPessoaCodigo,
            EnderecoDados enderecoDados,
            UUID escolaId);

    void atualizarPessoaEEndereco(
            PessoaEntity pessoa,
            PessoaDados pessoaDados,
            EnderecoDados enderecoDados,
            UUID escolaId);

    Optional<PessoaEntity> buscarPorCpf(String cpf);

    List<CatalogoPessoaResponse> listarTiposPessoa();

    List<CatalogoPessoaResponse> listarTiposEndereco();
}
