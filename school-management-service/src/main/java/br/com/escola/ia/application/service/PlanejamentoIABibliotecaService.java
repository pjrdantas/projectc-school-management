package br.com.escola.ia.application.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import br.com.escola.ia.adapter.out.persistence.entity.BibliotecaConteudoPedagogicoEntity;
import br.com.escola.ia.adapter.out.persistence.repository.BibliotecaConteudoPedagogicoJpaRepository;
import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaPublicacaoResumo;
import br.com.escola.ia.application.dto.internal.PlanejamentoIABibliotecaResumo;
import br.com.escola.ia.application.port.internal.PlanejamentoIABibliotecaPort;

@Service
public class PlanejamentoIABibliotecaService implements PlanejamentoIABibliotecaPort {

    private final BibliotecaConteudoPedagogicoJpaRepository bibliotecaJpaRepository;
    private final PlanejamentoIABibliotecaFactory planejamentoIABibliotecaFactory;

    public PlanejamentoIABibliotecaService(
            BibliotecaConteudoPedagogicoJpaRepository bibliotecaJpaRepository,
            PlanejamentoIABibliotecaFactory planejamentoIABibliotecaFactory) {
        this.bibliotecaJpaRepository = bibliotecaJpaRepository;
        this.planejamentoIABibliotecaFactory = planejamentoIABibliotecaFactory;
    }

    @Override
    public PlanejamentoIABibliotecaResumo publicar(PlanejamentoIABibliotecaPublicacaoResumo resumo) {
        BibliotecaConteudoPedagogicoEntity entity = bibliotecaJpaRepository.findFirstPublicadoEquivalente(
                        resumo.professorId(),
                        resumo.disciplinaId(),
                        resumo.tipoConteudoId(),
                        resumo.titulo(),
                        resumo.tema(),
                        resumo.conteudo(),
                        resumo.origem())
                .orElseGet(() -> bibliotecaJpaRepository.save(planejamentoIABibliotecaFactory.criar(resumo)));
        return toResumo(entity);
    }

    @Override
    public List<PlanejamentoIABibliotecaResumo> listar(
            UUID professorId,
            UUID disciplinaId,
            String tipoConteudo,
            String tema,
            UUID escolaId) {
        List<BibliotecaConteudoPedagogicoEntity> conteudos = tema == null
                ? bibliotecaJpaRepository.filtrar(professorId, disciplinaId, tipoConteudo, escolaId)
                : bibliotecaJpaRepository.filtrarPorTema(
                        professorId,
                        disciplinaId,
                        tipoConteudo,
                        "%" + tema.toLowerCase() + "%",
                        escolaId);
        return conteudos.stream()
                .map(this::toResumo)
                .toList();
    }

    private PlanejamentoIABibliotecaResumo toResumo(BibliotecaConteudoPedagogicoEntity entity) {
        return new PlanejamentoIABibliotecaResumo(
                entity.getId(),
                entity.getProfessor() == null || entity.getProfessor().getPessoa() == null || entity.getProfessor().getPessoa().getEscola() == null
                        ? null
                        : entity.getProfessor().getPessoa().getEscola().getId(),
                entity.getProfessor() == null || entity.getProfessor().getPessoa() == null || entity.getProfessor().getPessoa().getEscola() == null
                        ? null
                        : entity.getProfessor().getPessoa().getEscola().getNome(),
                entity.getProfessor() == null ? null : entity.getProfessor().getId(),
                entity.getProfessor() == null ? null : entity.getProfessor().getPessoa().getNomeCompleto(),
                entity.getDisciplina() == null ? null : entity.getDisciplina().getId(),
                entity.getDisciplina() == null ? null : entity.getDisciplina().getNome(),
                entity.getTipoConteudoIA() == null ? null : entity.getTipoConteudoIA().getCodigo(),
                entity.getTipoConteudoIA() == null ? null : entity.getTipoConteudoIA().getDescricao(),
                entity.getTitulo(),
                entity.getTema(),
                entity.getConteudo(),
                entity.getOrigem(),
                entity.getReutilizavel(),
                entity.getAtivo(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
