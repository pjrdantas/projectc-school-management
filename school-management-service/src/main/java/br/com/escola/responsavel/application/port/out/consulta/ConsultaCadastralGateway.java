package br.com.escola.responsavel.application.port.out.consulta;

import br.com.escola.responsavel.application.dto.consulta.ConsultaCadastralPageOutput;

public interface ConsultaCadastralGateway {

    ConsultaCadastralPageOutput consultar(
            String nomeAluno,
            String cpfAluno,
            String nomeResponsavel,
            String cpfResponsavel,
            int page,
            int size);
}
