package br.com.projeto.piloto.application.usecase;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import br.com.projeto.piloto.adapter.in.web.exception.InvalidLoginException;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.adapter.out.jpa.mapper.AuthUsuarioMapper;
import br.com.projeto.piloto.domain.model.AuthUsuarioModel;
import br.com.projeto.piloto.domain.port.outbound.AuthUsuarioRepositoryPort;

@ExtendWith(MockitoExtension.class)
class AuthInteractorTest {

    @Mock
    private AuthUsuarioRepositoryPort usuarioRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthUsuarioMapper mapper;

    @InjectMocks
    private AuthInteractor authInteractor;

    @Nested
    @DisplayName("Testes para o método authenticate")
    class AuthenticateTests {

        @Test
        @DisplayName("Deve autenticar com sucesso quando credenciais estão corretas")
        void deveAutenticarComSucesso() {
            String login = "user";
            String senha = "password123";
            AuthUsuario usuarioEntity = new AuthUsuario();
            usuarioEntity.setSenha("encodedPassword");
            AuthUsuarioModel usuarioModel = new AuthUsuarioModel();

            when(usuarioRepo.findByLogin(login)).thenReturn(Optional.of(usuarioEntity));
            when(passwordEncoder.matches(senha, usuarioEntity.getSenha())).thenReturn(true);
            when(mapper.toDomain(usuarioEntity)).thenReturn(usuarioModel);
            AuthUsuarioModel result = authInteractor.authenticate(login, senha);
            assertNotNull(result);
            verify(usuarioRepo).findByLogin(login);
            verify(passwordEncoder).matches(senha, usuarioEntity.getSenha());
        }

        @Test
        @DisplayName("Deve lançar InvalidLoginException quando usuário não existe")
        void deveFalharUsuarioInexistente() {
            when(usuarioRepo.findByLogin("invalido")).thenReturn(Optional.empty());

            assertThrows(InvalidLoginException.class, () -> authInteractor.authenticate("invalido", "senha"));
        }

        @Test
        @DisplayName("Deve lançar InvalidLoginException quando senha está incorreta")
        void deveFalharSenhaIncorreta() {
            String login = "user";
            AuthUsuario usuarioEntity = new AuthUsuario();
            usuarioEntity.setSenha("encodedPassword");

            when(usuarioRepo.findByLogin(login)).thenReturn(Optional.of(usuarioEntity));
            when(passwordEncoder.matches("errada", usuarioEntity.getSenha())).thenReturn(false);

            assertThrows(InvalidLoginException.class, () -> authInteractor.authenticate(login, "errada"));
        }
    }

    @Nested
    @DisplayName("Testes para o método findByLogin")
    class FindByLoginTests {

        @Test
        @DisplayName("Deve retornar usuário quando login existe")
        void deveRetornarUsuario() {
            String login = "admin";
            AuthUsuario entity = new AuthUsuario();
            AuthUsuarioModel model = new AuthUsuarioModel();

            when(usuarioRepo.findByLogin(login)).thenReturn(Optional.of(entity));
            when(mapper.toDomain(entity)).thenReturn(model);

            AuthUsuarioModel result = authInteractor.findByLogin(login);

            assertNotNull(result);
            assertEquals(model, result);
        }

        @Test
        @DisplayName("Deve lançar InvalidLoginException quando login não é localizado")
        void deveFalharLoginNaoLocalizado() {
            when(usuarioRepo.findByLogin("sumiu")).thenReturn(Optional.empty());

            assertThrows(InvalidLoginException.class, () -> authInteractor.findByLogin("sumiu"));
        }
    }
}
