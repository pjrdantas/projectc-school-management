package br.com.escola.institucional.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.escola.institucional.adapter.out.persistence.entity.EscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.entity.UsuarioEscolaEntity;
import br.com.escola.institucional.adapter.out.persistence.repository.UsuarioEscolaJpaRepository;
import br.com.escola.institucional.application.port.internal.UsuarioEscolaPort;
import br.com.escola.seguranca.adapter.out.persistence.entity.UsuarioEntity;

@Service
public class UsuarioEscolaService implements UsuarioEscolaPort {

    private final UsuarioEscolaJpaRepository usuarioEscolaJpaRepository;

    public UsuarioEscolaService(UsuarioEscolaJpaRepository usuarioEscolaJpaRepository) {
        this.usuarioEscolaJpaRepository = usuarioEscolaJpaRepository;
    }

    @Override
    @Transactional
    public void garantirVinculo(UUID usuarioId, UUID escolaId) {
        if (usuarioId == null || escolaId == null) {
            return;
        }
        if (usuarioEscolaJpaRepository.existsByUsuario_IdAndEscola_Id(usuarioId, escolaId)) {
            return;
        }

        UsuarioEntity usuario = new UsuarioEntity();
        usuario.setId(usuarioId);
        EscolaEntity escola = new EscolaEntity();
        escola.setId(escolaId);
        usuarioEscolaJpaRepository.save(new UsuarioEscolaEntity(usuario, escola));
    }

    @Override
    @Transactional(readOnly = true)
    public List<UUID> listarEscolasDoUsuario(UUID usuarioId) {
        if (usuarioId == null) {
            return List.of();
        }
        return usuarioEscolaJpaRepository.findEscolaIdsByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean usuarioTemVinculo(UUID usuarioId, UUID escolaId) {
        if (usuarioId == null || escolaId == null) {
            return false;
        }
        return usuarioEscolaJpaRepository.existsByUsuario_IdAndEscola_Id(usuarioId, escolaId);
    }
}
