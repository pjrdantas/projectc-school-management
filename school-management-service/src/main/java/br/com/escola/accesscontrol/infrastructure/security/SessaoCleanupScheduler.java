package br.com.escola.accesscontrol.infrastructure.security;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.escola.accesscontrol.adapter.out.persistence.repository.SessaoAutenticacaoJpaRepository;
import lombok.RequiredArgsConstructor;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class SessaoCleanupScheduler {

    private final SessaoAutenticacaoJpaRepository sessaoRepository;

    @Scheduled(fixedDelay = 86400000)
    public void executarFaxina() {
        executarFaxinaAt(LocalDateTime.now());
    }

    public void executarFaxinaAt(LocalDateTime dataHora) {
        sessaoRepository.findAll().stream()
                .filter(s -> s.getExpiraEm() != null && s.getExpiraEm().isBefore(dataHora))
                .forEach(sessaoRepository::delete);
    }
}
