package br.com.escola.institutionaltenantservice.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.institutionaltenantservice.application.dto.EscolaRequest;
import br.com.escola.institutionaltenantservice.application.model.EscolaAdministrada;
import br.com.escola.institutionaltenantservice.application.port.in.AdministrarEscolaUseCase;
import br.com.escola.institutionaltenantservice.application.port.out.EscolaAdministradaPort;

@Service
public class AdministracaoEscolaService implements AdministrarEscolaUseCase {

    private final EscolaAdministradaPort escolaAdministradaPort;

    public AdministracaoEscolaService(EscolaAdministradaPort escolaAdministradaPort) {
        this.escolaAdministradaPort = escolaAdministradaPort;
    }

    @Override
    public List<EscolaAdministrada> listar() {
        return escolaAdministradaPort.listar();
    }

    @Override
    public EscolaAdministrada buscar(UUID id) {
        return escolaAdministradaPort.buscar(id);
    }

    @Override
    public EscolaAdministrada criar(EscolaRequest request) {
        LocalDateTime now = LocalDateTime.now();
        return escolaAdministradaPort.salvar(toModel(UUID.randomUUID(), request, now, null));
    }

    @Override
    public EscolaAdministrada atualizar(UUID id, EscolaRequest request) {
        EscolaAdministrada current = escolaAdministradaPort.buscar(id);
        return escolaAdministradaPort.salvar(toModel(id, request, current.createdAt(), LocalDateTime.now()));
    }

    @Override
    public void excluir(UUID id) {
        escolaAdministradaPort.excluir(id);
    }

    private EscolaAdministrada toModel(
            UUID id,
            EscolaRequest request,
            LocalDateTime createdAt,
            LocalDateTime updatedAt) {
        return new EscolaAdministrada(
                id,
                request.nome().trim(),
                normalize(request.codigoInep()),
                normalize(request.cnpj()),
                normalize(request.telefone()),
                normalizeLower(request.email()),
                request.enderecoId(),
                request.ativo() == null || request.ativo(),
                createdAt,
                updatedAt);
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String normalizeLower(String value) {
        String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
