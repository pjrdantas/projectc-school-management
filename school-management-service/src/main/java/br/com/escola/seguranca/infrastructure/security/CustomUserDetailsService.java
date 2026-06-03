package br.com.escola.seguranca.infrastructure.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;
import br.com.escola.seguranca.adapter.out.persistence.repository.SpringUsuarioJpaRepository;
import lombok.RequiredArgsConstructor;

@Service("jwtUserDetailsService")
@Primary
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SpringUsuarioJpaRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UsuarioEntity usuario = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        if (!usuario.isAtivo()) {
            throw new DisabledException("Usuário inativo");
        }

        Set<String> authorities = new HashSet<>();
        userRepository.findPerfisByIdUsuario(usuario.getId()).forEach(perfil -> authorities.add("ROLE_" + perfil));
        userRepository.findPermissoesByIdUsuario(usuario.getId()).forEach(authorities::add);

        return User.withUsername(usuario.getUsername())
                .password(usuario.getSenhaHash())
                .authorities(authorities.toArray(new String[0]))
                .build();
    }
}
