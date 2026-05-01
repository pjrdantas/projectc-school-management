package br.com.projeto.piloto.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.time.LocalDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import br.com.projeto.piloto.adapter.out.jpa.repository.AuthSessaoRepository;

@ExtendWith(MockitoExtension.class)
class SessaoCleanupSchedulerTest {

    @Mock
    private AuthSessaoRepository authSessaoRepository;

    @InjectMocks
    private SessaoCleanupScheduler scheduler;

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("executarFaxinaAt deve chamar repository.deleteByDataExpiracaoBefore com a data passada")
    void executarFaxinaAt_chamaRepository() {
        LocalDateTime momento = LocalDateTime.of(2025, 12, 25, 10, 30);

        scheduler.executarFaxinaAt(momento);

        verify(authSessaoRepository, times(1)).deleteByDataExpiracaoBefore(eq(momento));
    }

    @Test
    @DisplayName("executarFaxina delega para executarFaxinaAt e chama repository")
    void executarFaxina_delegaParaExecutarFaxinaAt() {
        scheduler.executarFaxina();

        verify(authSessaoRepository, times(1)).deleteByDataExpiracaoBefore(any(LocalDateTime.class));
    }
}