package br.com.projeto.piloto.adapter.out.jpa;



import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.adapter.out.jpa.repository.SpringAuthUsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthUsuarioRepositoryAdapterTest {

    @Mock
    private SpringAuthUsuarioRepository repository;

    @InjectMocks
    private AuthUsuarioRepositoryAdapter adapter;

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void deveSalvarUsuario() {
        AuthUsuario usuario = new AuthUsuario();
        usuario.setLogin("admin");
        
        when(repository.save(usuario)).thenReturn(usuario);
        
        AuthUsuario result = adapter.save(usuario);
        
        assertNotNull(result);
        assertEquals("admin", result.getLogin());
        verify(repository).save(usuario);
    }

    @Test
    @DisplayName("Deve buscar por ID")
    void deveBuscarPorId() {
        Long id = 1L;
        AuthUsuario usuario = new AuthUsuario();
        when(repository.findById(id)).thenReturn(Optional.of(usuario));
        
        Optional<AuthUsuario> result = adapter.findById(id);
        
        assertTrue(result.isPresent());
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve buscar por login")
    void deveBuscarPorLogin() {
        String login = "user";
        when(repository.findByLogin(login)).thenReturn(Optional.of(new AuthUsuario()));
        
        Optional<AuthUsuario> result = adapter.findByLogin(login);
        
        assertTrue(result.isPresent());
        verify(repository).findByLogin(login);
    }

    @Test
    @DisplayName("Deve listar todos")
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new AuthUsuario()));
        
        List<AuthUsuario> result = adapter.findAll();
        
        assertFalse(result.isEmpty());
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve deletar por ID")
    void deveDeletarPorId() {
        Long id = 1L;
        adapter.deleteById(id);
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Deve verificar se existe por login e ID diferente")
    void deveVerificarDuplicidadeLogin() {
        when(repository.existsByLoginAndIdNot("login", 1L)).thenReturn(true);
        assertTrue(adapter.existsByDsLoginAndIdNot("login", 1L));
    }

    @Test
    @DisplayName("Deve verificar se existe por ID")
    void deveVerificarSeExistePorId() {
        when(repository.existsById(1L)).thenReturn(true);
        assertTrue(adapter.existsById(1L));
    }

    @Test
    @DisplayName("Cobre as exceções de Objects.requireNonNull")
    void deveLancarExcecaoParaParametrosNulos() {
        assertAll("Validações de NullPointerException",
            () -> assertThrows(NullPointerException.class, () -> adapter.save(null)),
            () -> assertThrows(NullPointerException.class, () -> adapter.findById(null)),
            () -> assertThrows(NullPointerException.class, () -> adapter.deleteById(null)),
            () -> assertThrows(NullPointerException.class, () -> adapter.existsById(null))
        );
    }
}
