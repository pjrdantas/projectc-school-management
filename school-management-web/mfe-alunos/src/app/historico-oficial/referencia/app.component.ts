import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { HistoricoEscolarApiService } from './core/services/historico-escolar-api.service';
import {
  CertificadoHistoricoResponse,
  CurriculoLinhaResponse,
  EstudoRealizadoResponse,
  HistoricoAlunoResponse,
  HistoricoCabecalhoResponse,
  HistoricoEscolarContextoResponse,
  HistoricoEscolarResponse,
  HistoricoEscolarSaveRequest,
  HistoricoPendenciaResponse,
  PeriodoLetivoResponse
} from './core/models/historico-escolar-api.models';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatTabsModule,
    MatTooltipModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit {
  carregando = true;
  salvando = false;
  importandoPdf = false;
  arquivoPdfImportado = '';
  mensagemSistema = '';
  mensagemTipo: 'success' | 'warning' | 'error' = 'warning';

  contexto: HistoricoEscolarContextoResponse = {
    modo: 'CADASTRO',
    status: 'RASCUNHO',
    serieMatriculaAtual: 6,
    serieConcluidaOrigem: 5,
    escolaOrigem: '',
    dataTransferencia: '',
    bloqueado: false
  };

  periodos: PeriodoLetivoResponse[] = [];

  cabecalho: HistoricoCabecalhoResponse = {
    governo: '',
    secretaria: '',
    diretoria: '',
    escola: '',
    atoLegal: 'Ato Legal de criação:',
    atoLegalCriacao: '',
    endereco: '',
    numero: '',
    bairro: '',
    municipio: '',
    cep: '',
    telefone: '',
    email: ''
  };

  aluno: HistoricoAlunoResponse = {
    nome: '',
    rg: '',
    ra: '',
    nascimentoMunicipio: '',
    nascimentoEstado: '',
    nascimentoPais: '',
    nascimentoData: ''
  };

  baseComumRows: CurriculoLinhaResponse[] = [];
  diversificadaRows: CurriculoLinhaResponse[] = [];

  totalBaseComum: string[] = [];
  totalParteDiversificada: string[] = [];
  totalAulasAnuais: string[] = [];
  totalCargaHoraria: string[] = [];

  estudosRealizados: EstudoRealizadoResponse[] = [];
  observacoes = '';

  certificado: CertificadoHistoricoResponse = {
    serieConcluida: null,
    diretor: '',
    escola: '',
    rgAluno: '',
    ano: '',
    publicacao: '',
    data: '',
    gerenteNome: '',
    gerenteRg: '',
    diretorNome: '',
    diretorRg: ''
  };

  constructor(private readonly historicoApi: HistoricoEscolarApiService) {}

  ngOnInit(): void {
    this.historicoApi.carregarHistoricoEscolar({ modo: 'CADASTRO' }).subscribe({
      next: (response) => {
        this.contexto = response.contexto;
        this.cabecalho = response.cabecalho;
        this.aluno = response.aluno;
        this.periodos = response.periodos;
        this.baseComumRows = response.baseComum;
        this.diversificadaRows = response.parteDiversificada;
        this.totalBaseComum = response.totais.totalBaseComum;
        this.totalParteDiversificada = response.totais.totalParteDiversificada;
        this.totalAulasAnuais = response.totais.totalAulasAnuais;
        this.totalCargaHoraria = response.totais.totalCargaHoraria;
        this.estudosRealizados = response.estudosRealizados;
        this.observacoes = response.observacoes;
        this.certificado = response.certificado;
        this.carregando = false;
        this.atualizarMensagemInicial();
      },
      error: () => {
        this.carregando = false;
        this.mensagemTipo = 'error';
        this.mensagemSistema = 'Não foi possível carregar o Histórico Escolar.';
      }
    });
  }

  get totalCurriculoRows(): number {
    return this.baseComumRows.length + this.diversificadaRows.length + 4;
  }

  get totalCampos(): number {
    return this.camposDoFormulario.length;
  }

  get camposPreenchidos(): number {
    return this.camposDoFormulario.filter((campo) => this.temValor(campo)).length;
  }

  get progressoPreenchimento(): number {
    if (!this.totalCampos) {
      return 0;
    }

    return Math.round((this.camposPreenchidos / this.totalCampos) * 100);
  }

  get camposCadastroObrigatoriosPendentes(): string[] {
    const campos: Array<[string, string | number | null | undefined]> = [
      ['Governo', this.cabecalho.governo],
      ['Secretaria', this.cabecalho.secretaria],
      ['Diretoria de Ensino', this.cabecalho.diretoria],
      ['Escola', this.cabecalho.escola],
      ['Ato legal de criação', this.cabecalho.atoLegalCriacao],
      ['Endereço', this.cabecalho.endereco],
      ['Número', this.cabecalho.numero],
      ['Bairro', this.cabecalho.bairro],
      ['Município', this.cabecalho.municipio],
      ['CEP', this.cabecalho.cep],
      ['Telefone', this.cabecalho.telefone],
      ['E-mail', this.cabecalho.email],
      ['Nome do aluno', this.aluno.nome],
      ['RG/RNM', this.aluno.rg],
      ['RA', this.aluno.ra],
      ['Município de nascimento', this.aluno.nascimentoMunicipio],
      ['Estado de nascimento', this.aluno.nascimentoEstado],
      ['País de nascimento', this.aluno.nascimentoPais],
      ['Data de nascimento', this.aluno.nascimentoData]
    ];

    return campos
      .filter(([, valor]) => !this.temValor(valor))
      .map(([label]) => label);
  }

  get cadastroMinimoValido(): boolean {
    return this.camposCadastroObrigatoriosPendentes.length === 0;
  }

  get seriesObrigatorias(): number[] {
    const limite = Math.max(0, Math.min(9, Number(this.contexto.serieMatriculaAtual || 0) - 1));
    return Array.from({ length: limite }, (_, index) => index + 1);
  }

  get pendencias(): HistoricoPendenciaResponse[] {
    const pendencias: HistoricoPendenciaResponse[] = [];

    if (!this.cadastroMinimoValido) {
      pendencias.push({
        codigo: 'CADASTRO_MINIMO_INCOMPLETO',
        severidade: 'ERRO',
        aba: 'CADASTRO',
        mensagem: `Preencha os dados iniciais da escola e do aluno: ${this.camposCadastroObrigatoriosPendentes.join(', ')}.`
      });
    }

    if (this.seriesObrigatorias.length > 0) {
      const seriesComEstudosPendentes = this.seriesObrigatorias.filter((serie) => !this.estudoRealizadoCompleto(serie));
      const seriesComNotasPendentes = this.seriesObrigatorias.filter((serie) => !this.serieCurricularCompleta(serie));

      if (seriesComEstudosPendentes.length > 0) {
        pendencias.push({
          codigo: 'ESTUDOS_REALIZADOS_PENDENTES',
          severidade: 'AVISO',
          aba: 'ESTUDOS',
          mensagem: `O aluno está matriculado na ${this.formatarSerie(this.contexto.serieMatriculaAtual)}. Complete os estudos realizados até a ${this.formatarSerie(this.contexto.serieMatriculaAtual - 1)}. Pendentes: ${seriesComEstudosPendentes.map((serie) => this.formatarSerie(serie)).join(', ')}.`
        });
      }

      if (seriesComNotasPendentes.length > 0) {
        pendencias.push({
          codigo: 'ANOS_COMPONENTES_PENDENTES',
          severidade: 'AVISO',
          aba: 'ANOS_COMPONENTES',
          mensagem: `Há componentes curriculares ou anos letivos pendentes até a série anterior à matrícula. Pendentes: ${seriesComNotasPendentes.map((serie) => this.formatarSerie(serie)).join(', ')}.`
        });
      }
    }

    if (!this.certificadoMinimoValido) {
      pendencias.push({
        codigo: 'CERTIFICADO_PENDENTE',
        severidade: 'AVISO',
        aba: 'CERTIFICADO',
        mensagem: `O certificado ainda não está completo. Ele deve refletir a série concluída na escola de origem, atualmente indicada como ${this.formatarSerie(this.contexto.serieConcluidaOrigem)}.`
      });
    }

    return pendencias;
  }

  get pendenciasAviso(): HistoricoPendenciaResponse[] {
    return this.pendencias.filter((pendencia) => pendencia.severidade === 'AVISO');
  }

  get pendenciasErro(): HistoricoPendenciaResponse[] {
    return this.pendencias.filter((pendencia) => pendencia.severidade === 'ERRO');
  }

  get certificadoMinimoValido(): boolean {
    return [
      this.certificado.serieConcluida,
      this.certificado.escola,
      this.certificado.rgAluno,
      this.certificado.ano,
      this.certificado.data
    ].every((campo) => this.temValor(campo));
  }

  get salvarDesabilitado(): boolean {
    return this.carregando || this.salvando || Boolean(this.contexto.bloqueado) || !this.cadastroMinimoValido;
  }

  get statusVisual(): string {
    if (!this.cadastroMinimoValido) {
      return 'Cadastro mínimo pendente';
    }

    return this.pendenciasAviso.length ? 'Salvável com pendências' : 'Histórico completo';
  }

  get modoVisual(): string {
    return this.contexto.modo === 'CADASTRO' ? 'Cadastro inicial' : 'Edição';
  }

  imprimir(): void {
    window.print();
  }

  exportarPdf(): void {
    this.imprimir();
  }


  importarPdf(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    input.value = '';

    if (!file) {
      return;
    }

    const arquivoPdf = file.type === 'application/pdf' || file.name.toLowerCase().endsWith('.pdf');
    if (!arquivoPdf) {
      this.mensagemTipo = 'error';
      this.mensagemSistema = 'Selecione um arquivo PDF do Histórico Escolar.';
      return;
    }

    this.importandoPdf = true;
    this.mensagemTipo = 'warning';
    this.mensagemSistema = 'Lendo PDF do Histórico Escolar. Aguarde...';

    this.historicoApi.importarHistoricoEscolarPdf(file).subscribe({
      next: (response) => {
        this.importandoPdf = false;
        this.arquivoPdfImportado = response.nomeArquivo;
        this.aplicarHistoricoImportado(response.historico);
        this.mensagemTipo = response.confiancaGeral >= 80 ? 'success' : 'warning';
        this.mensagemSistema = `PDF importado: ${response.nomeArquivo}. Confiança estimada: ${response.confiancaGeral}%. Confira os dados antes de salvar. ${response.avisos.join(' ')}`;
      },
      error: () => {
        this.importandoPdf = false;
        this.mensagemTipo = 'error';
        this.mensagemSistema = 'Não foi possível ler o PDF do Histórico Escolar.';
      }
    });
  }

  salvarHistorico(): void {
    if (this.salvarDesabilitado) {
      this.mensagemTipo = 'error';
      this.mensagemSistema = 'Preencha os dados iniciais da escola e do aluno para liberar o salvamento.';
      return;
    }

    const payload = this.montarPayloadSalvar();
    this.salvando = true;
    this.historicoApi.salvarHistoricoEscolar(payload).subscribe({
      next: (response) => {
        this.salvando = false;
        this.contexto.idHistoricoEscolar = response.idHistoricoEscolar;
        this.contexto.status = response.status;
        this.mensagemTipo = response.status === 'COMPLETO' ? 'success' : 'warning';
        this.mensagemSistema = response.mensagem;
      },
      error: () => {
        this.salvando = false;
        this.mensagemTipo = 'error';
        this.mensagemSistema = 'Não foi possível salvar o Histórico Escolar.';
      }
    });
  }

  campoCadastroPendente(valor: string | number | null | undefined): boolean {
    return !this.temValor(valor);
  }


  private aplicarHistoricoImportado(response: HistoricoEscolarResponse): void {
    this.contexto = {
      ...this.contexto,
      ...response.contexto,
      modo: this.contexto.idHistoricoEscolar ? 'EDICAO' : response.contexto.modo
    };
    this.cabecalho = response.cabecalho;
    this.aluno = response.aluno;
    this.periodos = response.periodos;
    this.baseComumRows = response.baseComum;
    this.diversificadaRows = response.parteDiversificada;
    this.totalBaseComum = response.totais.totalBaseComum;
    this.totalParteDiversificada = response.totais.totalParteDiversificada;
    this.totalAulasAnuais = response.totais.totalAulasAnuais;
    this.totalCargaHoraria = response.totais.totalCargaHoraria;
    this.estudosRealizados = response.estudosRealizados;
    this.observacoes = response.observacoes;
    this.certificado = response.certificado;
  }

  private montarPayloadSalvar(): HistoricoEscolarSaveRequest {
    return {
      idHistoricoEscolar: this.contexto.idHistoricoEscolar,
      idAluno: this.contexto.idAluno,
      idMatricula: this.contexto.idMatricula,
      modo: this.contexto.modo,
      statusPretendido: this.pendenciasAviso.length ? 'PENDENTE' : 'COMPLETO',
      contexto: {
        serieMatriculaAtual: Number(this.contexto.serieMatriculaAtual),
        serieConcluidaOrigem: Number(this.contexto.serieConcluidaOrigem),
        escolaOrigem: this.contexto.escolaOrigem,
        dataTransferencia: this.contexto.dataTransferencia
      },
      cabecalho: structuredClone(this.cabecalho),
      aluno: structuredClone(this.aluno),
      periodos: structuredClone(this.periodos),
      baseComum: structuredClone(this.baseComumRows),
      parteDiversificada: structuredClone(this.diversificadaRows),
      totais: {
        totalBaseComum: [...this.totalBaseComum],
        totalParteDiversificada: [...this.totalParteDiversificada],
        totalAulasAnuais: [...this.totalAulasAnuais],
        totalCargaHoraria: [...this.totalCargaHoraria]
      },
      estudosRealizados: structuredClone(this.estudosRealizados),
      observacoes: this.observacoes,
      certificado: structuredClone(this.certificado)
    };
  }

  private atualizarMensagemInicial(): void {
    this.mensagemTipo = 'warning';
    this.mensagemSistema = 'Cadastro inicial: preencha os dados da escola e do aluno para liberar o botão Salvar. As demais abas podem ficar pendentes, mas serão sinalizadas.';
  }

  private estudoRealizadoCompleto(serie: number): boolean {
    const estudo = this.estudosRealizados[serie - 1];
    if (!estudo) {
      return false;
    }

    return [estudo.serieAno, estudo.ano, estudo.escola, estudo.municipio, estudo.uf].every((campo) => this.temValor(campo));
  }

  private serieCurricularCompleta(serie: number): boolean {
    const index = serie - 1;
    const periodo = this.periodos[index];
    const anoLetivoPreenchido = this.temValor(periodo?.anoLetivo);
    const baseObrigatoriaPreenchida = this.baseComumRows.every((row) => this.temValor(row.valores[index]));

    return anoLetivoPreenchido && baseObrigatoriaPreenchida;
  }

  private formatarSerie(serie: number): string {
    if (!serie || serie < 1) {
      return 'série não informada';
    }
    return `${serie}ª série/${serie}º ano`;
  }

  private get camposDoFormulario(): string[] {
    return [
      ...Object.values(this.cabecalho),
      ...Object.values(this.aluno),
      String(this.contexto.serieMatriculaAtual ?? ''),
      String(this.contexto.serieConcluidaOrigem ?? ''),
      this.contexto.escolaOrigem ?? '',
      this.contexto.dataTransferencia ?? '',
      ...this.periodos.flatMap((periodo) => [periodo.anoLetivo, periodo.serie, periodo.equivalencia]),
      ...this.baseComumRows.flatMap((row) => [row.nome, ...row.valores]),
      ...this.diversificadaRows.flatMap((row) => [row.nome, ...row.valores]),
      ...this.totalBaseComum,
      ...this.totalParteDiversificada,
      ...this.totalAulasAnuais,
      ...this.totalCargaHoraria,
      ...this.estudosRealizados.flatMap((estudo) => [estudo.serieAno, estudo.ano, estudo.escola, estudo.municipio, estudo.uf]),
      this.observacoes,
      String(this.certificado.serieConcluida ?? ''),
      this.certificado.diretor,
      this.certificado.escola,
      this.certificado.rgAluno,
      this.certificado.ano,
      this.certificado.publicacao,
      this.certificado.data,
      this.certificado.gerenteNome,
      this.certificado.gerenteRg,
      this.certificado.diretorNome,
      this.certificado.diretorRg
    ].map((campo) => campo ?? '');
  }

  private temValor(valor: string | number | null | undefined): boolean {
    return String(valor ?? '').trim().length > 0;
  }
}
