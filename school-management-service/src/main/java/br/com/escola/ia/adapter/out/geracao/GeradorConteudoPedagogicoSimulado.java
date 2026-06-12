package br.com.escola.ia.adapter.out.geracao;

import org.springframework.stereotype.Component;

import br.com.escola.ia.application.gateway.GeradorConteudoPedagogicoGateway;
import br.com.escola.ia.application.service.GeracaoConteudoPedagogicoComando;
import br.com.escola.ia.application.service.GeracaoConteudoPedagogicoResultado;

@Component
public class GeradorConteudoPedagogicoSimulado implements GeradorConteudoPedagogicoGateway {

    private static final String MODELO_SIMULADO = "simulado-local-v1";

    @Override
    public GeracaoConteudoPedagogicoResultado gerar(GeracaoConteudoPedagogicoComando comando) {
        String conteudo = """
                Sugestão pedagógica simulada

                Planejamento: %s
                Tema: %s
                Turma: %s
                Disciplina: %s
                Tipo de conteúdo: %s

                Objetivo: %s

                Proposta:
                1. Retomar o conhecimento prévio da turma.
                2. Desenvolver o tema com exemplos contextualizados.
                3. Aplicar atividade formativa.
                4. Registrar evidências de aprendizagem.

                Solicitação do professor:
                %s
                """.formatted(
                comando.tituloPlanejamento(),
                comando.temaPrincipal(),
                comando.turmaNome(),
                comando.disciplinaNome(),
                comando.tipoConteudo(),
                comando.objetivoGeral() == null || comando.objetivoGeral().isBlank()
                        ? comando.descricaoInicial()
                        : comando.objetivoGeral(),
                comando.promptProfessor());

        return new GeracaoConteudoPedagogicoResultado(
                MODELO_SIMULADO,
                conteudo,
                estimarTokens(comando.promptProfessor()),
                estimarTokens(conteudo));
    }

    private int estimarTokens(String texto) {
        if (texto == null || texto.isBlank()) {
            return 0;
        }
        return Math.max(1, texto.trim().split("\\s+").length);
    }
}
