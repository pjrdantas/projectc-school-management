package br.com.escola.aluno.adapter.out.persistence;

import java.util.UUID;

import java.util.List;
import java.util.Optional;
import java.util.Locale;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.responsavel.adapter.out.persistence.entity.AlunoResponsavelEntity;
import br.com.escola.responsavel.adapter.out.persistence.repository.AlunoResponsavelJpaRepository;
import br.com.escola.responsavel.adapter.out.persistence.repository.ResponsavelJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarJpaRepository;
import br.com.escola.historico.adapter.out.persistence.repository.HistoricoEscolarItemJpaRepository;
import br.com.escola.documento.adapter.out.persistence.repository.DocumentoJpaRepository;
import br.com.escola.aluno.adapter.out.persistence.entity.AlunoEntity;
import br.com.escola.aluno.adapter.out.persistence.repository.AlunoJpaRepository;
import br.com.escola.aluno.adapter.out.persistence.repository.StatusAlunoJpaRepository;
import br.com.escola.aluno.application.dto.AlunoInput;
import br.com.escola.aluno.application.dto.AlunoOutput;
import br.com.escola.aluno.application.port.out.AlunoCommandGateway;
import br.com.escola.aluno.application.port.out.AlunoQueryGateway;
import br.com.escola.aluno.domain.exception.AlunoNaoEncontradoException;
import br.com.escola.transferencia.adapter.out.persistence.repository.TransferenciaAlunoJpaRepository;
import br.com.escola.compartilhado.pessoa.dto.EnderecoDados;
import br.com.escola.compartilhado.pessoa.dto.PessoaCriada;
import br.com.escola.compartilhado.pessoa.dto.PessoaDados;
import br.com.escola.compartilhado.endereco.entity.EnderecoEntity;
import br.com.escola.compartilhado.endereco.entity.PessoaEnderecoEntity;
import br.com.escola.compartilhado.endereco.repository.EnderecoJpaRepository;
import br.com.escola.compartilhado.endereco.repository.PessoaEnderecoJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaTipoPessoaJpaRepository;
import br.com.escola.compartilhado.pessoa.port.internal.PessoaCadastroPort;
import br.com.escola.institucional.application.service.EscolaTenantService;

@Component
public class AlunoPersistenceGateway implements AlunoCommandGateway, AlunoQueryGateway {

    private final AlunoJpaRepository alunoJpaRepository;
    private final StatusAlunoJpaRepository statusAlunoJpaRepository;
    private final PessoaJpaRepository pessoaJpaRepository;
    private final PessoaTipoPessoaJpaRepository pessoaTipoPessoaJpaRepository;
    private final EnderecoJpaRepository enderecoJpaRepository;
    private final PessoaEnderecoJpaRepository pessoaEnderecoJpaRepository;
    private final PessoaCadastroPort pessoaCadastroPort;
    private final AlunoResponsavelJpaRepository alunoResponsavelJpaRepository;
    private final ResponsavelJpaRepository responsavelJpaRepository;
    private final HistoricoEscolarJpaRepository historicoEscolarJpaRepository;
    private final HistoricoEscolarItemJpaRepository historicoEscolarItemJpaRepository;
    private final TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository;
    private final DocumentoJpaRepository documentoJpaRepository;
    private final EscolaTenantService escolaTenantService;

    public AlunoPersistenceGateway(
            AlunoJpaRepository alunoJpaRepository,
            StatusAlunoJpaRepository statusAlunoJpaRepository,
            PessoaJpaRepository pessoaJpaRepository,
            PessoaTipoPessoaJpaRepository pessoaTipoPessoaJpaRepository,
            EnderecoJpaRepository enderecoJpaRepository,
            PessoaEnderecoJpaRepository pessoaEnderecoJpaRepository,
            PessoaCadastroPort pessoaCadastroPort,
            AlunoResponsavelJpaRepository alunoResponsavelJpaRepository,
            ResponsavelJpaRepository responsavelJpaRepository,
            HistoricoEscolarJpaRepository historicoEscolarJpaRepository,
            HistoricoEscolarItemJpaRepository historicoEscolarItemJpaRepository,
            TransferenciaAlunoJpaRepository transferenciaAlunoJpaRepository,
            DocumentoJpaRepository documentoJpaRepository,
            EscolaTenantService escolaTenantService) {
        this.alunoJpaRepository = alunoJpaRepository;
        this.statusAlunoJpaRepository = statusAlunoJpaRepository;
        this.pessoaJpaRepository = pessoaJpaRepository;
        this.pessoaTipoPessoaJpaRepository = pessoaTipoPessoaJpaRepository;
        this.enderecoJpaRepository = enderecoJpaRepository;
        this.pessoaEnderecoJpaRepository = pessoaEnderecoJpaRepository;
        this.pessoaCadastroPort = pessoaCadastroPort;
        this.alunoResponsavelJpaRepository = alunoResponsavelJpaRepository;
        this.responsavelJpaRepository = responsavelJpaRepository;
        this.historicoEscolarJpaRepository = historicoEscolarJpaRepository;
        this.historicoEscolarItemJpaRepository = historicoEscolarItemJpaRepository;
        this.transferenciaAlunoJpaRepository = transferenciaAlunoJpaRepository;
        this.documentoJpaRepository = documentoJpaRepository;
        this.escolaTenantService = escolaTenantService;
    }

    @Override
    public boolean existsByCpf(String cpf, UUID escolaId) {
        return alunoJpaRepository.findByCpfAndEscolaId(cpf, resolverEscolaId(escolaId)).isPresent();
    }

    @Override
    public boolean existsByCpfAndIdNot(String cpf, UUID escolaId, @NonNull UUID id) {
        return alunoJpaRepository.existsByCpfAndEscolaIdAndIdNot(cpf, resolverEscolaId(escolaId), id);
    }

    @Override
    @Transactional
    public AlunoOutput save(AlunoInput input) {
        PessoaCriada pessoaCriada = pessoaCadastroPort.criarPessoaComTipoEEndereco(
                toPessoaDados(input),
                "ALUNO",
                toEnderecoDados(input),
                input.escolaId());

        AlunoEntity alunoEntity = new AlunoEntity();
        alunoEntity.setPessoa(pessoaJpaRepository.getReferenceById(pessoaCriada.pessoaId()));
        alunoEntity.setStatusAluno(buscarStatus(input.statusAluno()));
        return toOutput(alunoJpaRepository.save(alunoEntity));
    }

    @Override
    @Transactional
    public AlunoOutput update(@NonNull UUID id, AlunoInput input) {
        AlunoEntity alunoEntity = alunoJpaRepository.findByIdAndPessoa_Escola_Id(id, resolverEscolaId(input.escolaId()))
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));

        pessoaCadastroPort.atualizarPessoaEEndereco(
                alunoEntity.getPessoa(),
                toPessoaDados(input),
                toEnderecoDados(input),
                input.escolaId());
        alunoEntity.setStatusAluno(buscarStatus(input.statusAluno()));
        return toOutput(alunoJpaRepository.save(alunoEntity));
    }

    @Override
    @Transactional
    public void deleteById(@NonNull UUID id) {
        AlunoEntity aluno = alunoJpaRepository.findByIdAndPessoa_Escola_Id(id, resolverEscolaId(null))
                .orElseThrow(() -> new AlunoNaoEncontradoException(id));
        UUID pessoaId = aluno.getPessoa().getId();
        List<UUID> responsaveisVinculados = alunoResponsavelJpaRepository.findByIdAluno(id).stream()
                .map(AlunoResponsavelEntity::getIdResponsavel)
                .toList();

        documentoJpaRepository.deletePessoaDocumentoByPessoaId(pessoaId);
        documentoJpaRepository.deleteDocumentosSemVinculo();
        transferenciaAlunoJpaRepository.deleteByAluno_Id(id);
        historicoEscolarItemJpaRepository.deleteByAlunoDocumental(aluno.getNomeCompleto(), aluno.getDataNascimento());
        historicoEscolarJpaRepository.deleteByAlunoDocumental(aluno.getNomeCompleto(), aluno.getDataNascimento());
        alunoResponsavelJpaRepository.deleteByIdAluno(id);
        responsaveisVinculados.stream()
                .filter(responsavelId -> alunoResponsavelJpaRepository.countByIdResponsavel(responsavelId) == 0)
                .forEach(this::deleteResponsavelExclusivo);
        alunoJpaRepository.delete(aluno);
        alunoJpaRepository.flush();
        cleanupPessoa(pessoaId);
    }

    private void deleteResponsavelExclusivo(UUID responsavelId) {
        responsavelJpaRepository.findById(responsavelId).ifPresent(responsavel -> {
            UUID pessoaId = responsavel.getPessoa().getId();
            responsavelJpaRepository.delete(responsavel);
            responsavelJpaRepository.flush();
            cleanupPessoa(pessoaId);
        });
    }

    private void cleanupPessoa(UUID pessoaId) {
        List<UUID> enderecoIds = pessoaEnderecoJpaRepository.findByPessoaId(pessoaId).stream()
                .map(PessoaEnderecoEntity::getEndereco)
                .map(EnderecoEntity::getId)
                .toList();

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
    public Optional<AlunoOutput> findById(@NonNull UUID id) {
        return alunoJpaRepository.findByIdAndPessoa_Escola_Id(id, resolverEscolaId(null)).map(this::toOutput);
    }

    @Override
    public List<AlunoOutput> findAll() {
        return alunoJpaRepository.findAllByPessoa_Escola_Id(resolverEscolaId(null)).stream().map(this::toOutput).toList();
    }

    @Override
    public boolean existsById(@NonNull UUID id) {
        return alunoJpaRepository.existsByIdAndPessoa_Escola_Id(id, resolverEscolaId(null));
    }

    private AlunoOutput toOutput(AlunoEntity entity) {
        EnderecoEntity endereco = pessoaEnderecoJpaRepository
                .findPrincipalByPessoaId(entity.getPessoa().getId())
                .map(PessoaEnderecoEntity::getEndereco)
                .orElse(null);

        return new AlunoOutput(
                entity.getId(),
                entity.getNomeCompleto(),
                entity.getCpf(),
                entity.getEmail(),
                entity.getTelefone(),
                entity.getDataNascimento(),
                entity.getRg(),
                entity.getOrgaoEmissorRg(),
                entity.getUfRg(),
                entity.getNacionalidade(),
                entity.getNaturalidade(),
                entity.getSexo(),
                entity.getNomeSocial(),
                endereco != null ? endereco.getCep() : null,
                endereco != null ? endereco.getLogradouro() : null,
                endereco != null ? endereco.getNumero() : null,
                endereco != null ? endereco.getComplemento() : null,
                endereco != null ? endereco.getBairro() : null,
                endereco != null ? endereco.getCidade() : null,
                endereco != null ? endereco.getUf() : null,
                entity.getStatusAluno(),
                entity.getPessoa().getEscola().getId(),
                entity.getPessoa().getEscola().getNome(),
                entity.getCreatedAt());
    }

    private PessoaDados toPessoaDados(AlunoInput input) {
        return new PessoaDados(
                input.nomeCompleto(),
                input.cpf(),
                input.rg(),
                input.orgaoEmissorRg(),
                input.ufRg(),
                input.email(),
                input.telefone(),
                input.dataNascimento(),
                input.sexo(),
                input.nomeSocial(),
                input.nacionalidade(),
                input.naturalidade(),
                true);
    }

    private EnderecoDados toEnderecoDados(AlunoInput input) {
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

    private br.com.escola.aluno.adapter.out.persistence.entity.StatusAlunoEntity buscarStatus(String codigo) {
        String codigoNormalizado = codigo == null || codigo.isBlank()
                ? "ATIVO"
                : codigo.trim().toUpperCase(Locale.ROOT);
        return statusAlunoJpaRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Status de aluno nao cadastrado: " + codigoNormalizado));
    }

    private UUID resolverEscolaId(UUID escolaId) {
        return escolaId == null ? escolaTenantService.obterOuCriarEscolaPadrao().getId() : escolaId;
    }
}
