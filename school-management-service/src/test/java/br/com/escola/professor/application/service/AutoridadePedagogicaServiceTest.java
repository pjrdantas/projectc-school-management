package br.com.escola.professor.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import br.com.escola.compartilhado.pessoa.entity.PessoaEntity;
import br.com.escola.professor.application.port.internal.AutoridadePedagogicaPort;
import br.com.escola.rh.adapter.out.persistence.entity.CargoEntity;
import br.com.escola.rh.adapter.out.persistence.entity.FuncionarioEntity;
import br.com.escola.rh.adapter.out.persistence.repository.FuncionarioJpaRepository;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringUsuarioJpaRepository;
import br.com.escola.seguranca.application.dto.internal.ContextoAutenticadoResumo;
import br.com.escola.seguranca.application.port.internal.IdentidadeTenantPort;

@ExtendWith(MockitoExtension.class)
class AutoridadePedagogicaServiceTest {

    @Mock
    private IdentidadeTenantPort identidadeTenantPort;

    @Mock
    private SpringUsuarioJpaRepository usuarioRepository;

    @Mock
    private FuncionarioJpaRepository funcionarioRepository;

    private AutoridadePedagogicaService service;

    @BeforeEach
    void setUp() {
        service = new AutoridadePedagogicaService(
                identidadeTenantPort,
                usuarioRepository,
                funcionarioRepository);
    }

    @Test
    void deveResolverCoordenadorPedagogicoPeloUsuarioAutenticado() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID funcionarioId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();

        when(identidadeTenantPort.resolverContextoAtual("access-token")).thenReturn(contexto(usuarioId, escolaId));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId, "coord@example.com")));
        when(funcionarioRepository.findAtivosByPessoaEmailAndEscolaId("coord@example.com", escolaId))
                .thenReturn(List.of(funcionario(funcionarioId, pessoaId, "Maria Coordenadora", "COORDENADOR")));

        var resumo = service.resolverCoordenacaoOuDirecao("access-token");

        assertThat(resumo.usuarioId()).isEqualTo(usuarioId);
        assertThat(resumo.escolaId()).isEqualTo(escolaId);
        assertThat(resumo.funcionarioId()).isEqualTo(funcionarioId);
        assertThat(resumo.pessoaId()).isEqualTo(pessoaId);
        assertThat(resumo.nome()).isEqualTo("Maria Coordenadora");
        assertThat(resumo.cargoCodigo()).isEqualTo("COORDENADOR");
        assertThat(resumo.ehCoordenador()).isTrue();
        assertThat(resumo.ehDiretor()).isFalse();
        assertThat(resumo.perfis()).containsExactly("COORDENACAO");
        assertThat(resumo.permissoes()).containsExactly("DIARIO_CHECAGEM");
        verify(funcionarioRepository).findAtivosByPessoaEmailAndEscolaId("coord@example.com", escolaId);
    }

    @Test
    void deveResolverDiretorQuandoCargoPermitidoForDiretor() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        when(identidadeTenantPort.resolverContextoAtual("access-token")).thenReturn(contexto(usuarioId, escolaId));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId, "diretor@example.com")));
        when(funcionarioRepository.findAtivosByPessoaEmailAndEscolaId("diretor@example.com", escolaId))
                .thenReturn(List.of(funcionario(UUID.randomUUID(), UUID.randomUUID(), "Diretor Escolar", "DIRETOR")));

        var resumo = service.resolver("access-token", Set.of("DIRETOR"));

        assertThat(resumo.cargoCodigo()).isEqualTo("DIRETOR");
        assertThat(resumo.ehDiretor()).isTrue();
    }

    @Test
    void deveNegarQuandoUsuarioNaoPossuiFuncionarioAtivoNaEscola() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        when(identidadeTenantPort.resolverContextoAtual("access-token")).thenReturn(contexto(usuarioId, escolaId));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId, "sem-vinculo@example.com")));
        when(funcionarioRepository.findAtivosByPessoaEmailAndEscolaId("sem-vinculo@example.com", escolaId))
                .thenReturn(List.of());

        assertThatThrownBy(() -> service.resolverCoordenacaoOuDirecao("access-token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void deveNegarQuandoCargoDoFuncionarioNaoForPedagogicoPermitido() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();

        when(identidadeTenantPort.resolverContextoAtual("access-token")).thenReturn(contexto(usuarioId, escolaId));
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario(usuarioId, "secretaria@example.com")));
        when(funcionarioRepository.findAtivosByPessoaEmailAndEscolaId("secretaria@example.com", escolaId))
                .thenReturn(List.of(funcionario(UUID.randomUUID(), UUID.randomUUID(), "Secretaria", "SECRETARIA")));

        assertThatThrownBy(() -> service.resolverCoordenacaoOuDirecao("access-token"))
                .isInstanceOf(AccessDeniedException.class);
    }

    private ContextoAutenticadoResumo contexto(UUID usuarioId, UUID escolaId) {
        return new ContextoAutenticadoResumo(
                usuarioId,
                escolaId,
                "Escola Teste",
                "usuario",
                List.of("COORDENACAO"),
                List.of("DIARIO_CHECAGEM"));
    }

    private UsuarioEntity usuario(UUID id, String email) {
        return UsuarioEntity.builder()
                .id(id)
                .username(email)
                .nome("Usuario Teste")
                .email(email)
                .senhaHash("hash")
                .ativo(true)
                .build();
    }

    private FuncionarioEntity funcionario(UUID id, UUID pessoaId, String nome, String cargoCodigo) {
        PessoaEntity pessoa = new PessoaEntity();
        pessoa.setId(pessoaId);
        pessoa.setNomeCompleto(nome);
        pessoa.setEmail(nome + "@example.com");
        pessoa.setAtivo(true);

        return FuncionarioEntity.builder()
                .id(id)
                .ativo(true)
                .pessoa(pessoa)
                .cargo(CargoEntity.builder()
                        .id(UUID.randomUUID())
                        .codigo(cargoCodigo)
                        .descricao(cargoCodigo)
                        .build())
                .build();
    }
}
