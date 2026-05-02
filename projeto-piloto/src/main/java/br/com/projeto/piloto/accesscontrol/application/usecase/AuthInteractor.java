package br.com.projeto.piloto.accesscontrol.application.usecase;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.entity.AuthUsuario;
import br.com.projeto.piloto.accesscontrol.adapter.out.persistence.mapper.AuthUsuarioMapper;
import br.com.projeto.piloto.accesscontrol.application.port.in.AuthUseCasePort;
import br.com.projeto.piloto.accesscontrol.application.port.out.AuthUsuarioRepositoryPort;
import br.com.projeto.piloto.accesscontrol.domain.exception.InvalidLoginException;
import br.com.projeto.piloto.accesscontrol.domain.model.AuthUsuarioModel;
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
