package br.com.projeto.piloto.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.projeto.piloto.adapter.in.web.exception.InvalidLoginException;
import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.adapter.out.jpa.mapper.AuthUsuarioMapper;
import br.com.projeto.piloto.domain.model.AuthUsuarioModel;
import br.com.projeto.piloto.domain.port.inbound.AuthUseCasePort;
import br.com.projeto.piloto.domain.port.outbound.AuthUsuarioRepositoryPort;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthInteractor implements AuthUseCasePort {

	private final AuthUsuarioRepositoryPort usuarioRepo;
	private final PasswordEncoder passwordEncoder;
	private final AuthUsuarioMapper mapper;

	@Override
	public AuthUsuarioModel authenticate(String login, String senha) {

		AuthUsuario user = usuarioRepo.findByLogin(login).orElseThrow(() -> new InvalidLoginException("Usuário ou senha inválidos"));

		if (!passwordEncoder.matches(senha, user.getSenha())) {
			throw new InvalidLoginException("Usuário ou senha inválidos");
		}

		return mapper.toDomain(user);
	}
	
	@Override
	public AuthUsuarioModel findByLogin(String login) {
	    AuthUsuario usuario = usuarioRepo.findByLogin(login)
	            .orElseThrow(() -> new InvalidLoginException("Acesso negado: usuário não localizado."));

	    return mapper.toDomain(usuario);
	}

}
