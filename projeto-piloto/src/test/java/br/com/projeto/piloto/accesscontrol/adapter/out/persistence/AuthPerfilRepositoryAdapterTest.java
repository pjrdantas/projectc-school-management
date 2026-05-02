package br.com.projeto.piloto.accesscontrol.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPerfil;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.gateway.AuthPerfilRepositoryAdapter;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.repository.SpringAuthPerfilRepository;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;

@ExtendWith(MockitoExtension.class)
class AuthPerfilRepositoryAdapterTest {

	@Mock
	private SpringAuthPerfilRepository repository;

	@InjectMocks
	private AuthPerfilRepositoryAdapter adapter;

	@Test
	@DisplayName("Deve listar todos com sucesso")
	void listAllSucesso() {
		when(repository.findAll()).thenReturn(List.of(new AuthPerfil()));
		List<AuthPerfilModel> result = adapter.listAll();
		assertNotNull(result);
		assertFalse(result.isEmpty());
	}

	@Test
	@DisplayName("Deve buscar por ID com sucesso")
	void findByIdSucesso() {
		AuthPerfil entity = new AuthPerfil();
		entity.setId(1L);
		when(repository.findById(1L)).thenReturn(Optional.of(entity));

		Optional<AuthPerfilModel> result = adapter.findById(1L);

		assertTrue(result.isPresent());
		assertThrows(NullPointerException.class, () -> adapter.findById(null));
	}

	@Test
	@DisplayName("Deve criar perfil com sucesso")
	@SuppressWarnings("null")
	void createSucesso() {
		AuthPerfilModel model = AuthPerfilModel.builder().nmPerfil("ADMIN").id(1L).build();
		AuthPerfil entity = new AuthPerfil();
		when(repository.save(any(AuthPerfil.class))).thenReturn(entity);

		AuthPerfilModel result = adapter.create(model);

		assertNotNull(result);
		verify(repository).save(any(AuthPerfil.class));
	}

	@Test
	@DisplayName("Deve lançar erro ao criar com model nulo")
	@SuppressWarnings("null")
	void createErroNulo() {
		assertThrows(NullPointerException.class, () -> adapter.create(null));
	}

	@Test
	@DisplayName("Deve atualizar perfil com sucesso")
	@SuppressWarnings("null")
	void updateSucesso() {
		Long id = 1L;
		AuthPerfil existing = new AuthPerfil();
		existing.setId(id);
		AuthPerfilModel model = AuthPerfilModel.builder().nmPerfil("NOVO").build();

		when(repository.findById(id)).thenReturn(Optional.of(existing));
		when(repository.save(any(AuthPerfil.class))).thenReturn(existing);

		AuthPerfilModel result = adapter.update(id, model);

		assertNotNull(result);
		verify(repository).save(any(AuthPerfil.class));
	}

	@SuppressWarnings("null")
	@Test
	@DisplayName("Deve lançar erro ao atualizar perfil inexistente")
	void updateErroInexistente() {
		when(repository.findById(1L)).thenReturn(Optional.empty());
		AuthPerfilModel model = AuthPerfilModel.builder().nmPerfil("X").build();

		assertThrows(RuntimeException.class, () -> adapter.update(1L, model));
	}

    @Test
    @DisplayName("Deve deletar perfil por ID")
    void deleteSucesso() {
        adapter.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve lançar erro ao tentar deletar com ID nulo")
    @SuppressWarnings("null")
    void deleteErroNulo() {
        assertThrows(NullPointerException.class, () -> adapter.delete(null));
    }

	@Test
	@DisplayName("Deve verificar se existe por nome")
	void existsByNmPerfil() {
		when(repository.existsByNmPerfil("TESTE")).thenReturn(true);
		assertTrue(adapter.existsByNmPerfil("TESTE"));
	}

	@Test
	@DisplayName("Deve retornar vazio no findByNmPerfil")
	void findByNmPerfilVazio() {
		when(repository.findByNmPerfil(anyString())).thenReturn(Optional.empty());
		Optional<AuthPerfilModel> result = adapter.findByNmPerfil("QUALQUER");
		assertTrue(result.isEmpty());
	}
}
