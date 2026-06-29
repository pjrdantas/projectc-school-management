package br.com.escola.institucional.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.escola.institucional.adapter.out.persistence.entity.UsuarioEscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.UsuarioEscolaJpaRepository;

@ExtendWith(MockitoExtension.class)
class UsuarioEscolaServiceTest {

    @Mock
    private UsuarioEscolaJpaRepository usuarioEscolaJpaRepository;

    @Test
    void deveCriarVinculoQuandoAindaNaoExistir() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UsuarioEscolaService service = new UsuarioEscolaService(usuarioEscolaJpaRepository);

        when(usuarioEscolaJpaRepository.existsByUsuario_IdAndEscola_Id(usuarioId, escolaId)).thenReturn(false);

        service.garantirVinculo(usuarioId, escolaId);

        ArgumentCaptor<UsuarioEscolaEntity> captor = ArgumentCaptor.forClass(UsuarioEscolaEntity.class);
        verify(usuarioEscolaJpaRepository).save(captor.capture());
        assertThat(captor.getValue().getUsuario().getId()).isEqualTo(usuarioId);
        assertThat(captor.getValue().getEscola().getId()).isEqualTo(escolaId);
    }

    @Test
    void naoDeveDuplicarVinculoExistente() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UsuarioEscolaService service = new UsuarioEscolaService(usuarioEscolaJpaRepository);

        when(usuarioEscolaJpaRepository.existsByUsuario_IdAndEscola_Id(usuarioId, escolaId)).thenReturn(true);

        service.garantirVinculo(usuarioId, escolaId);

        verify(usuarioEscolaJpaRepository, never()).save(any());
    }

    @Test
    void deveListarEscolasDoUsuario() {
        UUID usuarioId = UUID.randomUUID();
        List<UUID> escolaIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        UsuarioEscolaService service = new UsuarioEscolaService(usuarioEscolaJpaRepository);

        when(usuarioEscolaJpaRepository.findEscolaIdsByUsuarioId(usuarioId)).thenReturn(escolaIds);

        assertThat(service.listarEscolasDoUsuario(usuarioId)).containsExactlyElementsOf(escolaIds);
    }

    @Test
    void deveInformarQuandoUsuarioPossuirVinculo() {
        UUID usuarioId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        UsuarioEscolaService service = new UsuarioEscolaService(usuarioEscolaJpaRepository);

        when(usuarioEscolaJpaRepository.existsByUsuario_IdAndEscola_Id(usuarioId, escolaId)).thenReturn(true);

        assertThat(service.usuarioTemVinculo(usuarioId, escolaId)).isTrue();
    }
}
