package br.com.escola.bff.application.port.out;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.AuthSessionContext;
import br.com.escola.bff.application.dto.CatalogReadQuery;
import reactor.core.publisher.Mono;

public interface PlanejamentoIaConteudoBibliotecaWritePort {

    Mono<ResponseEntity<String>> publicarBiblioteca(
            UUID conteudoId,
            CatalogReadQuery query,
            AuthSessionContext context);
}

