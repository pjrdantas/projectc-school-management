package br.com.escola.historico.application.mapper;

import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Component;

import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarItemRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarItemResponse;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarRequest;
import br.com.escola.historico.adapter.in.web.dto.HistoricoEscolarResponse;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolar;
import br.com.escola.historico.adapter.out.persistence.entity.HistoricoEscolarItem;

@Component
public class HistoricoEscolarMapper {

    public HistoricoEscolar toEntity(HistoricoEscolarRequest request) {
        HistoricoEscolar historico = HistoricoEscolar.builder()
                .nomeAluno(request.nomeAluno().trim())
                .rgRen(trimToNull(request.rgRen()))
                .ra(trimToNull(request.ra()))
                .rm(trimToNull(request.rm()))
                .dataNascimento(request.dataNascimento())
                .municipioNascimento(trimToNull(request.municipioNascimento()))
                .estadoNascimento(trimToNull(request.estadoNascimento()))
                .paisNascimento(trimToNull(request.paisNascimento()))
                .nomeEscola(trimToNull(request.nomeEscola()))
                .enderecoEscola(trimToNull(request.enderecoEscola()))
                .municipioEscola(trimToNull(request.municipioEscola()))
                .cepEscola(trimToNull(request.cepEscola()))
                .telefoneEscola(trimToNull(request.telefoneEscola()))
                .emailEscola(trimToNull(request.emailEscola()))
                .anoConclusao(request.anoConclusao())
                .ensinoConcluido(trimToNull(request.ensinoConcluido()))
                .dataEmissao(request.dataEmissao())
                .diretorNome(trimToNull(request.diretorNome()))
                .diretorRg(trimToNull(request.diretorRg()))
                .gerenteOrganizacaoNome(trimToNull(request.gerenteOrganizacaoNome()))
                .gerenteOrganizacaoRg(trimToNull(request.gerenteOrganizacaoRg()))
                .doeNumero(trimToNull(request.doeNumero()))
                .doeData(request.doeData())
                .doeVolume(trimToNull(request.doeVolume()))
                .doePagina(trimToNull(request.doePagina()))
                .observacoes(trimToNull(request.observacoes()))
                .build();

        historico.replaceComponentesCurriculares(toItemEntities(request.componentesCurriculares()));
        return historico;
    }

    public void copyToEntity(HistoricoEscolarRequest request, HistoricoEscolar historico) {
        historico.setNomeAluno(request.nomeAluno().trim());
        historico.setRgRen(trimToNull(request.rgRen()));
        historico.setRa(trimToNull(request.ra()));
        historico.setRm(trimToNull(request.rm()));
        historico.setDataNascimento(request.dataNascimento());
        historico.setMunicipioNascimento(trimToNull(request.municipioNascimento()));
        historico.setEstadoNascimento(trimToNull(request.estadoNascimento()));
        historico.setPaisNascimento(trimToNull(request.paisNascimento()));
        historico.setNomeEscola(trimToNull(request.nomeEscola()));
        historico.setEnderecoEscola(trimToNull(request.enderecoEscola()));
        historico.setMunicipioEscola(trimToNull(request.municipioEscola()));
        historico.setCepEscola(trimToNull(request.cepEscola()));
        historico.setTelefoneEscola(trimToNull(request.telefoneEscola()));
        historico.setEmailEscola(trimToNull(request.emailEscola()));
        historico.setAnoConclusao(request.anoConclusao());
        historico.setEnsinoConcluido(trimToNull(request.ensinoConcluido()));
        historico.setDataEmissao(request.dataEmissao());
        historico.setDiretorNome(trimToNull(request.diretorNome()));
        historico.setDiretorRg(trimToNull(request.diretorRg()));
        historico.setGerenteOrganizacaoNome(trimToNull(request.gerenteOrganizacaoNome()));
        historico.setGerenteOrganizacaoRg(trimToNull(request.gerenteOrganizacaoRg()));
        historico.setDoeNumero(trimToNull(request.doeNumero()));
        historico.setDoeData(request.doeData());
        historico.setDoeVolume(trimToNull(request.doeVolume()));
        historico.setDoePagina(trimToNull(request.doePagina()));
        historico.setObservacoes(trimToNull(request.observacoes()));
        historico.replaceComponentesCurriculares(toItemEntities(request.componentesCurriculares()));
    }

    public HistoricoEscolarResponse toResponse(HistoricoEscolar entity) {
        return new HistoricoEscolarResponse(
                entity.getId(),
                entity.getNomeAluno(),
                entity.getRgRen(),
                entity.getRa(),
                entity.getRm(),
                entity.getDataNascimento(),
                entity.getMunicipioNascimento(),
                entity.getEstadoNascimento(),
                entity.getPaisNascimento(),
                entity.getNomeEscola(),
                entity.getEnderecoEscola(),
                entity.getMunicipioEscola(),
                entity.getCepEscola(),
                entity.getTelefoneEscola(),
                entity.getEmailEscola(),
                entity.getAnoConclusao(),
                entity.getEnsinoConcluido(),
                entity.getDataEmissao(),
                entity.getDiretorNome(),
                entity.getDiretorRg(),
                entity.getGerenteOrganizacaoNome(),
                entity.getGerenteOrganizacaoRg(),
                entity.getDoeNumero(),
                entity.getDoeData(),
                entity.getDoeVolume(),
                entity.getDoePagina(),
                entity.getObservacoes(),
                entity.getComponentesCurriculares().stream()
                        .sorted(Comparator
                                .comparing(HistoricoEscolarItem::getAnoLetivo, Comparator.nullsLast(Integer::compareTo))
                                .thenComparing(HistoricoEscolarItem::getComponenteCurricular, String.CASE_INSENSITIVE_ORDER))
                        .map(this::toItemResponse)
                        .toList());
    }

    private List<HistoricoEscolarItem> toItemEntities(List<HistoricoEscolarItemRequest> requests) {
        if (requests == null) {
            return List.of();
        }
        return requests.stream().map(this::toItemEntity).toList();
    }

    private HistoricoEscolarItem toItemEntity(HistoricoEscolarItemRequest request) {
        return HistoricoEscolarItem.builder()
                .componenteCurricular(request.componenteCurricular().trim())
                .anoLetivo(request.anoLetivo())
                .serie(trimToNull(request.serie()))
                .ciclo(trimToNull(request.ciclo()))
                .notaConceito(trimToNull(request.notaConceito()))
                .totalAulas(request.totalAulas())
                .cargaHoraria(request.cargaHoraria())
                .build();
    }

    private HistoricoEscolarItemResponse toItemResponse(HistoricoEscolarItem entity) {
        return new HistoricoEscolarItemResponse(
                entity.getId(),
                entity.getComponenteCurricular(),
                entity.getAnoLetivo(),
                entity.getSerie(),
                entity.getCiclo(),
                entity.getNotaConceito(),
                entity.getTotalAulas(),
                entity.getCargaHoraria());
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
