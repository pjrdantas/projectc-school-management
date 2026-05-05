package br.com.escola.accesscontrol.infrastructure.security;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class SessaoCleanupScheduler {

    private final AuthSessaoRepository authSessaoRepository;

    @Scheduled(fixedDelay = 86400000)
    public void executarFaxina() {

        executarFaxinaAt(LocalDateTime.now());
    }

    public void executarFaxinaAt(LocalDateTime dataHora) {
        authSessaoRepository.deleteByDataExpiracaoBefore(dataHora);
    }
}