package br.com.escola.ia.application.gateway;

import br.com.escola.ia.application.service.GeracaoConteudoPedagogicoComando;
import br.com.escola.ia.application.service.GeracaoConteudoPedagogicoResultado;

public interface GeradorConteudoPedagogicoGateway {

    GeracaoConteudoPedagogicoResultado gerar(GeracaoConteudoPedagogicoComando comando);
}
