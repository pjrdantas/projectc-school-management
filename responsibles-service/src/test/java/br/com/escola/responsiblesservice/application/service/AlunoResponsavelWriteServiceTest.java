package br.com.escola.responsiblesservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.escola.responsiblesservice.application.context.InternalRequestContext;
import br.com.escola.responsiblesservice.application.dto.CriacaoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.DesvinculoAlunoResponsavelResultado;
import br.com.escola.responsiblesservice.application.dto.VincularAlunoResponsavelCommand;
import br.com.escola.responsiblesservice.application.exception.AlunoResponsavelVinculoDuplicadoException;
import br.com.escola.responsiblesservice.application.port.out.AlunoConsultaPort;
import br.com.escola.responsiblesservice.application.port.out.AlunoResponsavelWritePort;

class AlunoResponsavelWriteServiceTest {

    @Test
    void deveValidarAlunoNoServicoDonoECriarVinculoNormalizado() {
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        UUID escolaId = UUID.randomUUID();
        CapturingAlunoConsultaPort alunoPort = new CapturingAlunoConsultaPort(true);
        CapturingAlunoResponsavelWritePort vinculoPort = new CapturingAlunoResponsavelWritePort(
                CriacaoAlunoResponsavelResultado.CRIADO);
        AlunoResponsavelWriteService service = new AlunoResponsavelWriteService(alunoPort, vinculoPort);
        InternalRequestContext context = new InternalRequestContext("correlation", UUID.randomUUID(), escolaId);

        service.vincular(alunoId, new VincularAlunoResponsavelCommand(
                responsavelId, " mae ", true, null, true), context);

        assertThat(alunoPort.alunoId).isEqualTo(alunoId);
        assertThat(vinculoPort.alunoId).isEqualTo(alunoId);
        assertThat(vinculoPort.escolaId).isEqualTo(escolaId);
        assertThat(vinculoPort.command.parentesco()).isEqualTo("MAE");
        assertThat(vinculoPort.command.responsavelPedagogico()).isFalse();
    }

    @Test
    void deveMapearVinculoDuplicadoParaConflito() {
        AlunoResponsavelWriteService service = new AlunoResponsavelWriteService(
                new CapturingAlunoConsultaPort(true),
                new CapturingAlunoResponsavelWritePort(CriacaoAlunoResponsavelResultado.VINCULO_DUPLICADO));

        assertThatThrownBy(() -> service.vincular(
                UUID.randomUUID(),
                new VincularAlunoResponsavelCommand(UUID.randomUUID(), null, null, null, null),
                new InternalRequestContext("correlation", UUID.randomUUID(), UUID.randomUUID())))
                .isInstanceOf(AlunoResponsavelVinculoDuplicadoException.class);
    }

    @Test
    void deveValidarAlunoAntesDoDesvinculo() {
        UUID alunoId = UUID.randomUUID();
        UUID responsavelId = UUID.randomUUID();
        CapturingAlunoConsultaPort alunoPort = new CapturingAlunoConsultaPort(true);
        CapturingAlunoResponsavelWritePort vinculoPort = new CapturingAlunoResponsavelWritePort(
                CriacaoAlunoResponsavelResultado.CRIADO);
        AlunoResponsavelWriteService service = new AlunoResponsavelWriteService(alunoPort, vinculoPort);
        InternalRequestContext context = new InternalRequestContext("correlation", UUID.randomUUID(), UUID.randomUUID());

        service.desvincular(alunoId, responsavelId, context);

        assertThat(alunoPort.alunoId).isEqualTo(alunoId);
        assertThat(vinculoPort.responsavelId).isEqualTo(responsavelId);
        assertThat(vinculoPort.escolaId).isEqualTo(context.escolaId());
    }

    private static class CapturingAlunoConsultaPort implements AlunoConsultaPort {
        private final boolean exists;
        private UUID alunoId;

        private CapturingAlunoConsultaPort(boolean exists) {
            this.exists = exists;
        }

        @Override
        public boolean existeAtivoNaEscola(UUID alunoId, InternalRequestContext context) {
            this.alunoId = alunoId;
            return exists;
        }
    }

    private static class CapturingAlunoResponsavelWritePort implements AlunoResponsavelWritePort {
        private final CriacaoAlunoResponsavelResultado resultado;
        private UUID alunoId;
        private UUID escolaId;
        private UUID responsavelId;
        private VincularAlunoResponsavelCommand command;

        private CapturingAlunoResponsavelWritePort(CriacaoAlunoResponsavelResultado resultado) {
            this.resultado = resultado;
        }

        @Override
        public CriacaoAlunoResponsavelResultado vincular(
                UUID alunoId,
                VincularAlunoResponsavelCommand command,
                UUID escolaId) {
            this.alunoId = alunoId;
            this.command = command;
            this.escolaId = escolaId;
            return resultado;
        }

        @Override
        public DesvinculoAlunoResponsavelResultado desvincular(UUID alunoId, UUID responsavelId, UUID escolaId) {
            this.alunoId = alunoId;
            this.responsavelId = responsavelId;
            this.escolaId = escolaId;
            return DesvinculoAlunoResponsavelResultado.DESVINCULADO;
        }
    }
}
