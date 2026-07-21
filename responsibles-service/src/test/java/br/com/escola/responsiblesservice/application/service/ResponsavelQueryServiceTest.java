package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import com.fasterxml.jackson.databind.ObjectMapper;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.ResponsavelReadModelResponse;
import br.com.escola.responsiblesservice.application.port.out.ResponsavelLocalReadPort;
import br.com.escola.responsiblesservice.infra.config.LeituraModeloProperties;

class ResponsavelQueryServiceTest {

    @Test
    void deveUsarSomenteLeituraLocal() {
        UUID escolaId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        ResponsavelLocalReadPort local = mock(ResponsavelLocalReadPort.class);
        when(local.listarResponsaveis(escolaId, "Maria", null)).thenReturn(Optional.of(List.of(
                response(responsavelId, escolaId))));

        var result = service(local).listarResponsaveis(
                "Bearer token",
                context(escolaId),
                "Maria",
                null);

        assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(result.getBody()).contains(responsavelId.toString());
    }

    @Test
    void deveRetornarNotFoundSemConsultarOrigemAlternativa() {
        UUID escolaId = UUID.randomUUID();
        ResponsavelLocalReadPort local = mock(ResponsavelLocalReadPort.class);
        when(local.buscarResponsavelPorId(UUID.fromString("00000000-0000-0000-0000-000000000001"), escolaId))
                .thenReturn(Optional.empty());

        var result = service(local).buscarResponsavelPorId(
                "Bearer token",
                context(escolaId),
                UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertThat(result.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void deveBloquearConfiguracaoQueReativeFallback() {
        ResponsavelLocalReadPort local = mock(ResponsavelLocalReadPort.class);
        LeituraModeloProperties properties = new LeituraModeloProperties(true, true, true, false, false, 500, true, true);
        var service = service(local, properties);

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.listarResponsaveis(
                "Bearer token", context(UUID.randomUUID()), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("responsibles-local-read-required");
        verifyNoInteractions(local);
    }

    private ResponsavelQueryService service(ResponsavelLocalReadPort local) {
        return service(local, new LeituraModeloProperties(true, true, true, false, false, 500, true, false));
    }

    @SuppressWarnings("unchecked")
    private ResponsavelQueryService service(ResponsavelLocalReadPort local, LeituraModeloProperties properties) {
        ObjectProvider<ResponsavelLocalReadPort> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(local);
        return new ResponsavelQueryService(
                provider,
                properties,
                new LeituraModeloRouteGuard(properties),
                new ObjectMapper());
    }

    private InternalRequestContext context(UUID escolaId) {
        return new InternalRequestContext("corr", UUID.randomUUID(), escolaId);
    }

    private ResponsavelReadModelResponse response(UUID responsavelId, UUID escolaId) {
        return new ResponsavelReadModelResponse(
                responsavelId,
                "Maria",
                "123",
                "maria@example.com",
                "11999999999",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                escolaId,
                "Escola",
                null);
    }
}
