package br.com.escola.bff.application.service;

import org.springframework.http.ResponseEntity;

import br.com.escola.bff.application.dto.CatalogReadQuery;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.PeopleCadastroReadPort;
import br.com.escola.bff.application.usecase.ConsultarCadastroPessoaUseCase;
import reactor.core.publisher.Mono;

public class CadastroPessoaReadProxyService implements ConsultarCadastroPessoaUseCase {

    private final AuthContextPort authContextPort;
    private final PeopleCadastroReadPort peopleCadastroReadPort;

    public CadastroPessoaReadProxyService(
            AuthContextPort authContextPort,
            PeopleCadastroReadPort peopleCadastroReadPort) {
        this.authContextPort = authContextPort;
        this.peopleCadastroReadPort = peopleCadastroReadPort;
    }

    @Override
    public Mono<ResponseEntity<String>> consultar(
            String authorization,
            String correlationId,
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size) {
        CatalogReadQuery query = new CatalogReadQuery(authorization, correlationId);
        return authContextPort.resolve(query)
                .flatMap(context -> peopleCadastroReadPort.consultarCadastro(
                        query,
                        context,
                        nomeAluno,
                        cpfAluno,
                        nomeResponsavel,
                        cpfResponsavel,
                        page,
                        size));
    }
}
