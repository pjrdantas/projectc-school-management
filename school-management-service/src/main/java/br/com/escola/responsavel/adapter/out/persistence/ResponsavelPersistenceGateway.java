package br.com.escola.responsavel.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.responsavel.adapter.out.persistence.repository.AlunoResponsavelJpaRepository;
import br.com.escola.responsavel.adapter.out.persistence.entity.ResponsavelEntity;
import br.com.escola.responsavel.adapter.out.persistence.repository.ResponsavelJpaRepository;
import br.com.escola.responsavel.application.dto.ResponsavelInput;
import br.com.escola.responsavel.application.dto.ResponsavelOutput;
import br.com.escola.responsavel.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavel.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavel.domain.exception.ResponsavelNaoEncontradoException;
import br.com.escola.compartilhado.pessoa.dto.EnderecoDados;
import br.com.escola.compartilhado.pessoa.dto.PessoaCriada;
import br.com.escola.compartilhado.pessoa.dto.PessoaDados;
import br.com.escola.compartilhado.endereco.entity.EnderecoEntity;
import br.com.escola.compartilhado.endereco.entity.PessoaEnderecoEntity;
import br.com.escola.compartilhado.endereco.repository.EnderecoJpaRepository;
import br.com.escola.compartilhado.endereco.repository.PessoaEnderecoJpaRepository;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaCadastroPort;
import br.com.escola.compartilhado.pessoa.repository.PessoaJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaTipoPessoaJpaRepository;
import br.com.escola.documento.adapter.out.persistence.repository.DocumentoJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Component
public class ResponsavelPersistenceGateway implements ResponsavelCommandGateway, ResponsavelQueryGateway {

    private final ResponsavelJpaRepository responsavelJpaRepository;
    private final PessoaJpaRepository pessoaJpaRepository;
    private final PessoaTipoPessoaJpaRepository pessoaTipoPessoaJpaRepository;
    private final EnderecoJpaRepository enderecoJpaRepository;
    private final DocumentoJpaRepository documentoJpaRepository;
    private final PessoaEnderecoJpaRepository pessoaEnderecoJpaRepository;
    private final PessoaCadastroPort pessoaCadastroPort;
    private final AlunoResponsavelJpaRepository alunoResponsavelJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public ResponsavelPersistenceGateway(
            ResponsavelJpaRepository responsavelJpaRepository,
            PessoaJpaRepository pessoaJpaRepository,
            PessoaTipoPessoaJpaRepository pessoaTipoPessoaJpaRepository,
            EnderecoJpaRepository enderecoJpaRepository,
            DocumentoJpaRepository documentoJpaRepository,
            PessoaEnderecoJpaRepository pessoaEnderecoJpaRepository,
            PessoaCadastroPort pessoaCadastroPort,
            AlunoResponsavelJpaRepository alunoResponsavelJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.responsavelJpaRepository = responsavelJpaRepository;
        this.pessoaJpaRepository = pessoaJpaRepository;
        this.pessoaTipoPessoaJpaRepository = pessoaTipoPessoaJpaRepository;
        this.enderecoJpaRepository = enderecoJpaRepository;
        this.documentoJpaRepository = documentoJpaRepository;
        this.pessoaEnderecoJpaRepository = pessoaEnderecoJpaRepository;
        this.pessoaCadastroPort = pessoaCadastroPort;
        this.alunoResponsavelJpaRepository = alunoResponsavelJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    public boolean existsByCpf(String cpf, UUID escolaId) {
        return responsavelJpaRepository.findByCpfAndEscolaId(cpf, resolverEscolaId(escolaId)).isPresent();
    }

    @Override
    public boolean existsByCpfAndIdNot(String cpf, UUID escolaId, UUID id) {
        return responsavelJpaRepository.existsByCpfAndEscolaIdAndIdNot(cpf, resolverEscolaId(escolaId), id);
    }

    @Override
    @Transactional
    public ResponsavelOutput save(ResponsavelInput input) {
        PessoaCriada pessoaCriada = pessoaCadastroPort.criarPessoaComTipoEEndereco(
                toPessoaDados(input),
                "RESPONSAVEL",
                toEnderecoDados(input),
                input.escolaId());

        ResponsavelEntity entity = new ResponsavelEntity();
        entity.setPessoa(pessoaJpaRepository.getReferenceById(pessoaCriada.pessoaId()));
        return toOutput(responsavelJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ResponsavelOutput update(UUID id, ResponsavelInput input) {
        ResponsavelEntity entity = responsavelJpaRepository.findByIdAndPessoa_Escola_Id(id, resolverEscolaId(input.escolaId()))
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));

        pessoaCadastroPort.atualizarPessoaEEndereco(
                entity.getPessoa(),
                toPessoaDados(input),
                toEnderecoDados(input),
                input.escolaId());
        return toOutput(responsavelJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        ResponsavelEntity responsavel = responsavelJpaRepository.findByIdAndPessoa_Escola_Id(id, resolverEscolaId(null))
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));
        UUID pessoaId = responsavel.getPessoa().getId();
        List<UUID> enderecoIds = pessoaEnderecoJpaRepository.findByPessoaId(pessoaId).stream()
                .map(vinculo -> vinculo.getEndereco().getId())
                .toList();

        responsavelJpaRepository.delete(responsavel);
        responsavelJpaRepository.flush();
        documentoJpaRepository.deletePessoaDocumentoByPessoaId(pessoaId);
        documentoJpaRepository.deleteDocumentosSemVinculo();
        pessoaEnderecoJpaRepository.deleteByPessoaId(pessoaId);
        enderecoIds.stream()
                .filter(enderecoId -> pessoaEnderecoJpaRepository.countByEnderecoId(enderecoId) == 0)
                .forEach(enderecoJpaRepository::deleteById);
        pessoaTipoPessoaJpaRepository.deleteByPessoaId(pessoaId);
        pessoaJpaRepository.deleteById(pessoaId);
    }

    @Override
    public Optional<ResponsavelOutput> findById(UUID id) {
        return responsavelJpaRepository.findByIdAndPessoa_Escola_Id(id, resolverEscolaId(null)).map(this::toOutput);
    }

    @Override
    public List<ResponsavelOutput> findAll() {
        return responsavelJpaRepository.findAllByPessoa_Escola_Id(resolverEscolaId(null)).stream().map(this::toOutput).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return responsavelJpaRepository.existsByIdAndPessoa_Escola_Id(id, resolverEscolaId(null));
    }

    @Override
    public boolean hasAlunosVinculados(UUID id) {
        return alunoResponsavelJpaRepository.countByIdResponsavel(id) > 0;
    }

    private ResponsavelOutput toOutput(ResponsavelEntity entity) {
        EnderecoEntity endereco = pessoaEnderecoJpaRepository
                .findPrincipalByPessoaId(entity.getPessoa().getId())
                .map(PessoaEnderecoEntity::getEndereco)
                .orElse(null);

        return new ResponsavelOutput(
                entity.getId(),
                entity.getNomeCompleto(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getRg(),
                endereco != null ? endereco.getCep() : null,
                endereco != null ? endereco.getLogradouro() : null,
                endereco != null ? endereco.getNumero() : null,
                endereco != null ? endereco.getComplemento() : null,
                endereco != null ? endereco.getBairro() : null,
                endereco != null ? endereco.getCidade() : null,
                endereco != null ? endereco.getUf() : null,
                entity.getPessoa().getEscola().getId(),
                entity.getPessoa().getEscola().getNome(),
                entity.getCreatedAt());
    }

    private PessoaDados toPessoaDados(ResponsavelInput input) {
        return new PessoaDados(
                input.nomeCompleto(),
                input.cpf(),
                input.rg(),
                null,
                null,
                input.email(),
                input.telefone(),
                null,
                null,
                null,
                null,
                null,
                true);
    }

    private EnderecoDados toEnderecoDados(ResponsavelInput input) {
        return new EnderecoDados(
                input.cep(),
                input.logradouro(),
                input.numero(),
                input.complemento(),
                input.bairro(),
                input.cidade(),
                input.uf(),
                "RESIDENCIAL",
                true);
    }

    private UUID resolverEscolaId(UUID escolaId) {
        return escolaId == null ? escolaTenantService.obterOuCriarEscolaPadrao().getId() : escolaId;
    }
}
