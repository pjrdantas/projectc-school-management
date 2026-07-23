package br.com.escola.identityaccessservice.infra.security;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.com.escola.identityaccessservice.application.port.out.SenhaProtegidaPort;

@Component
public class SenhaProtegidaAdapter implements SenhaProtegidaPort {

    private final PasswordEncoder passwordEncoder;

    public SenhaProtegidaAdapter(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public String proteger(String senha) {
        return passwordEncoder.encode(senha);
    }
}
