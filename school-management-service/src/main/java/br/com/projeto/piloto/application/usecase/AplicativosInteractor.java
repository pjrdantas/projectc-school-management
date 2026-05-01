package br.com.projeto.piloto.application.usecase;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.projeto.piloto.domain.model.AplicativosModel;
import br.com.projeto.piloto.domain.port.inbound.AplicativosUseCase;
import br.com.projeto.piloto.domain.port.outbound.AplicativosRepositoryPort;

@Service
public class AplicativosInteractor implements AplicativosUseCase {

    private final AplicativosRepositoryPort repository;

    public AplicativosInteractor(AplicativosRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public List<AplicativosModel> listAll() {
        return repository.findAll();
    }

    @Override
    public Optional<AplicativosModel> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    public AplicativosModel create(AplicativosModel Aplicativos) {
        Aplicativos.setCriadoEm(LocalDateTime.now());
        Aplicativos.setAtualizadoEm(LocalDateTime.now());
        return repository.save(Aplicativos);
    }

    @Override
    public AplicativosModel update(Long id, AplicativosModel Aplicativos) {
        Optional<AplicativosModel> existing = repository.findById(id);

        if (existing.isEmpty()) {
            throw new IllegalArgumentException("Aplicativos não encontrado: " + id);
        }

        AplicativosModel m = existing.get();
        m.setNmAplicativo(Aplicativos.getNmAplicativo());
        m.setDsAplicativo(Aplicativos.getDsAplicativo());
        m.setDsUrl(Aplicativos.getDsUrl());
        m.setNmModulo(Aplicativos.getNmModulo());
        m.setModuloExposto(Aplicativos.getModuloExposto());
        m.setDsRota(Aplicativos.getDsRota());
        m.setFlAtivo(Aplicativos.getFlAtivo());
        m.setAtualizadoEm(LocalDateTime.now());

        return repository.save(m);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsByNome(String nome) {
        return repository.existsByNome(nome);
    }

    @Override
    public List<AplicativosModel> listAtivos() {
        return repository.findByAtivo("S");
    }

}
