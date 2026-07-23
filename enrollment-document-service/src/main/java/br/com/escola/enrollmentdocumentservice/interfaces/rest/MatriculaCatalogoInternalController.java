package br.com.escola.enrollmentdocumentservice.interfaces.rest;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({ "/internal/v1/matriculas/catalogos", "/internal/matriculas/catalogos" })
public class MatriculaCatalogoInternalController {

    @GetMapping("/status")
    public List<CatalogoStatusResponse> listarStatus() {
        return List.of(
                status("SOLICITADA", "Matricula solicitada"),
                status("EM_ANDAMENTO", "Matricula em andamento"),
                status("AGUARDANDO_DOCUMENTOS", "Aguardando documentos"),
                status("AGUARDANDO_HISTORICO_ESCOLAR", "Aguardando historico escolar"),
                status("EFETIVADA", "Matricula efetivada"),
                status("CONCLUIDA", "Matricula concluida"),
                status("CANCELADA", "Matricula cancelada"),
                status("INDEFERIDA", "Matricula indeferida"),
                status("TRANSFERIDO", "Matricula transferida"));
    }

    private static CatalogoStatusResponse status(String codigo, String descricao) {
        return new CatalogoStatusResponse(codigo, codigo, descricao);
    }

    public record CatalogoStatusResponse(String id, String codigo, String descricao) {
    }
}
