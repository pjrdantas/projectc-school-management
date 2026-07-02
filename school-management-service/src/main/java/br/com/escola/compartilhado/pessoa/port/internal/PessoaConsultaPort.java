package br.com.escola.compartilhado.pessoa.port.internal;

import java.util.List;

import br.com.escola.compartilhado.pessoa.dto.internal.PessoaCatalogoResumo;
import br.com.escola.compartilhado.pessoa.dto.internal.PessoaConsultaCadastralPage;

public interface PessoaConsultaPort {

    PessoaConsultaCadastralPage consultarCadastroAlunoResponsavel(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);

    List<PessoaCatalogoResumo> listarTiposPessoa();

    List<PessoaCatalogoResumo> listarTiposEndereco();
}
