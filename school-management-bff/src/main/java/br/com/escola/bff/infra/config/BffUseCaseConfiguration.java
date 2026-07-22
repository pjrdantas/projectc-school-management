package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.in.AdministrarAcessoUseCase;
import br.com.escola.bff.application.port.out.AdministracaoAcessoPort;
import br.com.escola.bff.application.port.out.AlunoReadPort;
import br.com.escola.bff.application.port.out.AlunoResponsavelVinculoReadPort;
import br.com.escola.bff.application.port.out.AlunoResponsavelWritePort;
import br.com.escola.bff.application.port.out.AulaPort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.AvaliacaoPort;
import br.com.escola.bff.application.port.out.BoletimReadPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogoDisciplinaWritePort;
import br.com.escola.bff.application.port.out.CatalogoNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.CatalogoPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.CatalogoReadPort;
import br.com.escola.bff.application.port.out.CatalogoSerieWritePort;
import br.com.escola.bff.application.port.out.CatalogoTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.CatalogoTurmaWritePort;
import br.com.escola.bff.application.port.out.CatalogoTurnoResolverPort;
import br.com.escola.bff.application.port.out.ConsultaCadastralReadPort;
import br.com.escola.bff.application.port.out.DiarioClasseReadPort;
import br.com.escola.bff.application.port.out.DiarioClasseWritePort;
import br.com.escola.bff.application.port.out.DocumentoAlunoMatriculaReadPort;
import br.com.escola.bff.application.port.out.DocumentoAlunoMatriculaWritePort;
import br.com.escola.bff.application.port.out.DocumentoMatriculaReadPort;
import br.com.escola.bff.application.port.out.DocumentoWritePort;
import br.com.escola.bff.application.port.out.EscolaOrigemMatriculaReadPort;
import br.com.escola.bff.application.port.out.EscolaOrigemMatriculaWritePort;
import br.com.escola.bff.application.port.out.FuncionarioCadastroReadPort;
import br.com.escola.bff.application.port.out.HistoricoEscolarReadPort;
import br.com.escola.bff.application.port.out.HistoricoEscolarWritePort;
import br.com.escola.bff.application.port.out.IdentityTenantAuthContextPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.InternalAuthContextPort;
import br.com.escola.bff.application.port.out.MatriculaDocumentoReadPort;
import br.com.escola.bff.application.port.out.MatriculaWritePort;
import br.com.escola.bff.application.port.out.PainelAcademicoReadPort;
import br.com.escola.bff.application.port.out.PainelAlertaReadPort;
import br.com.escola.bff.application.port.out.PainelConfiguracaoReadPort;
import br.com.escola.bff.application.port.out.PainelDiretorReadPort;
import br.com.escola.bff.application.port.out.PainelFrontendReadPort;
import br.com.escola.bff.application.port.out.PainelIndicadorHistoricoReadPort;
import br.com.escola.bff.application.port.out.PainelIndicadorSnapshotReadPort;
import br.com.escola.bff.application.port.out.PainelProfessorReadPort;
import br.com.escola.bff.application.port.out.PainelPublicoReadPort;
import br.com.escola.bff.application.port.out.PainelSecretariaReadPort;
import br.com.escola.bff.application.port.out.PessoaCadastroReadPort;
import br.com.escola.bff.application.port.out.PessoaCatalogoReadPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaBibliotecaReadPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoBibliotecaWritePort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoDetalheReadPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoReadPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoVersaoApprovePort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoVersaoReadPort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoVersaoWritePort;
import br.com.escola.bff.application.port.out.PlanejamentoIaConteudoWritePort;
import br.com.escola.bff.application.port.out.PlanejamentoIaInteracaoReadPort;
import br.com.escola.bff.application.port.out.ProfessorReadPort;
import br.com.escola.bff.application.port.out.ResponsavelCatalogoReadPort;
import br.com.escola.bff.application.port.out.ResponsavelWritePort;
import br.com.escola.bff.application.port.out.SessaoAutenticadaPort;
import br.com.escola.bff.application.port.out.TenantAtivoReadPort;
import br.com.escola.bff.application.port.out.TransferenciaMatriculaReadPort;
import br.com.escola.bff.application.port.out.TransferenciaMatriculaWritePort;
import br.com.escola.bff.application.service.AdministracaoAcessoProxyService;
import br.com.escola.bff.application.service.AlunoReadProxyService;
import br.com.escola.bff.application.service.AlunoResponsavelReadProxyService;
import br.com.escola.bff.application.service.AlunoResponsavelWriteProxyService;
import br.com.escola.bff.application.service.AulaReadProxyService;
import br.com.escola.bff.application.service.AulaWriteProxyService;
import br.com.escola.bff.application.service.AutenticacaoProxyService;
import br.com.escola.bff.application.service.AuthSessionProxyService;
import br.com.escola.bff.application.service.AvaliacaoReadProxyService;
import br.com.escola.bff.application.service.AvaliacaoWriteProxyService;
import br.com.escola.bff.application.service.BibliotecaConteudoPedagogicoReadProxyService;
import br.com.escola.bff.application.service.BoletimReadProxyService;
import br.com.escola.bff.application.service.CadastroPessoaReadProxyService;
import br.com.escola.bff.application.service.CatalogReadRoutingService;
import br.com.escola.bff.application.service.DiarioClasseReadProxyService;
import br.com.escola.bff.application.service.DiarioClasseWriteProxyService;
import br.com.escola.bff.application.service.DisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.DocumentoAlunoReadProxyService;
import br.com.escola.bff.application.service.DocumentoAlunoWriteProxyService;
import br.com.escola.bff.application.service.DocumentoReadProxyService;
import br.com.escola.bff.application.service.DocumentoWriteProxyService;
import br.com.escola.bff.application.service.EscolaOrigemReadProxyService;
import br.com.escola.bff.application.service.EscolaOrigemWriteProxyService;
import br.com.escola.bff.application.service.FuncionarioReadProxyService;
import br.com.escola.bff.application.service.HistoricoEscolarReadProxyService;
import br.com.escola.bff.application.service.HistoricoEscolarWriteProxyService;
import br.com.escola.bff.application.service.MatriculaReadProxyService;
import br.com.escola.bff.application.service.MatriculaWriteProxyService;
import br.com.escola.bff.application.service.PainelAcademicoReadProxyService;
import br.com.escola.bff.application.service.PainelAlertaReadProxyService;
import br.com.escola.bff.application.service.PainelConfiguracaoReadProxyService;
import br.com.escola.bff.application.service.PainelDiretorReadProxyService;
import br.com.escola.bff.application.service.PainelFrontendReadProxyService;
import br.com.escola.bff.application.service.PainelIndicadorHistoricoReadProxyService;
import br.com.escola.bff.application.service.PainelIndicadorSnapshotReadProxyService;
import br.com.escola.bff.application.service.PainelProfessorReadProxyService;
import br.com.escola.bff.application.service.PainelPublicoReadProxyService;
import br.com.escola.bff.application.service.PainelSecretariaReadProxyService;
import br.com.escola.bff.application.service.PeriodoLetivoWriteRoutingService;
import br.com.escola.bff.application.service.PessoaCatalogReadProxyService;
import br.com.escola.bff.application.service.PessoaDetailReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoBibliotecaWriteProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoDetailReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoVersaoApproveProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoVersaoReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoVersaoWriteProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoWriteProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaInteracaoReadProxyService;
import br.com.escola.bff.application.service.ProfessorReadProxyService;
import br.com.escola.bff.application.service.ResponsavelReadProxyService;
import br.com.escola.bff.application.service.ResponsavelWriteProxyService;
import br.com.escola.bff.application.service.SerieWriteRoutingService;
import br.com.escola.bff.application.service.TenantAtivoReadProxyService;
import br.com.escola.bff.application.service.TransferenciaReadProxyService;
import br.com.escola.bff.application.service.TransferenciaWriteProxyService;
import br.com.escola.bff.application.service.TurmaDisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.TurmaWriteRoutingService;
import br.com.escola.bff.application.usecase.AprovarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.application.usecase.AtualizarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.ConsultarAlunoUseCase;
import br.com.escola.bff.application.usecase.ConsultarAlunoResponsavelUseCase;
import br.com.escola.bff.application.usecase.AlunoResponsavelWriteUseCase;
import br.com.escola.bff.application.usecase.ConsultarAulaUseCase;
import br.com.escola.bff.application.usecase.ConsultarAuthSessionUseCase;
import br.com.escola.bff.application.usecase.ConsultarAvaliacaoUseCase;
import br.com.escola.bff.application.usecase.ConsultarBibliotecaConteudoPedagogicoUseCase;
import br.com.escola.bff.application.usecase.ConsultarBoletimUseCase;
import br.com.escola.bff.application.usecase.ConsultarCadastroPessoaUseCase;
import br.com.escola.bff.application.usecase.ConsultarDiarioClasseUseCase;
import br.com.escola.bff.application.usecase.ConsultarDocumentoAlunoUseCase;
import br.com.escola.bff.application.usecase.DocumentoAlunoWriteUseCase;
import br.com.escola.bff.application.usecase.ConsultarDocumentoUseCase;
import br.com.escola.bff.application.usecase.DocumentoWriteUseCase;
import br.com.escola.bff.application.usecase.ConsultarEscolaOrigemUseCase;
import br.com.escola.bff.application.usecase.ConsultarFuncionarioUseCase;
import br.com.escola.bff.application.usecase.ConsultarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.ConsultarMatriculaUseCase;
import br.com.escola.bff.application.usecase.MatriculaWriteUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelAcademicoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelAlertaUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelDiretorUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelFrontendUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelIndicadorHistoricoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelProfessorUseCase;
import br.com.escola.bff.application.usecase.ConsultarPainelSecretariaUseCase;
import br.com.escola.bff.application.usecase.ConsultarPessoaCatalogoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPessoaDetalheUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoDetailUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaInteracaoUseCase;
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import br.com.escola.bff.application.usecase.ConsultarResponsavelUseCase;
import br.com.escola.bff.application.usecase.ResponsavelWriteUseCase;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import br.com.escola.bff.application.usecase.ConsultarTransferenciaUseCase;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
import br.com.escola.bff.application.usecase.CreateTurmaUseCase;
import br.com.escola.bff.application.usecase.CriarAulaUseCase;
import br.com.escola.bff.application.usecase.CriarAvaliacaoUseCase;
import br.com.escola.bff.application.usecase.CriarEscolaOrigemUseCase;
import br.com.escola.bff.application.usecase.CriarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.application.usecase.CriarTransferenciaUseCase;
import br.com.escola.bff.application.usecase.GerenciarAutenticacaoUseCase;
import br.com.escola.bff.application.usecase.LinkTurmaDisciplinaUseCase;
import br.com.escola.bff.application.usecase.ListarPainelConfiguracaoUseCase;
import br.com.escola.bff.application.usecase.ListarPainelIndicadorSnapshotUseCase;
import br.com.escola.bff.application.usecase.ListarPainelPublicoUseCase;
import br.com.escola.bff.application.usecase.PublicarPlanejamentoIaConteudoBibliotecaUseCase;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import br.com.escola.bff.application.usecase.SalvarDiarioClasseUseCase;
import br.com.escola.bff.application.usecase.SelecionarEscolaAtivaUseCase;

@Configuration
public class BffUseCaseConfiguration {

    @Bean
    ConsultarPessoaCatalogoUseCase consultarPessoaCatalogoUseCase(
            InternalAuthContextPort authContextPort,
            PessoaCatalogoReadPort peopleCatalogReadPort) {
        return new PessoaCatalogReadProxyService(authContextPort, peopleCatalogReadPort);
    }

    @Bean
    ConsultarCadastroPessoaUseCase consultarCadastroPessoaUseCase(
            InternalAuthContextPort authContextPort,
            ConsultaCadastralReadPort peopleCadastroReadPort) {
        return new CadastroPessoaReadProxyService(authContextPort, peopleCadastroReadPort);
    }

    @Bean
    AdministrarAcessoUseCase administrarAcessoUseCase(
            InternalAuthContextPort authContextPort,
            AdministracaoAcessoPort administracaoAcessoPort) {
        return new AdministracaoAcessoProxyService(authContextPort, administracaoAcessoPort);
    }

    @Bean
    GerenciarAutenticacaoUseCase gerenciarAutenticacaoUseCase(
            SessaoAutenticadaPort sessaoAutenticadaPort) {
        return new AutenticacaoProxyService(sessaoAutenticadaPort);
    }

    @Bean
    ConsultarAuthSessionUseCase consultarAuthSessionUseCase(
            IdentityTenantAuthContextPort authContextPort,
            SessaoAutenticadaPort identityAccessSessionPort,
            TenantAtivoReadPort institutionalTenantReadPort,
            IdentityTenantObservabilityPort observabilityPort) {
        return new AuthSessionProxyService(
                authContextPort,
                identityAccessSessionPort,
                institutionalTenantReadPort,
                observabilityPort);
    }

    @Bean
    SelecionarEscolaAtivaUseCase selecionarEscolaAtivaUseCase(
            IdentityTenantAuthContextPort authContextPort,
            SessaoAutenticadaPort identityAccessSessionPort,
            TenantAtivoReadPort institutionalTenantReadPort,
            IdentityTenantObservabilityPort observabilityPort) {
        return new AuthSessionProxyService(
                authContextPort,
                identityAccessSessionPort,
                institutionalTenantReadPort,
                observabilityPort);
    }

    @Bean
    ConsultarTenantAtivoUseCase consultarTenantAtivoUseCase(
            IdentityTenantAuthContextPort authContextPort,
            TenantAtivoReadPort institutionalTenantReadPort,
            IdentityTenantObservabilityPort observabilityPort) {
        return new TenantAtivoReadProxyService(
                authContextPort,
                institutionalTenantReadPort,
                observabilityPort);
    }

    @Bean
    ConsultarAlunoResponsavelUseCase consultarAlunoResponsavelUseCase(
            InternalAuthContextPort authContextPort,
            AlunoResponsavelVinculoReadPort responsiblesAlunoResponsavelReadPort) {
        return new AlunoResponsavelReadProxyService(
                authContextPort,
                responsiblesAlunoResponsavelReadPort);
    }

    @Bean
    AlunoResponsavelWriteUseCase alunoResponsavelWriteUseCase(
            AuthContextPort authContextPort,
            AlunoResponsavelWritePort alunoResponsavelWritePort) {
        return new AlunoResponsavelWriteProxyService(authContextPort, alunoResponsavelWritePort);
    }

    @Bean
    ConsultarAlunoUseCase consultarAlunoUseCase(
            InternalAuthContextPort authContextPort,
            AlunoReadPort peopleAlunoReadPort) {
        return new AlunoReadProxyService(authContextPort, peopleAlunoReadPort);
    }

    @Bean
    ConsultarResponsavelUseCase consultarResponsavelUseCase(
            InternalAuthContextPort authContextPort,
            ResponsavelCatalogoReadPort responsiblesReadPort) {
        return new ResponsavelReadProxyService(
                authContextPort,
                responsiblesReadPort);
    }

    @Bean
    ResponsavelWriteUseCase responsavelWriteUseCase(
            AuthContextPort authContextPort,
            ResponsavelWritePort responsavelWritePort) {
        return new ResponsavelWriteProxyService(authContextPort, responsavelWritePort);
    }

    @Bean
    ConsultarPainelAcademicoUseCase consultarPainelAcademicoUseCase(
            InternalAuthContextPort authContextPort,
            PainelAcademicoReadPort dashboardAcademicoReadPort) {
        return new PainelAcademicoReadProxyService(
                authContextPort,
                dashboardAcademicoReadPort);
    }

    @Bean
    ConsultarPainelAlertaUseCase consultarPainelAlertaUseCase(
            InternalAuthContextPort authContextPort,
            PainelAlertaReadPort dashboardAlertaReadPort) {
        return new PainelAlertaReadProxyService(
                authContextPort,
                dashboardAlertaReadPort);
    }

    @Bean
    ConsultarPainelFrontendUseCase consultarPainelFrontendUseCase(
            InternalAuthContextPort authContextPort,
            PainelFrontendReadPort dashboardFrontendReadPort) {
        return new PainelFrontendReadProxyService(
                authContextPort,
                dashboardFrontendReadPort);
    }

    @Bean
    ListarPainelIndicadorSnapshotUseCase listarPainelIndicadorSnapshotUseCase(
            InternalAuthContextPort authContextPort,
            PainelIndicadorSnapshotReadPort dashboardIndicadorSnapshotReadPort) {
        return new PainelIndicadorSnapshotReadProxyService(
                authContextPort,
                dashboardIndicadorSnapshotReadPort);
    }

    @Bean
    ConsultarPainelIndicadorHistoricoUseCase consultarPainelIndicadorHistoricoUseCase(
            InternalAuthContextPort authContextPort,
            PainelIndicadorHistoricoReadPort dashboardIndicadorHistoricoReadPort) {
        return new PainelIndicadorHistoricoReadProxyService(
                authContextPort,
                dashboardIndicadorHistoricoReadPort);
    }

    @Bean
    ListarPainelPublicoUseCase listarPainelPublicoUseCase(
            InternalAuthContextPort authContextPort,
            PainelPublicoReadPort dashboardPublicoReadPort) {
        return new PainelPublicoReadProxyService(
                authContextPort,
                dashboardPublicoReadPort);
    }

    @Bean
    ListarPainelConfiguracaoUseCase listarPainelConfiguracaoUseCase(
            InternalAuthContextPort authContextPort,
            PainelConfiguracaoReadPort dashboardConfiguracaoReadPort) {
        return new PainelConfiguracaoReadProxyService(
                authContextPort,
                dashboardConfiguracaoReadPort);
    }

    @Bean
    ConsultarPainelSecretariaUseCase consultarPainelSecretariaUseCase(
            InternalAuthContextPort authContextPort,
            PainelSecretariaReadPort dashboardSecretariaReadPort) {
        return new PainelSecretariaReadProxyService(
                authContextPort,
                dashboardSecretariaReadPort);
    }

    @Bean
    ConsultarPainelDiretorUseCase consultarPainelDiretorUseCase(
            InternalAuthContextPort authContextPort,
            PainelDiretorReadPort dashboardDiretorReadPort) {
        return new PainelDiretorReadProxyService(
                authContextPort,
                dashboardDiretorReadPort);
    }

    @Bean
    ConsultarPainelProfessorUseCase consultarPainelProfessorUseCase(
            InternalAuthContextPort authContextPort,
            PainelProfessorReadPort dashboardProfessorReadPort) {
        return new PainelProfessorReadProxyService(
                authContextPort,
                dashboardProfessorReadPort);
    }

    @Bean
    ConsultarBoletimUseCase consultarBoletimUseCase(
            InternalAuthContextPort authContextPort,
            BoletimReadPort pedagogicalBoletimReadPort) {
        return new BoletimReadProxyService(
                authContextPort,
                pedagogicalBoletimReadPort);
    }

    @Bean
    ConsultarAvaliacaoUseCase consultarAvaliacaoUseCase(
            InternalAuthContextPort authContextPort,
            AvaliacaoPort pedagogicalAvaliacaoPort) {
        return new AvaliacaoReadProxyService(
                authContextPort,
                pedagogicalAvaliacaoPort);
    }

    @Bean
    CriarAvaliacaoUseCase criarAvaliacaoUseCase(
            AuthContextPort authContextPort,
            AvaliacaoPort pedagogicalAvaliacaoPort) {
        return new AvaliacaoWriteProxyService(authContextPort, pedagogicalAvaliacaoPort);
    }

    @Bean
    ConsultarAulaUseCase consultarAulaUseCase(
            InternalAuthContextPort authContextPort,
            AulaPort pedagogicalAulaPort) {
        return new AulaReadProxyService(
                authContextPort,
                pedagogicalAulaPort);
    }

    @Bean
    CriarAulaUseCase criarAulaUseCase(
            AuthContextPort authContextPort,
            AulaPort pedagogicalAulaPort) {
        return new AulaWriteProxyService(authContextPort, pedagogicalAulaPort);
    }

    @Bean
    ConsultarDiarioClasseUseCase consultarDiarioClasseUseCase(
            InternalAuthContextPort authContextPort,
            DiarioClasseReadPort pedagogicalDiarioClasseReadPort) {
        return new DiarioClasseReadProxyService(
                authContextPort,
                pedagogicalDiarioClasseReadPort);
    }

    @Bean
    SalvarDiarioClasseUseCase salvarDiarioClasseUseCase(
            AuthContextPort authContextPort,
            DiarioClasseWritePort pedagogicalDiarioClasseWritePort) {
        return new DiarioClasseWriteProxyService(authContextPort, pedagogicalDiarioClasseWritePort);
    }

    @Bean
    ConsultarHistoricoEscolarUseCase consultarHistoricoEscolarUseCase(
            InternalAuthContextPort authContextPort,
            HistoricoEscolarReadPort pedagogicalHistoricoEscolarReadPort) {
        return new HistoricoEscolarReadProxyService(
                authContextPort,
                pedagogicalHistoricoEscolarReadPort);
    }

    @Bean
    CriarHistoricoEscolarUseCase criarHistoricoEscolarUseCase(
            AuthContextPort authContextPort,
            HistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort) {
        return new HistoricoEscolarWriteProxyService(authContextPort, pedagogicalHistoricoEscolarWritePort);
    }

    @Bean
    AtualizarHistoricoEscolarUseCase atualizarHistoricoEscolarUseCase(
            AuthContextPort authContextPort,
            HistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort) {
        return new HistoricoEscolarWriteProxyService(authContextPort, pedagogicalHistoricoEscolarWritePort);
    }

    @Bean
    ConsultarPessoaDetalheUseCase consultarPessoaDetalheUseCase(
            InternalAuthContextPort authContextPort,
            PessoaCadastroReadPort peoplePessoaReadPort) {
        return new PessoaDetailReadProxyService(authContextPort, peoplePessoaReadPort);
    }

    @Bean
    ConsultarFuncionarioUseCase consultarFuncionarioUseCase(
            InternalAuthContextPort authContextPort,
            FuncionarioCadastroReadPort peopleFuncionarioReadPort) {
        return new FuncionarioReadProxyService(authContextPort, peopleFuncionarioReadPort);
    }

    @Bean
    ConsultarProfessorUseCase consultarProfessorUseCase(
            InternalAuthContextPort authContextPort,
            ProfessorReadPort professorReadPort) {
        return new ProfessorReadProxyService(authContextPort, professorReadPort);
    }

    @Bean
    ConsultarEscolaOrigemUseCase consultarEscolaOrigemUseCase(
            InternalAuthContextPort authContextPort,
            EscolaOrigemMatriculaReadPort enrollmentDocumentEscolaOrigemReadPort) {
        return new EscolaOrigemReadProxyService(authContextPort, enrollmentDocumentEscolaOrigemReadPort);
    }

    @Bean
    ConsultarTransferenciaUseCase consultarTransferenciaUseCase(
            InternalAuthContextPort authContextPort,
            TransferenciaMatriculaReadPort enrollmentDocumentTransferenciaReadPort) {
        return new TransferenciaReadProxyService(authContextPort, enrollmentDocumentTransferenciaReadPort);
    }

    @Bean
    ConsultarDocumentoAlunoUseCase consultarDocumentoAlunoUseCase(
            InternalAuthContextPort authContextPort,
            DocumentoAlunoMatriculaReadPort enrollmentDocumentAlunoReadPort) {
        return new DocumentoAlunoReadProxyService(authContextPort, enrollmentDocumentAlunoReadPort);
    }

    @Bean
    DocumentoAlunoWriteUseCase documentoAlunoWriteUseCase(
            AuthContextPort authContextPort,
            DocumentoAlunoMatriculaWritePort enrollmentDocumentAlunoWritePort) {
        return new DocumentoAlunoWriteProxyService(authContextPort, enrollmentDocumentAlunoWritePort);
    }

    @Bean
    ConsultarDocumentoUseCase consultarDocumentoUseCase(
            InternalAuthContextPort authContextPort,
            DocumentoMatriculaReadPort enrollmentDocumentReadPort) {
        return new DocumentoReadProxyService(authContextPort, enrollmentDocumentReadPort);
    }

    @Bean
    DocumentoWriteUseCase documentoWriteUseCase(
            AuthContextPort authContextPort,
            DocumentoWritePort enrollmentDocumentWritePort) {
        return new DocumentoWriteProxyService(authContextPort, enrollmentDocumentWritePort);
    }

    @Bean
    ConsultarBibliotecaConteudoPedagogicoUseCase consultarBibliotecaConteudoPedagogicoUseCase(
            InternalAuthContextPort authContextPort,
            PlanejamentoIaBibliotecaReadPort planningAiBibliotecaReadPort) {
        return new BibliotecaConteudoPedagogicoReadProxyService(
                authContextPort,
                planningAiBibliotecaReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaInteracaoUseCase consultarPlanejamentoIaInteracaoUseCase(
            InternalAuthContextPort authContextPort,
            PlanejamentoIaInteracaoReadPort planningAiInteracaoReadPort) {
        return new PlanejamentoIaInteracaoReadProxyService(
                authContextPort,
                planningAiInteracaoReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaConteudoUseCase consultarPlanejamentoIaConteudoUseCase(
            InternalAuthContextPort authContextPort,
            PlanejamentoIaConteudoReadPort planningAiConteudoReadPort) {
        return new PlanejamentoIaConteudoReadProxyService(
                authContextPort,
                planningAiConteudoReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaConteudoDetailUseCase consultarPlanejamentoIaConteudoDetailUseCase(
            InternalAuthContextPort authContextPort,
            PlanejamentoIaConteudoDetalheReadPort planningAiConteudoDetailReadPort) {
        return new PlanejamentoIaConteudoDetailReadProxyService(
                authContextPort,
                planningAiConteudoDetailReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaConteudoVersaoUseCase consultarPlanejamentoIaConteudoVersaoUseCase(
            InternalAuthContextPort authContextPort,
            PlanejamentoIaConteudoVersaoReadPort planningAiConteudoVersaoReadPort) {
        return new PlanejamentoIaConteudoVersaoReadProxyService(
                authContextPort,
                planningAiConteudoVersaoReadPort);
    }

    @Bean
    CriarPlanejamentoIaConteudoUseCase criarPlanejamentoIaConteudoUseCase(
            AuthContextPort authContextPort,
            PlanejamentoIaConteudoWritePort planningAiConteudoWritePort) {
        return new PlanejamentoIaConteudoWriteProxyService(
                authContextPort,
                planningAiConteudoWritePort);
    }

    @Bean
    CriarPlanejamentoIaConteudoVersaoUseCase criarPlanejamentoIaConteudoVersaoUseCase(
            AuthContextPort authContextPort,
            PlanejamentoIaConteudoVersaoWritePort planningAiConteudoVersaoWritePort) {
        return new PlanejamentoIaConteudoVersaoWriteProxyService(
                authContextPort,
                planningAiConteudoVersaoWritePort);
    }

    @Bean
    AprovarPlanejamentoIaConteudoVersaoUseCase aprovarPlanejamentoIaConteudoVersaoUseCase(
            AuthContextPort authContextPort,
            PlanejamentoIaConteudoVersaoApprovePort planningAiConteudoVersaoApprovePort) {
        return new PlanejamentoIaConteudoVersaoApproveProxyService(
                authContextPort,
                planningAiConteudoVersaoApprovePort);
    }

    @Bean
    PublicarPlanejamentoIaConteudoBibliotecaUseCase publicarPlanejamentoIaConteudoBibliotecaUseCase(
            AuthContextPort authContextPort,
            PlanejamentoIaConteudoBibliotecaWritePort planningAiConteudoBibliotecaWritePort) {
        return new PlanejamentoIaConteudoBibliotecaWriteProxyService(
                authContextPort,
                planningAiConteudoBibliotecaWritePort);
    }

    @Bean
    ConsultarMatriculaUseCase consultarMatriculaUseCase(
            InternalAuthContextPort authContextPort,
            MatriculaDocumentoReadPort enrollmentDocumentMatriculaReadPort) {
        return new MatriculaReadProxyService(authContextPort, enrollmentDocumentMatriculaReadPort);
    }

    @Bean
    CriarEscolaOrigemUseCase criarEscolaOrigemUseCase(
            AuthContextPort authContextPort,
            EscolaOrigemMatriculaWritePort enrollmentDocumentEscolaOrigemWritePort) {
        return new EscolaOrigemWriteProxyService(authContextPort, enrollmentDocumentEscolaOrigemWritePort);
    }

    @Bean
    CriarTransferenciaUseCase criarTransferenciaUseCase(
            AuthContextPort authContextPort,
            TransferenciaMatriculaWritePort enrollmentDocumentTransferenciaWritePort) {
        return new TransferenciaWriteProxyService(authContextPort, enrollmentDocumentTransferenciaWritePort);
    }

    @Bean
    MatriculaWriteUseCase matriculaWriteUseCase(
            AuthContextPort authContextPort,
            MatriculaWritePort enrollmentDocumentMatriculaWritePort) {
        return new MatriculaWriteProxyService(authContextPort, enrollmentDocumentMatriculaWritePort);
    }

    @Bean
    RouteCatalogReadUseCase routeCatalogReadUseCase(
            CatalogoReadPort academicCatalogReadPort,
            InternalAuthContextPort authContextPort,
            CatalogReadObservabilityPort observabilityPort) {
        return new CatalogReadRoutingService(
                academicCatalogReadPort,
                authContextPort,
                observabilityPort);
    }

    @Bean
    CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase(
            CatalogoPeriodoLetivoWritePort academicCatalogPeriodoLetivoWritePort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new PeriodoLetivoWriteRoutingService(
                academicCatalogPeriodoLetivoWritePort,
                authContextPort,
                observabilityPort);
    }

    @Bean
    CreateDisciplinaUseCase createDisciplinaUseCase(
            CatalogoDisciplinaWritePort academicCatalogDisciplinaWritePort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new DisciplinaWriteRoutingService(
                academicCatalogDisciplinaWritePort,
                authContextPort,
                observabilityPort);
    }

    @Bean
    CreateSerieUseCase createSerieUseCase(
            CatalogoSerieWritePort academicCatalogSerieWritePort,
            CatalogoNivelEnsinoResolverPort academicCatalogNivelEnsinoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new SerieWriteRoutingService(
                academicCatalogSerieWritePort,
                academicCatalogNivelEnsinoResolverPort,
                authContextPort,
                observabilityPort);
    }

    @Bean
    CreateTurmaUseCase createTurmaUseCase(
            CatalogoTurmaWritePort academicCatalogTurmaWritePort,
            CatalogoTurnoResolverPort academicCatalogTurnoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new TurmaWriteRoutingService(
                academicCatalogTurmaWritePort,
                academicCatalogTurnoResolverPort,
                authContextPort,
                observabilityPort);
    }

    @Bean
    LinkTurmaDisciplinaUseCase linkTurmaDisciplinaUseCase(
            CatalogoTurmaDisciplinaWritePort academicCatalogTurmaDisciplinaWritePort,
            AuthContextPort authContextPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new TurmaDisciplinaWriteRoutingService(
                academicCatalogTurmaDisciplinaWritePort,
                authContextPort,
                observabilityPort);
    }
}

