package br.com.escola.responsavelmanagement.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.responsavelmanagement.adapter.out.persistence.repository.AlunoResponsavelJpaRepository;
import br.com.escola.responsavelmanagement.adapter.out.persistence.entity.ResponsavelEntity;
import br.com.escola.responsavelmanagement.adapter.out.persistence.repository.ResponsavelJpaRepository;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelInput;
import br.com.escola.responsavelmanagement.application.dto.ResponsavelOutput;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelCommandGateway;
import br.com.escola.responsavelmanagement.application.port.out.ResponsavelQueryGateway;
import br.com.escola.responsavelmanagement.domain.exception.ResponsavelNaoEncontradoException;
import br.com.escola.shared.person.dto.EnderecoDados;
import br.com.escola.shared.person.dto.PessoaCriada;
import br.com.escola.shared.person.dto.PessoaDados;
import br.com.escola.shared.person.entity.EnderecoEntity;
import br.com.escola.shared.person.entity.PessoaEnderecoEntity;
import br.com.escola.shared.person.repository.PessoaEnderecoJpaRepository;
import br.com.escola.shared.person.repository.PessoaJpaRepository;
import br.com.escola.shared.person.service.PessoaFoundationService;

@Component
public class ResponsavelPersistenceGateway implements ResponsavelCommandGateway, ResponsavelQueryGateway {

    private final ResponsavelJpaRepository responsavelJpaRepository;
    private final PessoaJpaRepository pessoaJpaRepository;
    private final PessoaEnderecoJpaRepository pessoaEnderecoJpaRepository;
    private final PessoaFoundationService pessoaFoundationService;
    private final AlunoResponsavelJpaRepository alunoResponsavelJpaRepository;

    public ResponsavelPersistenceGateway(
            ResponsavelJpaRepository responsavelJpaRepository,
            PessoaJpaRepository pessoaJpaRepository,
            PessoaEnderecoJpaRepository pessoaEnderecoJpaRepository,
            PessoaFoundationService pessoaFoundationService,
            AlunoResponsavelJpaRepository alunoResponsavelJpaRepository) {
        this.responsavelJpaRepository = responsavelJpaRepository;
        this.pessoaJpaRepository = pessoaJpaRepository;
        this.pessoaEnderecoJpaRepository = pessoaEnderecoJpaRepository;
        this.pessoaFoundationService = pessoaFoundationService;
        this.alunoResponsavelJpaRepository = alunoResponsavelJpaRepository;
    }

    @Override
    public boolean existsByCpf(String cpf) {
        return responsavelJpaRepository.findByCpf(cpf).isPresent();
    }

    @Override
    public boolean existsByCpfAndIdNot(String cpf, UUID id) {
        return responsavelJpaRepository.existsByCpfAndIdNot(cpf, id);
    }

    @Override
    @Transactional
    public ResponsavelOutput save(ResponsavelInput input) {
        PessoaCriada pessoaCriada = pessoaFoundationService.criarPessoaComTipoEEndereco(
                toPessoaDados(input),
                "RESPONSAVEL",
                toEnderecoDados(input));

        ResponsavelEntity entity = new ResponsavelEntity();
        entity.setPessoa(pessoaJpaRepository.getReferenceById(pessoaCriada.pessoaId()));
        return toOutput(responsavelJpaRepository.save(entity));
    }

    @Override
    @Transactional
    public ResponsavelOutput update(UUID id, ResponsavelInput input) {
        ResponsavelEntity entity = responsavelJpaRepository.findById(id)
                .orElseThrow(() -> new ResponsavelNaoEncontradoException(id));

        pessoaFoundationService.atualizarPessoaEEndereco(
                entity.getPessoa(),
                toPessoaDados(input),
                toEnderecoDados(input));
        return toOutput(responsavelJpaRepository.save(entity));
    }

    @Override
    public void deleteById(UUID id) {
        responsavelJpaRepository.deleteById(id);
    }

    @Override
    public Optional<ResponsavelOutput> findById(UUID id) {
        return responsavelJpaRepository.findById(id).map(this::toOutput);
    }

    @Override
    public List<ResponsavelOutput> findAll() {
        return responsavelJpaRepository.findAll().stream().map(this::toOutput).toList();
    }

    @Override
    public boolean existsById(UUID id) {
        return responsavelJpaRepository.existsById(id);
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
}
