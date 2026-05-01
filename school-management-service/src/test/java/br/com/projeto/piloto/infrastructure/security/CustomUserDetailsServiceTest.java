package br.com.projeto.piloto.infrastructure.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPerfil;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthPermissao;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.domain.port.outbound.AuthUsuarioRepositoryPort;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private AuthUsuarioRepositoryPort userRepository;

    @InjectMocks
    private CustomUserDetailsService service;

    @Test
    @DisplayName("Deve carregar UserDetails com Roles e Permissões corretamente")
    void deveCarregarUsuarioComAuthorities() {
        String login = "admin";
        AuthUsuario usuario = new AuthUsuario();
        usuario.setLogin(login);
        usuario.setSenha("123");
        usuario.setAtivo("S");
        AuthPermissao permissao = new AuthPermissao();
        permissao.setNmPermissao("READ_PRIVILEGE");

        AuthPerfil perfil = new AuthPerfil();
        perfil.setNmPerfil("ADMIN");
        perfil.setPermissoes(Set.of(permissao));

        usuario.setPerfis(Set.of(perfil));

        when(userRepository.findByLogin(login)).thenReturn(Optional.of(usuario));
        UserDetails result = service.loadUserByUsername(login);
        assertNotNull(result);
        assertEquals(login, result.getUsername());
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(result.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("READ_PRIVILEGE")));
    }

    @Test
    @DisplayName("Cobre orElseThrow: Deve lançar erro quando usuário não existe")
    void deveLancarErroUsuarioNaoEncontrado() {
        when(userRepository.findByLogin("inexistente")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("inexistente"));
    }

    @Test
    @DisplayName("Cobre if(!'S'): Deve lançar erro quando usuário está inativo")
    void deveLancarErroUsuarioInativo() {
        AuthUsuario usuarioInativo = new AuthUsuario();
        usuarioInativo.setAtivo("N");

        when(userRepository.findByLogin("inativo")).thenReturn(Optional.of(usuarioInativo));

        assertThrows(DisabledException.class, () -> service.loadUserByUsername("inativo"));
    }
}
