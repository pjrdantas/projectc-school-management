package br.com.projeto.piloto.accesscontrol.application.usecase;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
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

import br.com.projeto.piloto.accesscontrol.application.port.out.AuthPerfilRepositoryPort;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.aplicativos.application.port.out.AplicativosRepositoryPort;

@ExtendWith(MockitoExtension.class)
class AuthPerfilInteractorTest {

    @Mock private AuthPerfilRepositoryPort repository;
    @Mock private AplicativosRepositoryPort aplicativosRepository;
    @InjectMocks private AuthPerfilInteractor service;

    @SuppressWarnings("null")
	@Test
    @DisplayName("Erro se nome de perfil já existe")
    void createNomeExiste() {
        AuthPerfilModel domain = AuthPerfilModel.builder().nmPerfil("EXISTE").build();
        when(repository.existsByNmPerfil("EXISTE")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(domain));

        verify(repository, never()).create(any());
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Sucesso ao criar perfil")
    void createSucessoTotal() {
        AuthPerfilModel domain = AuthPerfilModel.builder()
                .nmPerfil("NOVO")
                .build();

        when(repository.existsByNmPerfil("NOVO")).thenReturn(false);

        when(repository.create(any())).thenReturn(domain);

        AuthPerfilModel result = service.create(domain);
        
        assertNotNull(result);
        verify(repository).create(any());
    }

    @Test
    @DisplayName("Sucesso ao deletar perfil")
    void deleteSucesso() {
        assertDoesNotThrow(() -> service.delete(1L));
        verify(repository).delete(1L);
    }
    
    @Test
    @DisplayName("Deve validar o Builder e o valor Default do Set")
    void deveValidarBuilderEDefault() {
        AuthPerfilModel model = AuthPerfilModel.builder()
                .id(1L)
                .nmPerfil("USER")
                .build();

        assertNotNull(model);
        assertEquals("USER", model.getNmPerfil());
        assertNotNull(model.getPermissoes());  
        assertTrue(model.getPermissoes().isEmpty());
    }

    @Test
    @DisplayName("create lança NPE quando domain é nulo")
    void createNullDomain() {
        assertThrows(NullPointerException.class, () -> service.create(null));
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("update com sucesso quando não existe outro perfil com mesmo nome")
    void updateSuccess() {
        Long id = 2L;
        AuthPerfilModel domain = AuthPerfilModel.builder().id(id).nmPerfil("UPDATE").build();

        when(repository.findByNmPerfil("UPDATE")).thenReturn(Optional.empty());
        when(repository.update(id, domain)).thenReturn(domain);

        AuthPerfilModel result = service.update(id, domain);
        assertNotNull(result);
        assertEquals("UPDATE", result.getNmPerfil());
    }

    @Test
    @DisplayName("update lança NPE quando id é nulo")
    void updateIdNull() {
        AuthPerfilModel domain = AuthPerfilModel.builder().id(1L).nmPerfil("X").build();
        assertThrows(NullPointerException.class, () -> service.update(null, domain));
    }

    @Test
    @DisplayName("update lança NPE quando domain é nulo")
    void updateDomainNull() {
        assertThrows(NullPointerException.class, () -> service.update(1L, null));
    }

    @Test
    @DisplayName("update lança IllegalArgumentException quando outro perfil com mesmo nome existe")
    void updateNomeConflito() {
        Long id = 5L;
        AuthPerfilModel domain = AuthPerfilModel.builder().id(id).nmPerfil("CONFLITO").build();

        when(repository.findByNmPerfil("CONFLITO")).thenReturn(Optional.of(AuthPerfilModel.builder().id(999L).nmPerfil("CONFLITO").build()));

        assertThrows(IllegalArgumentException.class, () -> service.update(id, domain));
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("update não lança quando findByNmPerfil retorna mesmo id (sem conflito)")
    void updateSameIdNoConflict() {
        Long id = 6L;
        AuthPerfilModel domain = AuthPerfilModel.builder().id(id).nmPerfil("SAME").build();

        when(repository.findByNmPerfil("SAME")).thenReturn(Optional.of(AuthPerfilModel.builder().id(id).nmPerfil("SAME").build()));
        when(repository.update(id, domain)).thenReturn(domain);

        AuthPerfilModel result = service.update(id, domain);
        assertNotNull(result);
        assertEquals(id, result.getId());
    }

    @Test
    @DisplayName("findById delega para repository e retorna Optional")
    void findByIdDelegation() {
        AuthPerfilModel model = AuthPerfilModel.builder().id(20L).nmPerfil("X").build();
        when(repository.findById(20L)).thenReturn(Optional.of(model));

        Optional<AuthPerfilModel> result = service.findById(20L);
        assertTrue(result.isPresent());
        assertEquals(20L, result.get().getId());
    }

    @Test
    @DisplayName("listAll delega para repository")
    void listAllDelegation() {
        AuthPerfilModel model = AuthPerfilModel.builder().id(30L).nmPerfil("L").build();
        when(repository.listAll()).thenReturn(List.of(model));

        List<AuthPerfilModel> result = service.listAll();
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("L", result.get(0).getNmPerfil());
    }

    @Test
    @DisplayName("existsByNmPerfil delega para repository (true)")
    void existsByNmPerfil_true() {
        when(repository.existsByNmPerfil("A")).thenReturn(true);
        assertTrue(service.existsByNmPerfil("A"));
    }

    @Test
    @DisplayName("existsByNmPerfil delega para repository (false)")
    void existsByNmPerfil_false() {
        when(repository.existsByNmPerfil("B")).thenReturn(false);
        assertEquals(false, service.existsByNmPerfil("B"));
    }

    @Test
    @DisplayName("delete lança IllegalArgumentException quando id é nulo")
    void deleteIdNull() {
        assertThrows(IllegalArgumentException.class, () -> service.delete(null));
    }
}
