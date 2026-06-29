package br.com.escola.seguranca.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.application.dto.OrigemTenantAtivo;
import br.com.escola.institucional.application.dto.TenantAtivoResumo;
import br.com.escola.institucional.application.port.internal.TenantAtivoPort;
import br.com.escola.institucional.application.port.internal.UsuarioEscolaPort;
import br.com.escola.professor.adapter.out.persistence.repository.ProfessorJpaRepository;
import br.com.escola.seguranca.adapter.out.persistence.entity.PerfilEntity;
import br.com.escola.seguranca.adapter.out.persistence.entity.SessaoAutenticacaoEntity;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.repository.SessaoAutenticacaoJpaRepository;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringUsuarioJpaRepository;

@ExtendWith(MockitoExtension.class)
class IdentidadeTenantServiceTest {

    @Mock
    private SpringUsuarioJpaRepository usuarioRepository;

    @Mock
    private SessaoAutenticacaoJpaRepository sessaoRepository;

    @Mock
    private ProfessorJpaRepository professorRepository;

    @Mock
    private TenantAtivoPort tenantAtivoPort;

    @Mock
    private UsuarioEscolaPort usuarioEscolaPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JdbcTemplate jdbcTemplate;

    private IdentidadeTenantService service;

    @BeforeEach
    void setUp() {
        service = new IdentidadeTenantService(
                usuarioRepository,
                sessaoRepository,
                professorRepository,
                tenantAtivoPort,
                usuarioEscolaPort,
                passwordEncoder,
                jdbcTemplate);
    }

    @Test
    void deveAutenticarEResolverResumoDaSessaoPelaFronteiraInterna() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UUID professorId = UUID.randomUUID();
        UsuarioEntity usuario = usuario(usuarioId, "professor52", "prof52@example.com", "senha123");
        EscolaEntity escola = escola(escolaId, "Escola Fase 52");

        when(usuarioRepository.findByUsernameIgnoreCaseAndAtivoTrue("professor52")).thenReturn(Optional.of(usuario));
        when(tenantAtivoPort.resolverTenantAtivo(usuario)).thenReturn(new TenantAtivoResumo(
                escolaId,
                "Escola Fase 52",
                OrigemTenantAtivo.USUARIO_ESCOLA));
        when(tenantAtivoPort.carregarEscola(escolaId)).thenReturn(escola);
        when(professorRepository.findByUsuario_IdAndPessoa_Escola_Id(usuarioId, escolaId)).thenReturn(Optional.empty());
        when(professorRepository.findAtivoByPessoaEmailIgnoreCaseAndEscolaId("prof52@example.com", escolaId))
                .thenReturn(Optional.of(br.com.escola.professor.adapter.out.persistence.entity.ProfessorEntity.builder()
                        .id(professorId)
                        .build()));
        when(usuarioRepository.findPerfisByIdUsuario(usuarioId)).thenReturn(List.of("PROFESSOR"));
        when(usuarioRepository.findPermissoesByIdUsuario(usuarioId)).thenReturn(List.of("PLANEJAMENTO_LEITURA"));

        var resumo = service.autenticar("professor52", "senha123");

        assertThat(resumo.usuarioId()).isEqualTo(usuarioId);
        assertThat(resumo.professorId()).isEqualTo(professorId);
        assertThat(resumo.escolaId()).isEqualTo(escolaId);
        assertThat(resumo.escolaNome()).isEqualTo("Escola Fase 52");
        assertThat(resumo.username()).isEqualTo("professor52");
        assertThat(resumo.perfis()).containsExactly("PROFESSOR");
        assertThat(resumo.permissoes()).containsExactly("PLANEJAMENTO_LEITURA");
        assertThat(resumo.tokenType()).isEqualTo("Bearer");
        assertThat(resumo.accessToken()).isNotBlank();
        assertThat(resumo.refreshToken()).isNotBlank();
        verify(sessaoRepository).save(any(SessaoAutenticacaoEntity.class));
    }

    @Test
    void deveResolverContextoAtualComPerfisEPermissoesPelaFronteiraInterna() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UsuarioEntity usuario = usuario(usuarioId, "admin52", "admin52@example.com", "hash");
        EscolaEntity escola = escola(escolaId, "Escola Contexto");
        SessaoAutenticacaoEntity sessao = new SessaoAutenticacaoEntity(
                usuario,
                escola,
                "refresh-hash",
                "access-hash",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusMinutes(30));

        when(sessaoRepository.findByAccessTokenHashAndRevogadoFalseAndAccessExpiraEmAfter(anyString(), any()))
                .thenReturn(Optional.of(sessao));
        when(tenantAtivoPort.resolverTenantDaSessaoOuUsuario(usuario, escolaId)).thenReturn(new TenantAtivoResumo(
                escolaId,
                "Escola Contexto",
                OrigemTenantAtivo.ESCOLA_SESSAO));
        when(usuarioRepository.findPerfisByIdUsuario(usuarioId)).thenReturn(List.of("ADMIN"));
        when(usuarioRepository.findPermissoesByIdUsuario(usuarioId)).thenReturn(List.of("USUARIO_LEITURA", "USUARIO_ESCRITA"));

        var contexto = service.resolverContextoAtual("access-token-valido");

        assertThat(contexto.usuarioId()).isEqualTo(usuarioId);
        assertThat(contexto.escolaId()).isEqualTo(escolaId);
        assertThat(contexto.escolaNome()).isEqualTo("Escola Contexto");
        assertThat(contexto.username()).isEqualTo("admin52");
        assertThat(contexto.perfis()).containsExactly("ADMIN");
        assertThat(contexto.permissoes()).containsExactly("USUARIO_LEITURA", "USUARIO_ESCRITA");
    }

    @Test
    void deveSelecionarEscolaAtivaQuandoUsuarioPossuirVinculo() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaAtualId = UUID.randomUUID();
        UUID novaEscolaId = UUID.randomUUID();
        UsuarioEntity usuario = usuario(usuarioId, "admin52", "admin52@example.com", "hash");
        EscolaEntity escolaAtual = escola(escolaAtualId, "Escola Atual");
        EscolaEntity novaEscola = escola(novaEscolaId, "Escola Nova");
        SessaoAutenticacaoEntity sessao = new SessaoAutenticacaoEntity(
                usuario,
                escolaAtual,
                "refresh-hash",
                "access-hash",
                LocalDateTime.now().plusDays(1),
                LocalDateTime.now().plusMinutes(30));

        when(sessaoRepository.findByAccessTokenHashAndRevogadoFalseAndAccessExpiraEmAfter(anyString(), any()))
                .thenReturn(Optional.of(sessao));
        when(usuarioEscolaPort.usuarioTemVinculo(usuarioId, novaEscolaId)).thenReturn(true);
        when(tenantAtivoPort.carregarEscola(novaEscolaId)).thenReturn(novaEscola);
        when(usuarioRepository.findPerfisByIdUsuario(usuarioId)).thenReturn(List.of("ADMIN"));
        when(usuarioRepository.findPermissoesByIdUsuario(usuarioId)).thenReturn(List.of("USUARIO_LEITURA"));

        var contexto = service.selecionarEscolaAtiva("access-token-valido", novaEscolaId);

        assertThat(contexto.escolaId()).isEqualTo(novaEscolaId);
        assertThat(contexto.escolaNome()).isEqualTo("Escola Nova");
        assertThat(sessao.getEscola().getId()).isEqualTo(novaEscolaId);
        verify(sessaoRepository).save(sessao);
    }

    private UsuarioEntity usuario(UUID id, String username, String email, String senhaHash) {
        PerfilEntity perfil = PerfilEntity.builder()
                .id(UUID.randomUUID())
                .codigo("ADMIN")
                .nome("Administrador")
                .permissoes(Set.of())
                .build();
        return UsuarioEntity.builder()
                .id(id)
                .username(username)
                .nome(username)
                .email(email)
                .senhaHash(senhaHash)
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .perfis(Set.of(perfil))
                .build();
    }

    private EscolaEntity escola(UUID id, String nome) {
        EscolaEntity escola = new EscolaEntity();
        escola.setId(id);
        escola.setNome(nome);
        escola.setAtivo(true);
        escola.prePersist();
        return escola;
    }
}
