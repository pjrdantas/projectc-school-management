package br.com.projeto.piloto.adapter.out.jpa;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.piloto.adapter.out.jpa.entity.Aplicativo;
import br.com.projeto.piloto.adapter.out.jpa.repository.SpringDataAplicativosRepository;
import br.com.projeto.piloto.domain.model.AplicativosModel;

@ExtendWith(MockitoExtension.class)
class AplicativosRepositoryAdapterTest {

    @Mock
    private SpringDataAplicativosRepository repository;

    @InjectMocks
    private AplicativosRepositoryAdapter adapter;

    private Aplicativo createEntity() {
        Aplicativo e = new Aplicativo();
        e.setId(1L);
        e.setNmAplicativo("App");
        e.setCriadoEm(LocalDateTime.now());
        return e;
    }

    private AplicativosModel createModel() {
        return AplicativosModel.builder()
                .id(1L)
                .nmAplicativo("App")
                .build();
    }

    @Test
    @DisplayName("Deve listar todos e converter para domain")
    void findAllSucesso() {
        when(repository.findAll()).thenReturn(List.of(createEntity()));
        
        List<AplicativosModel> result = adapter.findAll();
        
        assertFalse(result.isEmpty());
        assertEquals("App", result.get(0).getNmAplicativo());
    }

    @Test
    @DisplayName("Deve buscar por ID e converter para domain")
    void findByIdSucesso() {
        when(repository.findById(1L)).thenReturn(Optional.of(createEntity()));
        
        Optional<AplicativosModel> result = adapter.findById(1L);
        
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
    }

    @Test
    @DisplayName("Cobre requireNonNull: Erro ao buscar por ID nulo")
    void findByIdErro() {
        assertThrows(NullPointerException.class, () -> adapter.findById(null));
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve salvar model convertendo para entity e voltando para domain")
    void saveSucesso() {
        AplicativosModel model = createModel();
        Aplicativo entity = createEntity();
        
        when(repository.save(any(Aplicativo.class))).thenReturn(entity);
        
        AplicativosModel result = adapter.save(model);
        
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(repository).save(any(Aplicativo.class));
    }

    @Test
    @DisplayName("Cobre requireNonNull: Erro ao salvar nulo")
    void saveErro() {
        assertThrows(NullPointerException.class, () -> adapter.save(null));
    }

    @Test
    @DisplayName("Deve deletar por ID")
    void deleteSucesso() {
        adapter.deleteById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Cobre requireNonNull: Erro ao deletar nulo")
    void deleteErro() {
        assertThrows(NullPointerException.class, () -> adapter.deleteById(null));
    }

    @Test
    @DisplayName("Deve verificar se existe por nome")
    void existsSucesso() {
        when(repository.existsByNmAplicativo("App")).thenReturn(true);
        assertTrue(adapter.existsByNome("App"));
    }

    @Test
    @DisplayName("Cobre requireNonNull: Erro ao verificar nome nulo")
    void existsErro() {
        assertThrows(NullPointerException.class, () -> adapter.existsByNome(null));
    }

    @Test
    @DisplayName("Deve buscar ativos e converter para domain")
    void findByAtivoSucesso() {
        when(repository.findByFlAtivo("S")).thenReturn(List.of(createEntity()));
        
        List<AplicativosModel> result = adapter.findByAtivo("S");
        
        assertFalse(result.isEmpty());
        verify(repository).findByFlAtivo("S");
    }
}
