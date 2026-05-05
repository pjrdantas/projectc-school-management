package br.com.escola.accesscontrol.application.usecase;

import java.util.stream.Collectors;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.escola.accesscontrol.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.accesscontrol.application.port.in.AuthUseCasePort;
import br.com.escola.accesscontrol.application.port.out.AuthRepositoryPort;
import br.com.escola.accesscontrol.domain.exception.InvalidLoginException;
import br.com.escola.accesscontrol.domain.model.AuthUsuarioModel;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthInteractor implements AuthUseCasePort {

    private final AuthRepositoryPort repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AuthUsuarioModel authenticate(String username, String senha) {

        UsuarioEntity user = repository.findByUsername(username)
                .orElseThrow(() -> new InvalidLoginException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(senha, user.getSenhaHash())) {
            throw new InvalidLoginException("Usuário ou senha inválidos");
        }

        return map(user);
    }

    @Override
    public AuthUsuarioModel findByLogin(String username) {

        UsuarioEntity user = repository.findByUsername(username)
                .orElseThrow(() -> new InvalidLoginException("Acesso negado: usuário não localizado."));

        return map(user);
    }

    // ================= MAPEAMENTO =================
    private AuthUsuarioModel map(UsuarioEntity user) {
        return AuthUsuarioModel.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nome(user.getNome())
                .email(user.getEmail())
                .senhaHash(user.getSenhaHash())
                .ativo(user.isAtivo())
                .createdAt(user.getCreatedAt())
                .perfis(
                        user.getPerfis().stream()
                                .map(p -> br.com.escola.accesscontrol.domain.model.PerfilModel.builder()
                                        .id(p.getId())
                                        .nome(p.getNome())
                                        .build()
                                )
                                .collect(Collectors.toSet())
                )
                .build();
    }
}