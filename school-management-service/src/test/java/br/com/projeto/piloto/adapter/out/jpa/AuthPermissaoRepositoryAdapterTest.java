package br.com.projeto.piloto.adapter.out.jpa;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import java.lang.reflect.Method;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPermissao;
import br.com.projeto.piloto.adapter.out.jpa.repository.SpringAuthPermissaoRepository;
import br.com.projeto.piloto.domain.model.AuthPermissaoModel;

@ExtendWith(MockitoExtension.class)
class AuthPermissaoRepositoryAdapterTest {

    @Mock
    private SpringAuthPermissaoRepository permissaoRepository;

    @InjectMocks
    private AuthPermissaoRepositoryAdapter adapter;

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve salvar permissão com sucesso")
    void saveSucesso() {
        AuthPermissaoModel model = AuthPermissaoModel.builder().id(1L).nmPermissao("READ").build();
        AuthPermissao entity = new AuthPermissao();
        entity.setId(1L);
        entity.setNmPermissao("READ");

        when(permissaoRepository.save(any(AuthPermissao.class))).thenReturn(entity);

        AuthPermissaoModel result = adapter.save(model);

        assertNotNull(result);
        assertEquals("READ", result.getNmPermissao());
        verify(permissaoRepository).save(any(AuthPermissao.class));
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Cobre Linha 24 e 33: Deve retornar null se model ou entity forem nulos")
    void testeMapeamentoNulo() {
        when(permissaoRepository.save(any())).thenReturn(null);
        assertNull(adapter.save(AuthPermissaoModel.builder().build()));
    }

    @Test
    @DisplayName("Deve buscar por ID com sucesso")
    void findByIdSucesso() {
        AuthPermissao entity = new AuthPermissao();
        entity.setId(1L);
        when(permissaoRepository.findById(1L)).thenReturn(Optional.of(entity));

        Optional<AuthPermissaoModel> result = adapter.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Deve listar todos com sucesso")
    void findAllSucesso() {
        when(permissaoRepository.findAll()).thenReturn(List.of(new AuthPermissao()));
        List<AuthPermissaoModel> result = adapter.findAll();
        assertFalse(result.isEmpty());
    }

    @Test
    @DisplayName("Deve deletar por ID")
    void deleteSucesso() {
        adapter.deleteById(1L);
        verify(permissaoRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve verificar se existe por nome")
    void existsSucesso() {
        when(permissaoRepository.existsByNmPermissao("WRITE")).thenReturn(true);
        assertTrue(adapter.existsByNmPermissao("WRITE"));
    }

    @Test
    @DisplayName("Cobre requireNonNull: Deve lançar erro para parâmetros nulos")
    void testeValidacoesNulas() {
        assertThrows(NullPointerException.class, () -> adapter.save(null));
        assertThrows(NullPointerException.class, () -> adapter.findById(null));
        assertThrows(NullPointerException.class, () -> adapter.deleteById(null));
        assertThrows(NullPointerException.class, () -> adapter.existsByNmPermissao(null));
    }
    
    @Test
    @DisplayName("Cobre Linha 25: Ramo nulo do método privado toEntity")
    void deveCobrirToEntityNulo() throws Exception {
        Method method = AuthPermissaoRepositoryAdapter.class.getDeclaredMethod("toEntity", AuthPermissaoModel.class);
        method.setAccessible(true);
        Object result = method.invoke(adapter, (Object) null);
        assertNull(result, "O mapeamento de model nulo deve retornar null");
    }

    @Test
    @DisplayName("Cobre Linha 34: Ramo nulo do método privado toDomain")
    void deveCobrirToDomainNulo() throws Exception {
        Method method = AuthPermissaoRepositoryAdapter.class.getDeclaredMethod("toDomain", AuthPermissao.class);
        method.setAccessible(true);
        Object result = method.invoke(adapter, (Object) null);
        assertNull(result, "O mapeamento de entidade nula deve retornar null");
    }    
}
