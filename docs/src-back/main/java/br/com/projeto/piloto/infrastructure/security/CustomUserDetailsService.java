package br.com.projeto.piloto.infrastructure.security;

import lombok.RequiredArgsConstructor;

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

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.domain.port.outbound.AuthUsuarioRepositoryPort;

@Service("jwtUserDetailsService") 
@Primary    
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthUsuarioRepositoryPort userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        AuthUsuario authUsuario = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        if (!"S".equalsIgnoreCase(authUsuario.getAtivo())) {
            throw new DisabledException("Usuário inativo");
        }

        Set<String> authorities = new HashSet<>();
        
        authUsuario.getPerfis().forEach(perfil -> {
            
            authorities.add("ROLE_" + perfil.getNmPerfil().toUpperCase()); 

            perfil.getPermissoes().forEach(permissao -> {
                authorities.add(permissao.getNmPermissao().toUpperCase()); 
            });
        });

        return User.withUsername(authUsuario.getLogin())
                .password(authUsuario.getSenha())
                .authorities(authorities.toArray(new String[0])) 
                .build();
    }
}