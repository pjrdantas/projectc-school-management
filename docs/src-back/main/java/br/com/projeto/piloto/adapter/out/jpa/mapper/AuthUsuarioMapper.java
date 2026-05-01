package br.com.projeto.piloto.adapter.out.jpa.mapper;

import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import br.com.projeto.piloto.adapter.out.jpa.entity.AuthUsuario;
import br.com.projeto.piloto.domain.model.AuthUsuarioModel;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class AuthUsuarioMapper {

    public AuthUsuarioModel toDomain(AuthUsuario usuario) {
        if (usuario == null) return null;

        return AuthUsuarioModel.builder()
                .id(usuario.getId())
                .login(usuario.getLogin())
                .senha(usuario.getSenha())
                .nome(usuario.getNome())
                .ativo(usuario.getAtivo())
                .email(usuario.getEmail())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .perfis(
                    usuario.getPerfis().stream()
                        .map(AuthPerfilMapper::toDomain) 
                        .collect(Collectors.toSet())
                )
                .build();
    }

    public AuthUsuario toEntity(AuthUsuarioModel model) {
        if (model == null) return null;

        AuthUsuario usuario = new AuthUsuario();
        usuario.setId(model.getId());
        usuario.setLogin(model.getLogin());
        usuario.setSenha(model.getSenha());
        usuario.setNome(model.getNome());
        usuario.setAtivo(model.getAtivo());
        usuario.setEmail(model.getEmail());

        usuario.setPerfis(
            model.getPerfis().stream()
                .map(AuthPerfilMapper::toEntity) 
                .collect(Collectors.toSet())
        );

        return usuario;
    }
}
