package br.com.escola.compartilhado.pessoa.service;

import java.util.Locale;
import java.util.Optional;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import br.com.escola.compartilhado.pessoa.dto.EnderecoDados;
import br.com.escola.compartilhado.pessoa.dto.CatalogoPessoaResponse;
import br.com.escola.compartilhado.pessoa.dto.PessoaCriada;
import br.com.escola.compartilhado.pessoa.dto.PessoaDados;
import br.com.escola.compartilhado.endereco.entity.EnderecoEntity;
import br.com.escola.compartilhado.endereco.entity.PessoaEnderecoEntity;
import br.com.escola.compartilhado.pessoa.entity.PessoaEntity;
import br.com.escola.compartilhado.pessoa.entity.PessoaTipoPessoaEntity;
import br.com.escola.compartilhado.endereco.entity.TipoEnderecoEntity;
import br.com.escola.compartilhado.pessoa.entity.TipoPessoaEntity;
import br.com.escola.compartilhado.endereco.repository.EnderecoJpaRepository;
import br.com.escola.compartilhado.endereco.repository.PessoaEnderecoJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaTipoPessoaJpaRepository;
import br.com.escola.compartilhado.endereco.repository.TipoEnderecoJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.TipoPessoaJpaRepository;

@Service
public class PessoaFoundationService {

    private static final String TIPO_ENDERECO_PADRAO = "RESIDENCIAL";

    private final PessoaJpaRepository pessoaRepository;
    private final TipoPessoaJpaRepository tipoPessoaRepository;
    private final PessoaTipoPessoaJpaRepository pessoaTipoPessoaRepository;
    private final EnderecoJpaRepository enderecoRepository;
    private final TipoEnderecoJpaRepository tipoEnderecoRepository;
    private final PessoaEnderecoJpaRepository pessoaEnderecoRepository;

    public PessoaFoundationService(
            PessoaJpaRepository pessoaRepository,
            TipoPessoaJpaRepository tipoPessoaRepository,
            PessoaTipoPessoaJpaRepository pessoaTipoPessoaRepository,
            EnderecoJpaRepository enderecoRepository,
            TipoEnderecoJpaRepository tipoEnderecoRepository,
            PessoaEnderecoJpaRepository pessoaEnderecoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.tipoPessoaRepository = tipoPessoaRepository;
        this.pessoaTipoPessoaRepository = pessoaTipoPessoaRepository;
        this.enderecoRepository = enderecoRepository;
        this.tipoEnderecoRepository = tipoEnderecoRepository;
        this.pessoaEnderecoRepository = pessoaEnderecoRepository;
    }

    @Transactional
    public PessoaCriada criarPessoaComTipoEEndereco(
            PessoaDados pessoaDados,
            String tipoPessoaCodigo,
            EnderecoDados enderecoDados) {
        TipoPessoaEntity tipoPessoa = buscarTipoPessoaObrigatorio(tipoPessoaCodigo);
        PessoaEntity pessoa = pessoaRepository.save(toPessoaEntity(pessoaDados));
        vincularTipoPessoa(pessoa, tipoPessoa);

        if (enderecoDados == null || isEnderecoVazio(enderecoDados)) {
            return new PessoaCriada(pessoa.getId(), null, null);
        }

        EnderecoEntity endereco = enderecoRepository.save(toEnderecoEntity(enderecoDados));
        PessoaEnderecoEntity vinculoEndereco = vincularEndereco(pessoa, endereco, enderecoDados);
        return new PessoaCriada(pessoa.getId(), endereco.getId(), vinculoEndereco.getId());
    }

    @Transactional
    public void atualizarPessoaEEndereco(PessoaEntity pessoa, PessoaDados pessoaDados, EnderecoDados enderecoDados) {
        preencherPessoa(pessoa, pessoaDados);
        pessoaRepository.save(pessoa);

        if (enderecoDados == null || isEnderecoVazio(enderecoDados)) {
            return;
        }

        PessoaEnderecoEntity vinculo = pessoaEnderecoRepository.findPrincipalByPessoaId(pessoa.getId())
                .orElse(null);

        if (vinculo == null) {
            EnderecoEntity endereco = enderecoRepository.save(toEnderecoEntity(enderecoDados));
            vincularEndereco(pessoa, endereco, enderecoDados);
            return;
        }

        EnderecoEntity endereco = vinculo.getEndereco();
        preencherEndereco(endereco, enderecoDados);
        enderecoRepository.save(endereco);
        vinculo.setTipoEndereco(buscarTipoEndereco(enderecoDados.tipoEnderecoCodigo()));
        vinculo.setPrincipal(enderecoDados.principal() == null || enderecoDados.principal());
        pessoaEnderecoRepository.save(vinculo);
    }

    @Transactional
    public void vincularTipoPessoa(PessoaEntity pessoa, String tipoPessoaCodigo) {
        vincularTipoPessoa(pessoa, buscarTipoPessoaObrigatorio(tipoPessoaCodigo));
    }

    @Transactional(readOnly = true)
    public TipoPessoaEntity buscarTipoPessoaObrigatorio(String codigo) {
        return tipoPessoaRepository.findByCodigo(normalizarCodigo(codigo))
                .orElseThrow(() -> new IllegalArgumentException("Tipo de pessoa nao cadastrado: " + codigo));
    }

    @Transactional(readOnly = true)
    public Optional<PessoaEntity> buscarPorCpf(String cpf) {
        if (!StringUtils.hasText(cpf)) {
            return Optional.empty();
        }
        return pessoaRepository.findByCpf(cpf.trim());
    }

    @Transactional(readOnly = true)
    public List<CatalogoPessoaResponse> listarTiposPessoa() {
        return tipoPessoaRepository.findAll().stream()
                .map(tipo -> new CatalogoPessoaResponse(tipo.getId(), tipo.getCodigo(), tipo.getDescricao()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CatalogoPessoaResponse> listarTiposEndereco() {
        return tipoEnderecoRepository.findAll().stream()
                .map(tipo -> new CatalogoPessoaResponse(tipo.getId(), tipo.getCodigo(), tipo.getDescricao()))
                .toList();
    }

    private void vincularTipoPessoa(PessoaEntity pessoa, TipoPessoaEntity tipoPessoa) {
        if (pessoaTipoPessoaRepository.existsByPessoaAndTipoPessoa(pessoa, tipoPessoa)) {
            return;
        }
        PessoaTipoPessoaEntity vinculo = new PessoaTipoPessoaEntity();
        vinculo.setPessoa(pessoa);
        vinculo.setTipoPessoa(tipoPessoa);
        pessoaTipoPessoaRepository.save(vinculo);
    }

    private PessoaEnderecoEntity vincularEndereco(PessoaEntity pessoa, EnderecoEntity endereco, EnderecoDados dados) {
        PessoaEnderecoEntity vinculo = new PessoaEnderecoEntity();
        vinculo.setPessoa(pessoa);
        vinculo.setEndereco(endereco);
        vinculo.setPrincipal(dados.principal() == null || dados.principal());
        vinculo.setTipoEndereco(buscarTipoEndereco(dados.tipoEnderecoCodigo()));
        return pessoaEnderecoRepository.save(vinculo);
    }

    private TipoEnderecoEntity buscarTipoEndereco(String codigo) {
        String codigoNormalizado = StringUtils.hasText(codigo)
                ? normalizarCodigo(codigo)
                : TIPO_ENDERECO_PADRAO;

        return tipoEnderecoRepository.findByCodigo(codigoNormalizado)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de endereco nao cadastrado: " + codigoNormalizado));
    }

    private PessoaEntity toPessoaEntity(PessoaDados dados) {
        if (dados == null || !StringUtils.hasText(dados.nomeCompleto())) {
            throw new IllegalArgumentException("Nome completo da pessoa e obrigatorio.");
        }

        PessoaEntity entity = new PessoaEntity();
        preencherPessoa(entity, dados);
        return entity;
    }

    private void preencherPessoa(PessoaEntity entity, PessoaDados dados) {
        if (dados == null || !StringUtils.hasText(dados.nomeCompleto())) {
            throw new IllegalArgumentException("Nome completo da pessoa e obrigatorio.");
        }
        entity.setNomeCompleto(dados.nomeCompleto().trim());
        entity.setCpf(trimToNull(dados.cpf()));
        entity.setRg(trimToNull(dados.rg()));
        entity.setOrgaoEmissorRg(trimToNull(dados.orgaoEmissorRg()));
        entity.setUfRg(trimToNull(dados.ufRg()));
        entity.setEmail(trimToNull(dados.email()));
        entity.setTelefone(trimToNull(dados.telefone()));
        entity.setDataNascimento(dados.dataNascimento());
        entity.setSexo(trimToNull(dados.sexo()));
        entity.setNomeSocial(trimToNull(dados.nomeSocial()));
        entity.setNacionalidade(trimToNull(dados.nacionalidade()));
        entity.setNaturalidade(trimToNull(dados.naturalidade()));
        entity.setAtivo(dados.ativo() == null || dados.ativo());
    }

    private EnderecoEntity toEnderecoEntity(EnderecoDados dados) {
        EnderecoEntity entity = new EnderecoEntity();
        preencherEndereco(entity, dados);
        return entity;
    }

    private void preencherEndereco(EnderecoEntity entity, EnderecoDados dados) {
        entity.setCep(trimToNull(dados.cep()));
        entity.setLogradouro(trimToNull(dados.logradouro()));
        entity.setNumero(trimToNull(dados.numero()));
        entity.setComplemento(trimToNull(dados.complemento()));
        entity.setBairro(trimToNull(dados.bairro()));
        entity.setCidade(trimToNull(dados.cidade()));
        entity.setUf(trimToNull(dados.uf()));
    }

    private boolean isEnderecoVazio(EnderecoDados dados) {
        return !StringUtils.hasText(dados.cep())
                && !StringUtils.hasText(dados.logradouro())
                && !StringUtils.hasText(dados.numero())
                && !StringUtils.hasText(dados.complemento())
                && !StringUtils.hasText(dados.bairro())
                && !StringUtils.hasText(dados.cidade())
                && !StringUtils.hasText(dados.uf());
    }

    private String normalizarCodigo(String codigo) {
        if (!StringUtils.hasText(codigo)) {
            throw new IllegalArgumentException("Codigo obrigatorio.");
        }
        return codigo.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
