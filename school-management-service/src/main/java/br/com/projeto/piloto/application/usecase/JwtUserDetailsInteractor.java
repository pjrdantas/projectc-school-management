package br.com.projeto.piloto.application.usecase;

import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
@Primary
public class JwtUserDetailsInteractor implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) {

        return User.builder()
                .username(username)
                .password("") 
                .authorities("ROLE_ADMIN") 
                .build();
    }
}
