package br.com.escola.peopleservice.application.port.in;

import java.util.List;
import java.util.UUID;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaContatoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaDocumentoMetadataResponse;
import br.com.escola.peopleservice.application.dto.PessoaEnderecoResponse;
import br.com.escola.peopleservice.application.dto.PessoaFuncionarioResumoResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;

public interface PessoaQueryUseCase {

    List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context);

    List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context);

    List<PessoaCatalogoResponse> listarStatusAluno(String authorization, InternalRequestContext context);

    List<PessoaCatalogoResponse> listarParentescos(String authorization, InternalRequestContext context);

    PessoaEnderecoResponse buscarEnderecoPrincipalPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId);

    List<PessoaEnderecoResponse> listarEnderecosPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId);

    PessoaContatoResponse buscarContatoPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId);

    PessoaDocumentoMetadataResponse buscarDocumentoPorId(
            String authorization,
            InternalRequestContext context,
            UUID documentoId);

    List<PessoaDocumentoMetadataResponse> listarDocumentosPorPessoa(
            String authorization,
            InternalRequestContext context,
            UUID pessoaId);

    PessoaFuncionarioResumoResponse buscarFuncionarioPorId(
            String authorization,
            InternalRequestContext context,
            UUID funcionarioId);

    List<PessoaFuncionarioResumoResponse> listarFuncionariosAtivosPorEscola(
            String authorization,
            InternalRequestContext context);

    PessoaResumoResponse buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId);

    PessoaConsultaCadastralPageResponse consultarCadastro(
            String authorization,
            InternalRequestContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);
}
