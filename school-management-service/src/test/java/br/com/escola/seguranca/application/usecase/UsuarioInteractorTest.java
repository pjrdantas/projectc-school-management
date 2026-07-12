package br.com.escola.seguranca.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.EscolaJpaRepository;
import br.com.escola.institucional.application.port.internal.UsuarioEscolaPort;
import br.com.escola.institucional.application.service.EscolaTenantService;
import br.com.escola.seguranca.adapter.out.persistence.entity.PerfilEntity;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.mapper.UsuarioMapper;
import br.com.escola.seguranca.application.port.out.UsuarioRepositoryPort;
import br.com.escola.seguranca.domain.model.PerfilModel;
import br.com.escola.seguranca.domain.model.UsuarioModel;

@ExtendWith(MockitoExtension.class)
class UsuarioInteractorTest {

    @Mock
    private UsuarioRepositoryPort repository;

    @Mock
    private UsuarioMapper mapper;

    @Mock
    private EscolaJpaRepository escolaJpaRepository;

    @Mock
    private EscolaTenantService escolaTenantService;

    @Mock
    private UsuarioEscolaPort usuarioEscolaPort;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void deveSincronizarUsuarioEscolaAoCriarUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        EscolaEntity escola = escola(escolaId, "Escola A");
        UsuarioModel model = usuarioModel(escolaId);
        UsuarioEntity entity = usuarioEntity(usuarioId, escolaId);
        UsuarioEntity saved = usuarioEntity(usuarioId, escolaId);
        UsuarioInteractor interactor = new UsuarioInteractor(
                repository,
                mapper,
                escolaJpaRepository,
                escolaTenantService,
                usuarioEscolaPort,
                passwordEncoder);

        when(repository.findByUsername(model.getUsername())).thenReturn(Optional.empty());
        when(repository.existsByEmail(model.getEmail())).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash");
        when(mapper.toEntity(any())).thenReturn(entity);
        when(repository.save(entity)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(model);
        when(escolaJpaRepository.findById(escolaId)).thenReturn(Optional.of(escola));

        interactor.create(model);

        verify(usuarioEscolaPort).garantirVinculo(usuarioId, escolaId);
        ArgumentCaptor<UsuarioModel> captor = ArgumentCaptor.forClass(UsuarioModel.class);
        verify(mapper).toEntity(captor.capture());
        assertThat(captor.getValue().getSenhaHash()).isEqualTo("hash");
    }

    @Test
    void deveSincronizarUsuarioEscolaAoAtualizarUsuario() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        EscolaEntity escola = escola(escolaId, "Escola B");
        UsuarioModel model = usuarioModel(escolaId);
        UsuarioEntity existing = usuarioEntity(usuarioId, escolaId);
        UsuarioEntity saved = usuarioEntity(usuarioId, escolaId);
        UsuarioInteractor interactor = new UsuarioInteractor(
                repository,
                mapper,
                escolaJpaRepository,
                escolaTenantService,
                usuarioEscolaPort,
                passwordEncoder);

        when(repository.findById(usuarioId)).thenReturn(Optional.of(existing));
        when(repository.existsByUsernameAndIdNot(model.getUsername(), usuarioId)).thenReturn(false);
        when(repository.existsByEmailAndIdNot(model.getEmail(), usuarioId)).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("hash-atualizado");
        when(mapper.toEntity(any())).thenReturn(usuarioEntity(usuarioId, escolaId));
        when(repository.save(existing)).thenReturn(saved);
        when(mapper.toDomain(saved)).thenReturn(model);
        when(escolaJpaRepository.findById(escolaId)).thenReturn(Optional.of(escola));

        interactor.update(usuarioId, model);

        verify(usuarioEscolaPort).garantirVinculo(usuarioId, escolaId);
        assertThat(existing.getSenhaHash()).isEqualTo("hash-atualizado");
    }

    private UsuarioModel usuarioModel(UUID escolaId) {
        return UsuarioModel.builder()
                .id(UUID.randomUUID())
                .username("usuario52")
                .nome("Usuario 52")
                .email("usuario52@example.com")
                .senhaHash("senha123")
                .ativo(true)
                .escolaId(escolaId)
                .createdAt(LocalDateTime.now())
                .perfis(Set.of(PerfilModel.builder()
                        .id(UUID.randomUUID())
                        .codigo("ADMIN")
                        .nome("Administrador")
                        .build()))
                .build();
    }

    private UsuarioEntity usuarioEntity(UUID usuarioId, UUID escolaId) {
        UsuarioEntity entity = UsuarioEntity.builder()
                .id(usuarioId)
                .username("usuario52")
                .nome("Usuario 52")
                .email("usuario52@example.com")
                .senhaHash("hash")
                .ativo(true)
                .createdAt(LocalDateTime.now())
                .perfis(new HashSet<>(Set.of(PerfilEntity.builder()
                        .id(UUID.randomUUID())
                        .codigo("ADMIN")
                        .nome("Administrador")
                        .build())))
                .build();
        entity.setEscola(escola(escolaId, "Escola"));
        return entity;
    }

    private EscolaEntity escola(UUID escolaId, String nome) {
        EscolaEntity escola = new EscolaEntity();
        escola.setId(escolaId);
        escola.setNome(nome);
        escola.setAtivo(true);
        escola.prePersist();
        return escola;
    }
}
