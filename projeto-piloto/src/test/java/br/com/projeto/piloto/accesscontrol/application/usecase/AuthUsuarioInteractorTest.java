package br.com.projeto.piloto.accesscontrol.application.usecase;



import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthPerfil;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthUsuario;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper.AuthUsuarioMapper;
import br.com.projeto.piloto.accesscontrol.application.port.out.AuthUsuarioRepositoryPort;
import br.com.projeto.piloto.accesscontrol.domain.exception.UserNotFoundException;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthPerfilModel;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthUsuarioModel;

@ExtendWith(MockitoExtension.class)
class AuthUsuarioInteractorTest {

    @Mock
    private AuthUsuarioRepositoryPort repository;

    @Mock
    private AuthUsuarioMapper mapper;
    
    @Mock 
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthUsuarioInteractor service;
    private Set<AuthPerfilModel> getMockPerfisModel() {
        return Set.of(AuthPerfilModel.builder().nmPerfil("ADMIN").build());
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void deveCriarUsuarioComSucesso() {
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("admin")
                .perfis(getMockPerfisModel())
                .build();
        AuthUsuario entity = new AuthUsuario();
        
        when(repository.findByLogin("admin")).thenReturn(Optional.empty());
        when(mapper.toEntity(model)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(model);

        AuthUsuarioModel result = service.criar(model);

        assertNotNull(result);
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve lançar erro ao criar com login duplicado")
    void deveErroCriarLoginDuplicado() {
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("admin")
                .perfis(getMockPerfisModel())
                .build();
        
        when(repository.findByLogin("admin")).thenReturn(Optional.of(new AuthUsuario()));

        assertThrows(DataIntegrityViolationException.class, () -> service.criar(model));
    }

    @Test
    @DisplayName("Deve lançar erro ao validar usuário sem perfis")
    void deveErroValidarSemPerfis() {
        AuthUsuarioModel model = AuthUsuarioModel.builder().login("user").perfis(null).build();
        assertThrows(IllegalArgumentException.class, () -> service.criar(model));
        
        AuthUsuarioModel modelVazio = AuthUsuarioModel.builder().login("user").perfis(Set.of()).build();
        assertThrows(IllegalArgumentException.class, () -> service.criar(modelVazio));
    }

    @Test
    @DisplayName("Deve atualizar usuário com nova senha e perfis")
    void deveAtualizarCompleto() {
        Long id = 1L;
        String senhaPlana = "123";
        String senhaCripto = "$2a$10$hashSimulado"; 

        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("novo")
                .nome("Nome")
                .senha(senhaPlana)
                .perfis(getMockPerfisModel())
                .build();
        
        AuthUsuario existing = new AuthUsuario();
        existing.setPerfis(new HashSet<AuthPerfil>());
        
        AuthUsuario updatedEntity = new AuthUsuario();
        updatedEntity.setPerfis(new HashSet<AuthPerfil>());

 
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByDsLoginAndIdNot("novo", id)).thenReturn(false);
        when(mapper.toEntity(model)).thenReturn(updatedEntity);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDomain(existing)).thenReturn(model);
        when(passwordEncoder.encode(senhaPlana)).thenReturn(senhaCripto);

        AuthUsuarioModel result = service.atualizar(id, model);

        assertNotNull(result);
        assertEquals(senhaCripto, existing.getSenha()); 
        verify(passwordEncoder).encode(senhaPlana); 
    }


    @Test
    @DisplayName("Deve lançar erro ao atualizar ID inexistente")
    void deveErroAtualizarInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        AuthUsuarioModel model = AuthUsuarioModel.builder().perfis(getMockPerfisModel()).build();
        
        assertThrows(UserNotFoundException.class, () -> service.atualizar(1L, model));
    }

    @Test
    @DisplayName("Deve deletar com sucesso")
    void deveDeletarSucesso() {
        when(repository.existsById(1L)).thenReturn(true);
        service.deletar(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    @DisplayName("Deve buscar por ID")
    void deveBuscarPorId() {
        AuthUsuario entity = new AuthUsuario();
        when(repository.findById(1L)).thenReturn(Optional.of(entity));
        when(mapper.toDomain(entity)).thenReturn(new AuthUsuarioModel());

        assertNotNull(service.buscarPorId(1L));
    }

    @Test
    @DisplayName("Deve listar todos")
    void deveListarTodos() {
        when(repository.findAll()).thenReturn(List.of(new AuthUsuario()));
        when(mapper.toDomain(any())).thenReturn(new AuthUsuarioModel());

        List<AuthUsuarioModel> result = service.listarTodos();
        assertFalse(result.isEmpty());
    }
    
    @Test
    void deveLancarErroSeModelNulo() {
        assertThrows(NullPointerException.class, () -> service.criar(null));
    }

    @Test
    void deveLancarErroAoCriarLoginJaExistente() {
        AuthUsuarioModel model = AuthUsuarioModel.builder().login("admin").perfis(Set.of(new AuthPerfilModel())).build();
        when(repository.findByLogin("admin")).thenReturn(Optional.of(new AuthUsuario()));
        assertThrows(DataIntegrityViolationException.class, () -> service.criar(model));
    }

    @Test
    void deveAtualizarUsuarioSemAlterarSenhaSeForBranca() {
        Long id = 1L;
        AuthUsuarioModel model = AuthUsuarioModel.builder().login("u").senha(" ").perfis(Set.of(new AuthPerfilModel())).build();
        AuthUsuario entity = new AuthUsuario();
        entity.setSenha("SENHA_ANTIGA");
        entity.setPerfis(new HashSet<>());

        when(repository.findById(id)).thenReturn(Optional.of(entity));
        when(repository.existsByDsLoginAndIdNot(any(), anyLong())).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(new AuthUsuario());
        when(repository.save(any())).thenReturn(entity);

        service.atualizar(id, model);
        assertEquals("SENHA_ANTIGA", entity.getSenha()); 
    }

    @Test
    void deveLancarErroAoDeletarIdInexistente() {
        when(repository.existsById(1L)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> service.deletar(1L));
    }
    
    @Test
    @DisplayName("Cobre Linha 45 (Amarela) e 49 (Vermelha): Erro de Login Duplicado no Update")
    void deveErroAtualizarLoginDuplicado() {
        Long id = 1L;
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("duplicado")
                .perfis(getMockPerfisModel())
                .build();
        
        AuthUsuario existing = new AuthUsuario();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByDsLoginAndIdNot("duplicado", id)).thenReturn(true);

        assertThrows(DataIntegrityViolationException.class, () -> service.atualizar(id, model));
    }

    @Test
    @DisplayName("Cobre Linha 59 (Amarela): Update com senha nula ou branca")
    void deveAtualizarSemAlterarSenha() {
        Long id = 1L;
        AuthUsuarioModel modelSenhaNula = AuthUsuarioModel.builder()
                .login("user").senha(null).perfis(getMockPerfisModel()).build();
        AuthUsuarioModel modelSenhaBranca = AuthUsuarioModel.builder()
                .login("user").senha("   ").perfis(getMockPerfisModel()).build();

        AuthUsuario existing = new AuthUsuario();
        existing.setSenha("SENHA_ORIGINAL");
        existing.setPerfis(new HashSet<>());
        
        AuthUsuario entityFromMapper = new AuthUsuario();
        entityFromMapper.setPerfis(new HashSet<>());

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByDsLoginAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(mapper.toEntity(any(AuthUsuarioModel.class))).thenReturn(entityFromMapper);
        when(repository.save(any())).thenReturn(existing);
        service.atualizar(id, modelSenhaNula);
        assertEquals("SENHA_ORIGINAL", existing.getSenha());
        service.atualizar(id, modelSenhaBranca);
        assertEquals("SENHA_ORIGINAL", existing.getSenha());
    }

    @Test
    @DisplayName("Cobre Linha 73 (Amarela): Delete com sucesso")
    void deveDeletarComSucessoTotal() {
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);
        
        assertDoesNotThrow(() -> service.deletar(id));
        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Cobre Fluxo de Exceção no buscarPorId")
    void deveErroAoBuscarIdInexistente() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> service.buscarPorId(1L));
    }
    
    @Test
    @DisplayName("Cobre validar(model) nulo")
    void deveErroValidarModelNulo() {
        assertThrows(NullPointerException.class, () -> service.criar(null));
    }

    @Test
    @DisplayName("Cobre Linha 73 (Ramo Verdadeiro): Erro quando usuário não existe no delete")
    void deveLancarErroQuandoUsuarioNaoExisteNoDelete() {
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(false);
        assertThrows(UserNotFoundException.class, () -> service.deletar(id));
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Cobre Linha 73 (Ramo Falso): Sucesso quando usuário existe no delete")
    void deveDeletarQuandoUsuarioExiste() {
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);
        service.deletar(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Cobre Linha 73: Caminho de sucesso do delete (ID existe)")
    void deveDeletarComSucessoParaJacoco() {
        Long idValido = 1L;
        when(repository.existsById(eq(idValido))).thenReturn(true);
        assertDoesNotThrow(() -> service.deletar(idValido));
        verify(repository, times(1)).existsById(idValido);
        verify(repository, times(1)).deleteById(idValido);
    }

    @Test
    @DisplayName("Cobre Linha 73: Caminho de erro do delete (ID não existe)")
    void deveLancarErroParaJacoco() {
        Long idInexistente = 99L;
        when(repository.existsById(eq(idInexistente))).thenReturn(false);
        UserNotFoundException ex = assertThrows(UserNotFoundException.class, 
            () -> service.deletar(idInexistente));
        
        assertEquals("Usuário não encontrado.", ex.getMessage());
        verify(repository).existsById(idInexistente);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Delete - Cobre ramo 'id == null' (Linha 72)")
    void deletar_IdNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.deletar(null));
        verifyNoInteractions(repository);
    }

    @Test
    @DisplayName("Delete - Cobre ramo 'Usuário não encontrado' (Linha 73 - Parte 1)")
    void deletar_UsuarioNaoExiste() {
        Long id = 10L;
        when(repository.existsById(id)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> service.deletar(id));
        verify(repository).existsById(id);
        verify(repository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Delete - Cobre ramo 'Sucesso' (Linha 73 - Parte 2 e Linha 74)")
    void deletar_Sucesso() {
        Long id = 10L;
        when(repository.existsById(id)).thenReturn(true);

        assertDoesNotThrow(() -> service.deletar(id));

        verify(repository, times(1)).existsById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    @DisplayName("Deve criar usuário e codificar a senha quando fornecida")
    void deveCriarUsuarioComSenha() {
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("admin2")
                .senha("plainpass")
                .perfis(getMockPerfisModel())
                .build();
        AuthUsuario entity = new AuthUsuario();

        when(repository.findByLogin("admin2")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plainpass")).thenReturn("hashed-pass");
        when(mapper.toEntity(model)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDomain(entity)).thenReturn(model);

        AuthUsuarioModel result = service.criar(model);

        assertNotNull(result);
        verify(passwordEncoder).encode("plainpass");
        assertEquals("hashed-pass", model.getSenha());
    }

    @Test
    @DisplayName("Deve atualizar usuario definindo ativo e email")
    void deveAtualizarAtivoEmail() {
        Long id = 2L;
        AuthUsuarioModel model = AuthUsuarioModel.builder()
                .login("u2")
                .nome("Nome2")
                .ativo("S")
                .email("u2@example.com")
                .senha(null)
                .perfis(getMockPerfisModel())
                .build();

        AuthUsuario existing = new AuthUsuario();
        existing.setPerfis(new HashSet<>());

        AuthUsuario entityFromMapper = new AuthUsuario();
        entityFromMapper.setPerfis(new HashSet<>());

        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.existsByDsLoginAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(mapper.toEntity(any())).thenReturn(entityFromMapper);
        when(repository.save(existing)).thenReturn(existing);
        when(mapper.toDomain(existing)).thenReturn(model);

        AuthUsuarioModel result = service.atualizar(id, model);
        assertNotNull(result);
        assertEquals("S", existing.getAtivo());
        assertEquals("u2@example.com", existing.getEmail());
    }

    @Test
    @DisplayName("listarTodos retorna lista vazia quando não há usuários")
    void listarTodosVazio() {
        when(repository.findAll()).thenReturn(List.of());
        List<AuthUsuarioModel> result = service.listarTodos();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

}
