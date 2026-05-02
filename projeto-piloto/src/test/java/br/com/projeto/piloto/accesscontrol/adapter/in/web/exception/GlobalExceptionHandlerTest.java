package br.com.projeto.piloto.accesscontrol.adapter.in.web.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import br.com.projeto.piloto.accesscontrol.domain.exception.DomainException;
import br.com.projeto.piloto.accesscontrol.domain.exception.InvalidLoginException;
import br.com.projeto.piloto.accesscontrol.domain.exception.UserNotFoundException;
import br.com.projeto.piloto.shared.exception.ErrorResponse;
import br.com.projeto.piloto.shared.exception.GlobalExceptionHandler;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler handler;

    @Mock
    private HttpServletRequest request;


    @SuppressWarnings("null")
    @Test
    @DisplayName("Deve tratar AuthorizationDeniedException com 403")
    void handleAuthorizationDenied() {
        when(request.getRequestURI()).thenReturn("/usuarios");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new AuthorizationDeniedException("Access Denied"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acesso negado", response.getBody().getError());
    }


    @SuppressWarnings("null")
    @Test
    @DisplayName("Deve tratar AccessDeniedException com 403")
    void handleAccessDeniedBase() {
        when(request.getRequestURI()).thenReturn("/aplicativos");
        ResponseEntity<ErrorResponse> response = handler.handleAccessDenied(
                new AccessDeniedException("Denied"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Acesso negado", response.getBody().getError());
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve tratar RuntimeException generica")
    void handleRuntimeException() {
        when(request.getRequestURI()).thenReturn("/api/teste");
        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(new RuntimeException("Erro fatal"), request);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Erro de execução", response.getBody().getError());
    }

    @Test
    @DisplayName("Deve tratar InvalidLoginException")
    void handleInvalidLogin() {
        ResponseEntity<ErrorResponse> response = handler.handleInvalidLogin(new InvalidLoginException("Login falhou"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar DomainException")
    void handleDomain() {
        ResponseEntity<ErrorResponse> response = handler.handleDomain(new DomainException("Erro negocio"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    @DisplayName("Deve tratar UserNotFoundException")
    void handleUserNotFound() {
        ResponseEntity<ErrorResponse> response = handler.handleUserNotFound(new UserNotFoundException("Sumiu"), request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @SuppressWarnings({ "null" })
	@Test
    @DisplayName("Deve tratar MethodArgumentNotValidException e mapear erros")
    void handleValidation() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("obj", "campo", "obrigatorio");
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, request);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertEquals("obrigatorio", response.getBody().getValidationErrors().get("campo"));
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve tratar ExpiredJwtException")
    void handleExpiredJwt() {
        ResponseEntity<ErrorResponse> response = handler.handleExpiredJwt(null, request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertTrue(response.getBody().getMessage().contains("expirou"));
    }

    @Test
    @DisplayName("Deve tratar JwtException")
    void handleJwtException() {
        ResponseEntity<ErrorResponse> response = handler.handleJwtException(new JwtException("Token maluco"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve tratar IllegalArgumentException com e sem mensagem")
    void handleIllegalArgument() {
        ResponseEntity<ErrorResponse> r1 = handler.handleIllegalArgument(new IllegalArgumentException("Msg"), request);
        assertEquals("Msg", r1.getBody().getMessage());
        ResponseEntity<ErrorResponse> r2 = handler.handleIllegalArgument(new IllegalArgumentException(), request);
        assertEquals("Dados inválidos.", r2.getBody().getMessage());
    }

    @SuppressWarnings("null")
	@Test
    @DisplayName("Deve tratar DataIntegrityViolationException cobrindo todos os IFs de mensagens Oracle")
    void handleDataIntegrityViolation() {
        DataIntegrityViolationException ex = mock(DataIntegrityViolationException.class);
        Throwable cause = mock(Throwable.class);
        when(ex.getMostSpecificCause()).thenReturn(cause);
        when(cause.getMessage()).thenReturn("Unique constraint SYS_C008226 violated");
        assertEquals("Já existe um usuário com este login.", handler.handleDataIntegrityViolation(ex, request).getBody().getMessage());
        when(cause.getMessage()).thenReturn("Integrity constraint FK_UP_PERFIL violated");
        assertEquals("Um ou mais perfis informados não existem.", handler.handleDataIntegrityViolation(ex, request).getBody().getMessage());
        when(cause.getMessage()).thenReturn("Erro generico");
        assertTrue(handler.handleDataIntegrityViolation(ex, request).getBody().getMessage().contains("Violação"));
    }
}
