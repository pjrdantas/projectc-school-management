package br.com.escola.bff.infra.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.escola.bff.application.port.out.AcademicCatalogPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogNivelEnsinoResolverPort;
import br.com.escola.bff.application.port.out.AcademicCatalogSerieWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogReadPort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurnoResolverPort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.AcademicCatalogTurmaWritePort;
import br.com.escola.bff.application.port.out.AuthContextPort;
import br.com.escola.bff.application.port.out.CatalogReadCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogReadObservabilityPort;
import br.com.escola.bff.application.port.out.CatalogWriteCutoverPolicyPort;
import br.com.escola.bff.application.port.out.CatalogWriteObservabilityPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentAlunoReadPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentReadPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentEscolaOrigemReadPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentEscolaOrigemWritePort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentMatriculaReadPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentTransferenciaReadPort;
import br.com.escola.bff.application.port.out.EnrollmentDocumentTransferenciaWritePort;
import br.com.escola.bff.application.port.out.IdentityAccessSessionPort;
import br.com.escola.bff.application.port.out.IdentityTenantCutoverPolicyPort;
import br.com.escola.bff.application.port.out.IdentityTenantObservabilityPort;
import br.com.escola.bff.application.port.out.InstitutionalTenantReadPort;
import br.com.escola.bff.application.port.out.MonolithCatalogReadPort;
import br.com.escola.bff.application.port.out.MonolithAuthSessionPort;
import br.com.escola.bff.application.port.out.MonolithDisciplinaWritePort;
import br.com.escola.bff.application.port.out.MonolithPeriodoLetivoWritePort;
import br.com.escola.bff.application.port.out.MonolithSerieWritePort;
import br.com.escola.bff.application.port.out.MonolithTenantReadPort;
import br.com.escola.bff.application.port.out.MonolithTurmaDisciplinaWritePort;
import br.com.escola.bff.application.port.out.MonolithTurmaWritePort;
import br.com.escola.bff.application.port.out.PeopleCadastroReadPort;
import br.com.escola.bff.application.port.out.PeopleAlunoResponsavelReadPort;
import br.com.escola.bff.application.port.out.PeopleCatalogReadPort;
import br.com.escola.bff.application.port.out.PeopleFuncionarioReadPort;
import br.com.escola.bff.application.port.out.PeoplePessoaReadPort;
import br.com.escola.bff.application.port.out.PedagogicalAvaliacaoPort;
import br.com.escola.bff.application.port.out.PedagogicalAulaPort;
import br.com.escola.bff.application.port.out.PedagogicalBoletimReadPort;
import br.com.escola.bff.application.port.out.PedagogicalDiarioClasseReadPort;
import br.com.escola.bff.application.port.out.PedagogicalDiarioClasseWritePort;
import br.com.escola.bff.application.port.out.PedagogicalHistoricoEscolarReadPort;
import br.com.escola.bff.application.port.out.PedagogicalHistoricoEscolarWritePort;
import br.com.escola.bff.application.port.out.PlanningAiBibliotecaReadPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoReadPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoDetailReadPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoVersaoReadPort;
import br.com.escola.bff.application.port.out.PlanningAiConteudoWritePort;
import br.com.escola.bff.application.port.out.PlanningAiInteracaoReadPort;
import br.com.escola.bff.application.service.AvaliacaoProxyService;
import br.com.escola.bff.application.service.AulaProxyService;
import br.com.escola.bff.application.service.AlunoResponsavelReadProxyService;
import br.com.escola.bff.application.service.AuthSessionProxyService;
import br.com.escola.bff.application.service.BibliotecaConteudoPedagogicoReadProxyService;
import br.com.escola.bff.application.service.BoletimReadProxyService;
import br.com.escola.bff.application.service.CadastroPessoaReadProxyService;
import br.com.escola.bff.application.service.CatalogReadRoutingService;
import br.com.escola.bff.application.service.DisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.DocumentoAlunoReadProxyService;
import br.com.escola.bff.application.service.DocumentoReadProxyService;
import br.com.escola.bff.application.service.DiarioClasseReadProxyService;
import br.com.escola.bff.application.service.DiarioClasseWriteProxyService;
import br.com.escola.bff.application.service.EscolaOrigemReadProxyService;
import br.com.escola.bff.application.service.EscolaOrigemWriteProxyService;
import br.com.escola.bff.application.service.FuncionarioReadProxyService;
import br.com.escola.bff.application.service.HistoricoEscolarReadProxyService;
import br.com.escola.bff.application.service.HistoricoEscolarWriteProxyService;
import br.com.escola.bff.application.service.InstitutionalTenantReadProxyService;
import br.com.escola.bff.application.service.MatriculaReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoDetailReadProxyService;
import br.com.escola.bff.application.service.PeriodoLetivoWriteRoutingService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoVersaoReadProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaConteudoWriteProxyService;
import br.com.escola.bff.application.service.PlanejamentoIaInteracaoReadProxyService;
import br.com.escola.bff.application.service.PessoaCatalogReadProxyService;
import br.com.escola.bff.application.service.PessoaDetailReadProxyService;
import br.com.escola.bff.application.service.ProfessorReadProxyService;
import br.com.escola.bff.application.service.SerieWriteRoutingService;
import br.com.escola.bff.application.service.TransferenciaReadProxyService;
import br.com.escola.bff.application.service.TransferenciaWriteProxyService;
import br.com.escola.bff.application.service.TurmaDisciplinaWriteRoutingService;
import br.com.escola.bff.application.service.TurmaWriteRoutingService;
import br.com.escola.bff.application.usecase.ConsultarCadastroPessoaUseCase;
import br.com.escola.bff.application.usecase.ConsultarAlunoResponsavelUseCase;
import br.com.escola.bff.application.usecase.ConsultarAuthSessionUseCase;
import br.com.escola.bff.application.usecase.ConsultarAvaliacaoUseCase;
import br.com.escola.bff.application.usecase.ConsultarAulaUseCase;
import br.com.escola.bff.application.usecase.ConsultarBibliotecaConteudoPedagogicoUseCase;
import br.com.escola.bff.application.usecase.ConsultarBoletimUseCase;
import br.com.escola.bff.application.usecase.ConsultarDiarioClasseUseCase;
import br.com.escola.bff.application.usecase.ConsultarDocumentoAlunoUseCase;
import br.com.escola.bff.application.usecase.ConsultarDocumentoUseCase;
import br.com.escola.bff.application.usecase.ConsultarEscolaOrigemUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoDetailUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaConteudoVersaoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPlanejamentoIaInteracaoUseCase;
import br.com.escola.bff.application.usecase.CriarPlanejamentoIaConteudoUseCase;
import br.com.escola.bff.application.usecase.CriarAvaliacaoUseCase;
import br.com.escola.bff.application.usecase.CriarAulaUseCase;
import br.com.escola.bff.application.usecase.CriarEscolaOrigemUseCase;
import br.com.escola.bff.application.usecase.CriarTransferenciaUseCase;
import br.com.escola.bff.application.usecase.ConsultarPessoaCatalogoUseCase;
import br.com.escola.bff.application.usecase.ConsultarPessoaDetalheUseCase;
import br.com.escola.bff.application.port.out.PeopleProfessorReadPort;
import br.com.escola.bff.application.usecase.ConsultarFuncionarioUseCase;
import br.com.escola.bff.application.usecase.ConsultarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.CriarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.AtualizarHistoricoEscolarUseCase;
import br.com.escola.bff.application.usecase.SalvarDiarioClasseUseCase;
import br.com.escola.bff.application.usecase.ConsultarMatriculaUseCase;
import br.com.escola.bff.application.usecase.ConsultarProfessorUseCase;
import br.com.escola.bff.application.usecase.ConsultarTenantAtivoUseCase;
import br.com.escola.bff.application.usecase.ConsultarTransferenciaUseCase;
import br.com.escola.bff.application.usecase.CreateDisciplinaUseCase;
import br.com.escola.bff.application.usecase.CreatePeriodoLetivoUseCase;
import br.com.escola.bff.application.usecase.CreateSerieUseCase;
import br.com.escola.bff.application.usecase.CreateTurmaUseCase;
import br.com.escola.bff.application.usecase.LinkTurmaDisciplinaUseCase;
import br.com.escola.bff.application.usecase.RouteCatalogReadUseCase;
import br.com.escola.bff.application.usecase.SelecionarEscolaAtivaUseCase;

@Configuration
public class BffUseCaseConfiguration {

    @Bean
    ConsultarPessoaCatalogoUseCase consultarPessoaCatalogoUseCase(
            AuthContextPort authContextPort,
            PeopleCatalogReadPort peopleCatalogReadPort) {
        return new PessoaCatalogReadProxyService(authContextPort, peopleCatalogReadPort);
    }

    @Bean
    ConsultarCadastroPessoaUseCase consultarCadastroPessoaUseCase(
            AuthContextPort authContextPort,
            PeopleCadastroReadPort peopleCadastroReadPort) {
        return new CadastroPessoaReadProxyService(authContextPort, peopleCadastroReadPort);
    }

    @Bean
    ConsultarAuthSessionUseCase consultarAuthSessionUseCase(
            AuthContextPort authContextPort,
            IdentityAccessSessionPort identityAccessSessionPort,
            MonolithAuthSessionPort monolithAuthSessionPort,
            IdentityTenantCutoverPolicyPort cutoverPolicyPort,
            IdentityTenantObservabilityPort observabilityPort) {
        return new AuthSessionProxyService(
                authContextPort,
                identityAccessSessionPort,
                monolithAuthSessionPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    SelecionarEscolaAtivaUseCase selecionarEscolaAtivaUseCase(
            AuthContextPort authContextPort,
            IdentityAccessSessionPort identityAccessSessionPort,
            MonolithAuthSessionPort monolithAuthSessionPort,
            IdentityTenantCutoverPolicyPort cutoverPolicyPort,
            IdentityTenantObservabilityPort observabilityPort) {
        return new AuthSessionProxyService(
                authContextPort,
                identityAccessSessionPort,
                monolithAuthSessionPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    ConsultarTenantAtivoUseCase consultarTenantAtivoUseCase(
            AuthContextPort authContextPort,
            InstitutionalTenantReadPort institutionalTenantReadPort,
            MonolithTenantReadPort monolithTenantReadPort,
            IdentityTenantCutoverPolicyPort cutoverPolicyPort,
            IdentityTenantObservabilityPort observabilityPort) {
        return new InstitutionalTenantReadProxyService(
                authContextPort,
                institutionalTenantReadPort,
                monolithTenantReadPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    ConsultarAlunoResponsavelUseCase consultarAlunoResponsavelUseCase(
            AuthContextPort authContextPort,
            PeopleAlunoResponsavelReadPort peopleAlunoResponsavelReadPort) {
        return new AlunoResponsavelReadProxyService(authContextPort, peopleAlunoResponsavelReadPort);
    }

    @Bean
    ConsultarBoletimUseCase consultarBoletimUseCase(
            AuthContextPort authContextPort,
            PedagogicalBoletimReadPort pedagogicalBoletimReadPort) {
        return new BoletimReadProxyService(authContextPort, pedagogicalBoletimReadPort);
    }

    @Bean
    ConsultarAvaliacaoUseCase consultarAvaliacaoUseCase(
            AuthContextPort authContextPort,
            PedagogicalAvaliacaoPort pedagogicalAvaliacaoPort) {
        return new AvaliacaoProxyService(authContextPort, pedagogicalAvaliacaoPort);
    }

    @Bean
    CriarAvaliacaoUseCase criarAvaliacaoUseCase(
            AuthContextPort authContextPort,
            PedagogicalAvaliacaoPort pedagogicalAvaliacaoPort) {
        return new AvaliacaoProxyService(authContextPort, pedagogicalAvaliacaoPort);
    }

    @Bean
    ConsultarAulaUseCase consultarAulaUseCase(
            AuthContextPort authContextPort,
            PedagogicalAulaPort pedagogicalAulaPort) {
        return new AulaProxyService(authContextPort, pedagogicalAulaPort);
    }

    @Bean
    CriarAulaUseCase criarAulaUseCase(
            AuthContextPort authContextPort,
            PedagogicalAulaPort pedagogicalAulaPort) {
        return new AulaProxyService(authContextPort, pedagogicalAulaPort);
    }

    @Bean
    ConsultarDiarioClasseUseCase consultarDiarioClasseUseCase(
            AuthContextPort authContextPort,
            PedagogicalDiarioClasseReadPort pedagogicalDiarioClasseReadPort) {
        return new DiarioClasseReadProxyService(authContextPort, pedagogicalDiarioClasseReadPort);
    }

    @Bean
    SalvarDiarioClasseUseCase salvarDiarioClasseUseCase(
            AuthContextPort authContextPort,
            PedagogicalDiarioClasseWritePort pedagogicalDiarioClasseWritePort) {
        return new DiarioClasseWriteProxyService(authContextPort, pedagogicalDiarioClasseWritePort);
    }

    @Bean
    ConsultarHistoricoEscolarUseCase consultarHistoricoEscolarUseCase(
            AuthContextPort authContextPort,
            PedagogicalHistoricoEscolarReadPort pedagogicalHistoricoEscolarReadPort) {
        return new HistoricoEscolarReadProxyService(authContextPort, pedagogicalHistoricoEscolarReadPort);
    }

    @Bean
    CriarHistoricoEscolarUseCase criarHistoricoEscolarUseCase(
            AuthContextPort authContextPort,
            PedagogicalHistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort) {
        return new HistoricoEscolarWriteProxyService(authContextPort, pedagogicalHistoricoEscolarWritePort);
    }

    @Bean
    AtualizarHistoricoEscolarUseCase atualizarHistoricoEscolarUseCase(
            AuthContextPort authContextPort,
            PedagogicalHistoricoEscolarWritePort pedagogicalHistoricoEscolarWritePort) {
        return new HistoricoEscolarWriteProxyService(authContextPort, pedagogicalHistoricoEscolarWritePort);
    }

    @Bean
    ConsultarPessoaDetalheUseCase consultarPessoaDetalheUseCase(
            AuthContextPort authContextPort,
            PeoplePessoaReadPort peoplePessoaReadPort) {
        return new PessoaDetailReadProxyService(authContextPort, peoplePessoaReadPort);
    }

    @Bean
    ConsultarFuncionarioUseCase consultarFuncionarioUseCase(
            AuthContextPort authContextPort,
            PeopleFuncionarioReadPort peopleFuncionarioReadPort) {
        return new FuncionarioReadProxyService(authContextPort, peopleFuncionarioReadPort);
    }

    @Bean
    ConsultarProfessorUseCase consultarProfessorUseCase(
            AuthContextPort authContextPort,
            PeopleProfessorReadPort peopleProfessorReadPort) {
        return new ProfessorReadProxyService(authContextPort, peopleProfessorReadPort);
    }

    @Bean
    ConsultarEscolaOrigemUseCase consultarEscolaOrigemUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentEscolaOrigemReadPort enrollmentDocumentEscolaOrigemReadPort) {
        return new EscolaOrigemReadProxyService(authContextPort, enrollmentDocumentEscolaOrigemReadPort);
    }

    @Bean
    ConsultarTransferenciaUseCase consultarTransferenciaUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentTransferenciaReadPort enrollmentDocumentTransferenciaReadPort) {
        return new TransferenciaReadProxyService(authContextPort, enrollmentDocumentTransferenciaReadPort);
    }

    @Bean
    ConsultarDocumentoAlunoUseCase consultarDocumentoAlunoUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentAlunoReadPort enrollmentDocumentAlunoReadPort) {
        return new DocumentoAlunoReadProxyService(authContextPort, enrollmentDocumentAlunoReadPort);
    }

    @Bean
    ConsultarDocumentoUseCase consultarDocumentoUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentReadPort enrollmentDocumentReadPort) {
        return new DocumentoReadProxyService(authContextPort, enrollmentDocumentReadPort);
    }

    @Bean
    ConsultarBibliotecaConteudoPedagogicoUseCase consultarBibliotecaConteudoPedagogicoUseCase(
            AuthContextPort authContextPort,
            PlanningAiBibliotecaReadPort planningAiBibliotecaReadPort) {
        return new BibliotecaConteudoPedagogicoReadProxyService(
                authContextPort,
                planningAiBibliotecaReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaInteracaoUseCase consultarPlanejamentoIaInteracaoUseCase(
            AuthContextPort authContextPort,
            PlanningAiInteracaoReadPort planningAiInteracaoReadPort) {
        return new PlanejamentoIaInteracaoReadProxyService(
                authContextPort,
                planningAiInteracaoReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaConteudoUseCase consultarPlanejamentoIaConteudoUseCase(
            AuthContextPort authContextPort,
            PlanningAiConteudoReadPort planningAiConteudoReadPort) {
        return new PlanejamentoIaConteudoReadProxyService(
                authContextPort,
                planningAiConteudoReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaConteudoDetailUseCase consultarPlanejamentoIaConteudoDetailUseCase(
            AuthContextPort authContextPort,
            PlanningAiConteudoDetailReadPort planningAiConteudoDetailReadPort) {
        return new PlanejamentoIaConteudoDetailReadProxyService(
                authContextPort,
                planningAiConteudoDetailReadPort);
    }

    @Bean
    ConsultarPlanejamentoIaConteudoVersaoUseCase consultarPlanejamentoIaConteudoVersaoUseCase(
            AuthContextPort authContextPort,
            PlanningAiConteudoVersaoReadPort planningAiConteudoVersaoReadPort) {
        return new PlanejamentoIaConteudoVersaoReadProxyService(
                authContextPort,
                planningAiConteudoVersaoReadPort);
    }

    @Bean
    CriarPlanejamentoIaConteudoUseCase criarPlanejamentoIaConteudoUseCase(
            AuthContextPort authContextPort,
            PlanningAiConteudoWritePort planningAiConteudoWritePort) {
        return new PlanejamentoIaConteudoWriteProxyService(
                authContextPort,
                planningAiConteudoWritePort);
    }

    @Bean
    ConsultarMatriculaUseCase consultarMatriculaUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentMatriculaReadPort enrollmentDocumentMatriculaReadPort) {
        return new MatriculaReadProxyService(authContextPort, enrollmentDocumentMatriculaReadPort);
    }

    @Bean
    CriarEscolaOrigemUseCase criarEscolaOrigemUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentEscolaOrigemWritePort enrollmentDocumentEscolaOrigemWritePort) {
        return new EscolaOrigemWriteProxyService(authContextPort, enrollmentDocumentEscolaOrigemWritePort);
    }

    @Bean
    CriarTransferenciaUseCase criarTransferenciaUseCase(
            AuthContextPort authContextPort,
            EnrollmentDocumentTransferenciaWritePort enrollmentDocumentTransferenciaWritePort) {
        return new TransferenciaWriteProxyService(authContextPort, enrollmentDocumentTransferenciaWritePort);
    }

    @Bean
    RouteCatalogReadUseCase routeCatalogReadUseCase(
            MonolithCatalogReadPort monolithCatalogReadPort,
            AcademicCatalogReadPort academicCatalogReadPort,
            AuthContextPort authContextPort,
            CatalogReadCutoverPolicyPort cutoverPolicyPort,
            CatalogReadObservabilityPort observabilityPort) {
        return new CatalogReadRoutingService(
                monolithCatalogReadPort,
                academicCatalogReadPort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    CreatePeriodoLetivoUseCase createPeriodoLetivoUseCase(
            MonolithPeriodoLetivoWritePort monolithPeriodoLetivoWritePort,
            AcademicCatalogPeriodoLetivoWritePort academicCatalogPeriodoLetivoWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new PeriodoLetivoWriteRoutingService(
                monolithPeriodoLetivoWritePort,
                academicCatalogPeriodoLetivoWritePort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    CreateDisciplinaUseCase createDisciplinaUseCase(
            MonolithDisciplinaWritePort monolithDisciplinaWritePort,
            AcademicCatalogDisciplinaWritePort academicCatalogDisciplinaWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new DisciplinaWriteRoutingService(
                monolithDisciplinaWritePort,
                academicCatalogDisciplinaWritePort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    CreateSerieUseCase createSerieUseCase(
            MonolithSerieWritePort monolithSerieWritePort,
            AcademicCatalogSerieWritePort academicCatalogSerieWritePort,
            AcademicCatalogNivelEnsinoResolverPort academicCatalogNivelEnsinoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new SerieWriteRoutingService(
                monolithSerieWritePort,
                academicCatalogSerieWritePort,
                academicCatalogNivelEnsinoResolverPort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    CreateTurmaUseCase createTurmaUseCase(
            MonolithTurmaWritePort monolithTurmaWritePort,
            AcademicCatalogTurmaWritePort academicCatalogTurmaWritePort,
            AcademicCatalogTurnoResolverPort academicCatalogTurnoResolverPort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new TurmaWriteRoutingService(
                monolithTurmaWritePort,
                academicCatalogTurmaWritePort,
                academicCatalogTurnoResolverPort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }

    @Bean
    LinkTurmaDisciplinaUseCase linkTurmaDisciplinaUseCase(
            MonolithTurmaDisciplinaWritePort monolithTurmaDisciplinaWritePort,
            AcademicCatalogTurmaDisciplinaWritePort academicCatalogTurmaDisciplinaWritePort,
            AuthContextPort authContextPort,
            CatalogWriteCutoverPolicyPort cutoverPolicyPort,
            CatalogWriteObservabilityPort observabilityPort) {
        return new TurmaDisciplinaWriteRoutingService(
                monolithTurmaDisciplinaWritePort,
                academicCatalogTurmaDisciplinaWritePort,
                authContextPort,
                cutoverPolicyPort,
                observabilityPort);
    }
}
