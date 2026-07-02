package br.com.escola.peopleservice.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.peopleservice.application.context.InternalRequestContext;
import br.com.escola.peopleservice.application.dto.PessoaCatalogoResponse;
import br.com.escola.peopleservice.application.dto.PessoaConsultaCadastralPageResponse;
import br.com.escola.peopleservice.application.dto.PessoaResumoResponse;
import br.com.escola.peopleservice.application.exception.PeopleServiceResourceNotFoundException;
import br.com.escola.peopleservice.application.port.in.PessoaQueryUseCase;
import br.com.escola.peopleservice.application.port.out.PeopleCatalogLocalReadPort;
import br.com.escola.peopleservice.application.port.out.PeopleIdentityLocalReadPort;
import br.com.escola.peopleservice.application.port.out.PessoaReadPort;
import io.micrometer.core.instrument.MeterRegistry;

@Service
public class PessoaQueryService implements PessoaQueryUseCase {

    private final PessoaReadPort pessoaReadPort;
    private final PeopleCatalogLocalReadPort catalogLocalReadPort;
    private final PeopleIdentityLocalReadPort identityLocalReadPort;
    private final PeopleLocalReadCutoverGuard readCutoverGuard;
    private final MeterRegistry meterRegistry;

    public PessoaQueryService(
            PessoaReadPort pessoaReadPort,
            PeopleCatalogLocalReadPort catalogLocalReadPort,
            PeopleIdentityLocalReadPort identityLocalReadPort,
            PeopleLocalReadCutoverGuard readCutoverGuard,
            MeterRegistry meterRegistry) {
        this.pessoaReadPort = pessoaReadPort;
        this.catalogLocalReadPort = catalogLocalReadPort;
        this.identityLocalReadPort = identityLocalReadPort;
        this.readCutoverGuard = readCutoverGuard;
        this.meterRegistry = meterRegistry;
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposPessoa(String authorization, InternalRequestContext context) {
        var decision = readCutoverGuard.registrarDecisao("listarTiposPessoa");
        if (decision.localReadEligible()) {
            try {
                List<PessoaCatalogoResponse> response = catalogLocalReadPort.listarTiposPessoa();
                registrarLeituraLocal("listarTiposPessoa", "success");
                return response;
            } catch (RuntimeException ex) {
                registrarLeituraLocal("listarTiposPessoa", "fallback");
            }
        }
        return pessoaReadPort.listarTiposPessoa(authorization, context);
    }

    @Override
    public List<PessoaCatalogoResponse> listarTiposEndereco(String authorization, InternalRequestContext context) {
        var decision = readCutoverGuard.registrarDecisao("listarTiposEndereco");
        if (decision.localReadEligible()) {
            try {
                List<PessoaCatalogoResponse> response = catalogLocalReadPort.listarTiposEndereco();
                registrarLeituraLocal("listarTiposEndereco", "success");
                return response;
            } catch (RuntimeException ex) {
                registrarLeituraLocal("listarTiposEndereco", "fallback");
            }
        }
        return pessoaReadPort.listarTiposEndereco(authorization, context);
    }

    @Override
    public PessoaResumoResponse buscarPessoaPorId(String authorization, InternalRequestContext context, UUID pessoaId) {
        var decision = readCutoverGuard.registrarDecisao("buscarPorId");
        if (decision.localReadEligible()) {
            try {
                var localResponse = identityLocalReadPort.buscarPessoaPorId(pessoaId, context.escolaId());
                if (localResponse.isPresent()) {
                    registrarLeituraIdentidadeLocal("buscarPorId", "success");
                    return localResponse.get();
                }
                registrarLeituraIdentidadeLocal("buscarPorId", "fallback_not_found");
            } catch (RuntimeException ex) {
                registrarLeituraIdentidadeLocal("buscarPorId", "fallback_error");
            }
        }
        return pessoaReadPort.buscarPessoaPorId(authorization, context, pessoaId)
                .orElseThrow(() -> new PeopleServiceResourceNotFoundException("Pessoa nao encontrada"));
    }

    @Override
    public PessoaConsultaCadastralPageResponse consultarCadastro(
            String authorization,
            InternalRequestContext context,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        readCutoverGuard.registrarDecisao("consultarCadastro");
        return pessoaReadPort.consultarCadastro(
                authorization,
                context,
                nomeAluno,
                cpfAluno,
                nomeResponsavel,
                cpfResponsavel,
                page,
                size);
    }

    private void registrarLeituraLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.catalog.reads",
                "operation", operation,
                "result", result)
                .increment();
    }

    private void registrarLeituraIdentidadeLocal(String operation, String result) {
        meterRegistry.counter(
                "people.shadow.local.persistence.identity.reads",
                "operation", operation,
                "result", result)
                .increment();
    }
}
