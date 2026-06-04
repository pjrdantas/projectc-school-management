package br.com.escola.historico.application.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarGeracaoRequest;

public interface HistoricoEscolarService {

    HistoricoEscolarResponse criar(HistoricoEscolarRequest request);

    HistoricoEscolarResponse atualizar(UUID id, HistoricoEscolarRequest request);

    void excluir(UUID id);

    HistoricoEscolarResponse buscarPorId(UUID id);

    Page<HistoricoEscolarResponse> listar(Pageable pageable);

    HistoricoEscolarResponse gerarPorBoletim(UUID matriculaId, HistoricoEscolarGeracaoRequest request);
}
