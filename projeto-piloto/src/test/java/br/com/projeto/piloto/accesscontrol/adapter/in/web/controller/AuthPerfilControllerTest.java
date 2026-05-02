
package br.com.projeto.piloto.accesscontrol.adapter.in.web.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPerfilRequestDTO;
import br.com.projeto.piloto.accesscontrol.adapter.in.web.dto.AuthPerfilResponseDTO;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper.AuthPerfilMapper;
import br.com.projeto.piloto.accesscontrol.application.port.in.AuthPerfilUseCase;
import br.com.projeto.piloto.accesscontrol.application.port.in.AuthPermissaoUseCase;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPermissaoModel;
import br.com.projeto.piloto.shared.exception.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;

class AuthPerfilControllerTest {

    @Mock
    private AuthPerfilUseCase authPerfilUseCase;
    @Mock
    private AuthPermissaoUseCase authPermissaoUseCase;
    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private AuthPerfilController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(request.getRequestURI()).thenReturn("/api/perfis");
    }

    @Test
    void create_PerfilJaExiste() {
        AuthPerfilRequestDTO dto = mock(AuthPerfilRequestDTO.class);
        when(dto.nmPerfil()).thenReturn("ADMIN");
        when(authPerfilUseCase.existsByNmPerfil("ADMIN")).thenReturn(true);

        ResponseEntity<?> response = controller.create(dto, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    void create_Sucesso() {
        Set<Long> permissoesIds = Set.of(1L);
        AuthPerfilRequestDTO dto = mock(AuthPerfilRequestDTO.class);
        when(dto.nmPerfil()).thenReturn("ADMIN");
        when(dto.permissoesIds()).thenReturn(permissoesIds);

        AuthPermissaoModel permissao = mock(AuthPermissaoModel.class);
        when(authPerfilUseCase.existsByNmPerfil("ADMIN")).thenReturn(false);
        when(authPermissaoUseCase.findById(1L)).thenReturn(Optional.of(permissao));

        AuthPerfilModel domain = mock(AuthPerfilModel.class);
        AuthPerfilModel created = mock(AuthPerfilModel.class);

        try (MockedStatic<AuthPerfilMapper> mapper = mockStatic(AuthPerfilMapper.class)) {
            mapper.when(() -> AuthPerfilMapper.toDomain(dto, Set.of(permissao))).thenReturn(domain);
            when(authPerfilUseCase.create(domain)).thenReturn(created);
            AuthPerfilResponseDTO responseDTO = mock(AuthPerfilResponseDTO.class);
            mapper.when(() -> AuthPerfilMapper.toResponse(created)).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.create(dto, request);

            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertEquals(responseDTO, response.getBody());
        }
    }

    @Test
    void create_PermissaoNaoEncontrada() {
        Set<Long> permissoesIds = Set.of(2L);
        AuthPerfilRequestDTO dto = mock(AuthPerfilRequestDTO.class);
        when(dto.nmPerfil()).thenReturn("ADMIN");
        when(dto.permissoesIds()).thenReturn(permissoesIds);

        when(authPerfilUseCase.existsByNmPerfil("ADMIN")).thenReturn(false);
        when(authPermissaoUseCase.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.create(dto, request));
    }

    @Test
    void update_PerfilNaoEncontrado() {
        AuthPerfilRequestDTO dto = mock(AuthPerfilRequestDTO.class);
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.update(1L, dto, request));
    }

    @Test
    void update_Sucesso() {
        Set<Long> permissoesIds = Set.of(1L);
        AuthPerfilRequestDTO dto = mock(AuthPerfilRequestDTO.class);
        when(dto.permissoesIds()).thenReturn(permissoesIds);

        AuthPerfilModel perfil = mock(AuthPerfilModel.class);
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.of(perfil));

        AuthPermissaoModel permissao = mock(AuthPermissaoModel.class);
        when(authPermissaoUseCase.findById(1L)).thenReturn(Optional.of(permissao));

        AuthPerfilModel domain = mock(AuthPerfilModel.class);
        AuthPerfilModel updated = mock(AuthPerfilModel.class);

        try (MockedStatic<AuthPerfilMapper> mapper = mockStatic(AuthPerfilMapper.class)) {
            mapper.when(() -> AuthPerfilMapper.toDomain(dto, Set.of(permissao))).thenReturn(domain);
            when(authPerfilUseCase.update(1L, domain)).thenReturn(updated);
            AuthPerfilResponseDTO responseDTO = mock(AuthPerfilResponseDTO.class);
            mapper.when(() -> AuthPerfilMapper.toResponse(updated)).thenReturn(responseDTO);

            ResponseEntity<?> response = controller.update(1L, dto, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(responseDTO, response.getBody());
        }
    }

    @Test
    void update_PermissaoNaoEncontrada() {
        Set<Long> permissoesIds = Set.of(2L);
        AuthPerfilRequestDTO dto = mock(AuthPerfilRequestDTO.class);
        when(dto.permissoesIds()).thenReturn(permissoesIds);

        AuthPerfilModel perfil = mock(AuthPerfilModel.class);
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.of(perfil));
        when(authPermissaoUseCase.findById(2L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> controller.update(1L, dto, request));
    }

    @Test
    void delete_Sucesso() {
        AuthPerfilModel perfil = mock(AuthPerfilModel.class);
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.of(perfil));

        ResponseEntity<?> response = controller.delete(1L, request);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(authPerfilUseCase).delete(1L);
    }

    @Test
    void delete_NaoEncontrado() {
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.delete(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    void findById_Sucesso() {
        AuthPerfilModel perfil = mock(AuthPerfilModel.class);
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.of(perfil));
        AuthPerfilResponseDTO dto = mock(AuthPerfilResponseDTO.class);

        try (MockedStatic<AuthPerfilMapper> mapper = mockStatic(AuthPerfilMapper.class)) {
            mapper.when(() -> AuthPerfilMapper.toResponse(perfil)).thenReturn(dto);

            ResponseEntity<?> response = controller.findById(1L, request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(dto, response.getBody());
        }
    }

    @Test
    void findById_NaoEncontrado() {
        when(authPerfilUseCase.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = controller.findById(1L, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }

    @Test
    void listAll_Sucesso() {
        AuthPerfilModel perfil = mock(AuthPerfilModel.class);
        when(authPerfilUseCase.listAll()).thenReturn(List.of(perfil));
        AuthPerfilResponseDTO dto = mock(AuthPerfilResponseDTO.class);

        try (MockedStatic<AuthPerfilMapper> mapper = mockStatic(AuthPerfilMapper.class)) {
            mapper.when(() -> AuthPerfilMapper.toResponse(perfil)).thenReturn(dto);

            ResponseEntity<?> response = controller.listAll(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals(List.of(dto), response.getBody());
        }
    }

    @Test
    void listAll_Vazio() {
        when(authPerfilUseCase.listAll()).thenReturn(List.of());

        ResponseEntity<?> response = controller.listAll(request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof ErrorResponse);
    }
}
