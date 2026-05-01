package br.com.projeto.piloto.adapter.out.jpa;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import br.com.projeto.piloto.adapter.out.jpa.entity.Aplicativo;
import br.com.projeto.piloto.adapter.out.jpa.repository.SpringDataAplicativosRepository;
import br.com.projeto.piloto.domain.model.AplicativosModel;
import br.com.projeto.piloto.domain.port.outbound.AplicativosRepositoryPort;
import lombok.RequiredArgsConstructor;

@Component
@Transactional
@RequiredArgsConstructor
public class AplicativosRepositoryAdapter implements AplicativosRepositoryPort {

    private final SpringDataAplicativosRepository repository;

     
    private AplicativosModel toDomain(Aplicativo e) {
        return AplicativosModel.builder()
                .id(e.getId())
                .nmAplicativo(e.getNmAplicativo())
                .dsAplicativo(e.getDsAplicativo())
                .dsUrl(e.getDsUrl())
                .nmModulo(e.getNmModulo())
                .moduloExposto(e.getModuloExposto())
                .dsRota(e.getDsRota())
                .flAtivo(e.getFlAtivo())
                .criadoEm(e.getCriadoEm())
                .atualizadoEm(e.getAtualizadoEm())
                .build();
    }

     
    private Aplicativo toEntity(AplicativosModel d) {
        Objects.requireNonNull(d, "AplicativosModel não pode ser nulo");

        Aplicativo e = new Aplicativo();
        e.setId(d.getId());
        e.setNmAplicativo(d.getNmAplicativo());
        e.setDsAplicativo(d.getDsAplicativo());
        e.setDsUrl(d.getDsUrl());
        e.setNmModulo(d.getNmModulo());
        e.setModuloExposto(d.getModuloExposto());
        e.setDsRota(d.getDsRota());
        e.setFlAtivo(d.getFlAtivo());
        e.setCriadoEm(d.getCriadoEm());
        e.setAtualizadoEm(d.getAtualizadoEm());
        return e;
    }

    @Override
    public List<AplicativosModel> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<AplicativosModel> findById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        return repository.findById(id).map(this::toDomain);
    }

    @SuppressWarnings("null")
	@Override
    public AplicativosModel save(AplicativosModel domain) {
        Objects.requireNonNull(domain, "domain não pode ser nulo");
        Aplicativo saved = repository.save(toEntity(domain));
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, "id não pode ser nulo");
        repository.deleteById(id);
    }

    @Override
    public boolean existsByNome(String nome) {
        Objects.requireNonNull(nome, "nome não pode ser nulo");
        return repository.existsByNmAplicativo(nome);
    }
    
    @Override
    public List<AplicativosModel> findByAtivo(String ativo) {
        return repository.findByFlAtivo(ativo)
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

}
