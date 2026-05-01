package br.com.projeto.piloto.application.usecase;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.piloto.domain.model.AplicativosModel;
import br.com.projeto.piloto.domain.port.outbound.AplicativosRepositoryPort;

@ExtendWith(MockitoExtension.class)
class AplicativosInteractorTest {

    @Mock
    private AplicativosRepositoryPort repository;

    @InjectMocks
    private AplicativosInteractor service;

    @Test
    @DisplayName("Deve listar todos os aplicativos")
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new AplicativosModel()));
        List<AplicativosModel> result = service.listAll();
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Deve buscar por ID")
    void deveBuscarPorId() {
        when(repository.findById(1L)).thenReturn(Optional.of(new AplicativosModel()));
        assertTrue(service.findById(1L).isPresent());
    }

    @Test
    @DisplayName("Deve criar um aplicativo com timestamps")
    void deveCriarAplicativo() {
        AplicativosModel model = AplicativosModel.builder().nmAplicativo("Test").build();
        when(repository.save(any(AplicativosModel.class))).thenReturn(model);

        AplicativosModel result = service.create(model);

        assertNotNull(result.getCriadoEm());
        assertNotNull(result.getAtualizadoEm());
        verify(repository).save(model);
    }

    @Test
    @DisplayName("Deve atualizar aplicativo com sucesso")
    void deveAtualizarComSucesso() {
        Long id = 1L;
        AplicativosModel existing = AplicativosModel.builder().id(id).nmAplicativo("Velho").build();
        AplicativosModel updateData = AplicativosModel.builder().nmAplicativo("Novo").flAtivo("S").build();

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(AplicativosModel.class))).thenReturn(existing);

        AplicativosModel result = service.update(id, updateData);

        assertEquals("Novo", result.getNmAplicativo());
        assertNotNull(result.getAtualizadoEm());
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar ID inexistente")
    void deveErroAoAtualizarInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, new AplicativosModel()));
    }

    @Test
    @DisplayName("Deve deletar por ID")
    void deveDeletar() {
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve verificar se existe por nome")
    void deveVerificarSeExisteNome() {
        when(repository.existsByNome("App")).thenReturn(true);
        assertTrue(service.existsByNome("App"));
    }

    @Test
    @DisplayName("Deve listar apenas aplicativos ativos")
    void deveListarAtivos() {
        when(repository.findByAtivo("S")).thenReturn(List.of(new AplicativosModel()));
        List<AplicativosModel> result = service.listAtivos();
        assertFalse(result.isEmpty());
        verify(repository).findByAtivo("S");
    }
}
