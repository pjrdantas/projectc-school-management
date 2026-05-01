package br.com.projeto.piloto.application.usecase;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.piloto.domain.model.AuthPermissaoModel;
import br.com.projeto.piloto.domain.port.outbound.AuthPermissaoRepositoryPort;

@ExtendWith(MockitoExtension.class)
class AuthPermissaoInteractorTest {

    @Mock
    private AuthPermissaoRepositoryPort repository;

    @InjectMocks
    private AuthPermissaoInteractor service;

    @Test
    @DisplayName("Deve listar todas as permissões")
    void deveListarTodas() {
        when(repository.findAll()).thenReturn(List.of(new AuthPermissaoModel()));
        List<AuthPermissaoModel> result = service.listAll();
        assertFalse(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve buscar permissão por ID")
    void deveBuscarPorId() {
        AuthPermissaoModel model = new AuthPermissaoModel();
        when(repository.findById(1L)).thenReturn(Optional.of(model));
        
        Optional<AuthPermissaoModel> result = service.findById(1L);
        
        assertTrue(result.isPresent());
        verify(repository).findById(1L);
    }

    @Test
    @DisplayName("Deve criar permissão com sucesso")
    void deveCriarComSucesso() {
        AuthPermissaoModel model = new AuthPermissaoModel();
        model.setNmPermissao("ROLE_ADMIN");
        
        when(repository.existsByNmPermissao("ROLE_ADMIN")).thenReturn(false);
        when(repository.save(model)).thenReturn(model);

        AuthPermissaoModel result = service.create(model);

        assertNotNull(result);
        verify(repository).save(model);
    }

    @Test
    @DisplayName("Deve lançar erro ao criar permissão duplicada")
    void deveErroAoCriarDuplicada() {
        AuthPermissaoModel model = new AuthPermissaoModel();
        model.setNmPermissao("ROLE_USER");
        
        when(repository.existsByNmPermissao("ROLE_USER")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(model));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar permissão com sucesso")
    void deveAtualizarComSucesso() {
        Long id = 1L;
        AuthPermissaoModel existente = new AuthPermissaoModel();
        AuthPermissaoModel novosDados = new AuthPermissaoModel();
        novosDados.setNmPermissao("NOVA_ROLE");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.save(any(AuthPermissaoModel.class))).thenReturn(existente);

        AuthPermissaoModel result = service.update(id, novosDados);

        assertEquals("NOVA_ROLE", result.getNmPermissao());
        verify(repository).save(existente);
    }

    @Test
    @DisplayName("Deve lançar erro ao atualizar ID inexistente")
    void deveErroAoAtualizarInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        
        assertThrows(IllegalArgumentException.class, () -> service.update(1L, new AuthPermissaoModel()));
    }

    @Test
    @DisplayName("Deve deletar permissão por ID")
    void deveDeletar() {
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve verificar se existe por nome")
    void deveVerificarSeExistePorNome() {
        when(repository.existsByNmPermissao("ROLE_TEST")).thenReturn(true);
        assertTrue(service.existsByNmPermissao("ROLE_TEST"));
    }
}
