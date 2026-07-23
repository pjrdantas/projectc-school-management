package br.com.escola.enrollmentdocumentservice.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.escola.enrollmentdocumentservice.application.context.InternalHeaders;
import br.com.escola.enrollmentdocumentservice.application.context.InternalRequestContext;
import br.com.escola.enrollmentdocumentservice.application.dto.CriarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.AtualizarStatusMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.CancelarMatriculaCommand;
import br.com.escola.enrollmentdocumentservice.application.dto.MatriculaResponse;
import br.com.escola.enrollmentdocumentservice.application.port.in.MatriculaWriteUseCase;

@RestController
@RequestMapping({ "/internal/v1/matriculas", "/internal/matriculas" })
public class MatriculaWriteInternalController {

    private final MatriculaWriteUseCase matriculaWriteUseCase;

    public MatriculaWriteInternalController(MatriculaWriteUseCase matriculaWriteUseCase) {
        this.matriculaWriteUseCase = matriculaWriteUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MatriculaResponse criar(
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody CriarMatriculaCommand command) {
        return matriculaWriteUseCase.criar(command, context);
    }

    @PutMapping("/{matriculaId}")
    public MatriculaResponse atualizar(
            @PathVariable java.util.UUID matriculaId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody AtualizarMatriculaCommand command) {
        return matriculaWriteUseCase.atualizar(matriculaId, command, context);
    }

    @PatchMapping("/{matriculaId}/status")
    public MatriculaResponse atualizarStatus(
            @PathVariable java.util.UUID matriculaId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody AtualizarStatusMatriculaCommand command) {
        return matriculaWriteUseCase.atualizarStatus(matriculaId, command, context);
    }

    @PostMapping("/{matriculaId}/cancelamento")
    public MatriculaResponse cancelar(
            @PathVariable java.util.UUID matriculaId,
            @RequestAttribute(InternalHeaders.REQUEST_CONTEXT_ATTRIBUTE) InternalRequestContext context,
            @RequestBody CancelarMatriculaCommand command) {
        return matriculaWriteUseCase.cancelar(matriculaId, command, context);
    }
}
