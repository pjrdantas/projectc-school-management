package br.com.escola.compartilhado.pessoa.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import br.com.escola.compartilhado.endereco.entity.EnderecoEntity;
import br.com.escola.compartilhado.endereco.entity.PessoaEnderecoEntity;
import br.com.escola.compartilhado.endereco.entity.TipoEnderecoEntity;
import br.com.escola.compartilhado.endereco.repository.EnderecoJpaRepository;
import br.com.escola.compartilhado.endereco.repository.PessoaEnderecoJpaRepository;
import br.com.escola.compartilhado.endereco.repository.TipoEnderecoJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.PessoaTipoPessoaJpaRepository;
import br.com.escola.compartilhado.pessoa.repository.TipoPessoaJpaRepository;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.service.EscolaTenantService;

@ExtendWith(MockitoExtension.class)
class PessoaFoundationServiceEnderecoPortTest {

    @Mock
    private PessoaJpaRepository pessoaRepository;

    @Mock
    private TipoPessoaJpaRepository tipoPessoaRepository;

    @Mock
    private PessoaTipoPessoaJpaRepository pessoaTipoPessoaRepository;

    @Mock
    private EnderecoJpaRepository enderecoRepository;

    @Mock
    private TipoEnderecoJpaRepository tipoEnderecoRepository;

    @Mock
    private PessoaEnderecoJpaRepository pessoaEnderecoRepository;

    @Mock
    private EscolaJpaRepository escolaJpaRepository;

    @Mock
    private EscolaTenantService escolaTenantService;

    @InjectMocks
    private PessoaFoundationService service;

    @Test
    void deveRetornarEnderecoPrincipalComoResumoSemExporEntidadeJpa() {
        UUID pessoaId = UUID.randomUUID();
        UUID enderecoId = UUID.randomUUID();
        UUID pessoaEnderecoId = UUID.randomUUID();
        EnderecoEntity endereco = endereco(enderecoId);
        TipoEnderecoEntity tipoEndereco = tipoEndereco("RESIDENCIAL");
        PessoaEnderecoEntity vinculo = new PessoaEnderecoEntity();
        ReflectionTestUtils.setField(vinculo, "id", pessoaEnderecoId);
        vinculo.setEndereco(endereco);
        vinculo.setTipoEndereco(tipoEndereco);
        vinculo.setPrincipal(true);
        when(pessoaEnderecoRepository.findPrincipalByPessoaId(pessoaId)).thenReturn(Optional.of(vinculo));

        var resumo = service.buscarEnderecoPrincipalPorPessoa(pessoaId);

        assertThat(resumo).isPresent();
        assertThat(resumo.get().pessoaEnderecoId()).isEqualTo(pessoaEnderecoId);
        assertThat(resumo.get().enderecoId()).isEqualTo(enderecoId);
        assertThat(resumo.get().cep()).isEqualTo("01001000");
        assertThat(resumo.get().logradouro()).isEqualTo("Praca da Se");
        assertThat(resumo.get().tipoEnderecoCodigo()).isEqualTo("RESIDENCIAL");
        assertThat(resumo.get().principal()).isTrue();
    }

    @Test
    void deveRemoverVinculosEExcluirApenasEnderecosOrfaos() {
        UUID pessoaId = UUID.randomUUID();
        UUID enderecoOrfaoId = UUID.randomUUID();
        UUID enderecoCompartilhadoId = UUID.randomUUID();
        PessoaEnderecoEntity vinculoOrfao = vinculo(enderecoOrfaoId);
        PessoaEnderecoEntity vinculoCompartilhado = vinculo(enderecoCompartilhadoId);
        when(pessoaEnderecoRepository.findByPessoaId(pessoaId))
                .thenReturn(List.of(vinculoOrfao, vinculoCompartilhado));
        when(pessoaEnderecoRepository.countByEnderecoId(enderecoOrfaoId)).thenReturn(0L);
        when(pessoaEnderecoRepository.countByEnderecoId(enderecoCompartilhadoId)).thenReturn(1L);

        service.removerEnderecosDaPessoaRemovendoOrfaos(pessoaId);

        verify(pessoaEnderecoRepository).deleteByPessoaId(pessoaId);
        verify(enderecoRepository).deleteById(enderecoOrfaoId);
        verify(enderecoRepository, never()).deleteById(enderecoCompartilhadoId);
    }

    private PessoaEnderecoEntity vinculo(UUID enderecoId) {
        PessoaEnderecoEntity vinculo = new PessoaEnderecoEntity();
        vinculo.setEndereco(endereco(enderecoId));
        return vinculo;
    }

    private EnderecoEntity endereco(UUID id) {
        EnderecoEntity endereco = new EnderecoEntity();
        ReflectionTestUtils.setField(endereco, "id", id);
        endereco.setCep("01001000");
        endereco.setLogradouro("Praca da Se");
        endereco.setNumero("100");
        endereco.setComplemento("lado impar");
        endereco.setBairro("Se");
        endereco.setCidade("Sao Paulo");
        endereco.setUf("SP");
        return endereco;
    }

    private TipoEnderecoEntity tipoEndereco(String codigo) {
        TipoEnderecoEntity tipoEndereco = new TipoEnderecoEntity();
        ReflectionTestUtils.setField(tipoEndereco, "codigo", codigo);
        return tipoEndereco;
    }
}
