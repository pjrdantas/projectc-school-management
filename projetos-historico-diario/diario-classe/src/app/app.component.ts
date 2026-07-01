import { CommonModule } from '@angular/common';
import { Component, Injectable, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { DateAdapter, MAT_DATE_FORMATS, MAT_DATE_LOCALE, MatDateFormats, NativeDateAdapter } from '@angular/material/core';
import { MatDividerModule } from '@angular/material/divider';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { finalize } from 'rxjs';
import {
  AvaliacaoDiarioResponse,
  ConteudoPlanejadoResponse,
  DiarioClasseAlunoResponse,
  DiarioClasseCabecalhoResponse,
  DiarioClasseResponse,
  DiarioClasseSaveRequest,
  FrequenciaStatus
} from './core/models/diario-classe-api.models';
import { DiarioClasseApiService } from './core/services/diario-classe-api.service';


const PT_BR_DATE_FORMATS: MatDateFormats = {
  parse: {
    dateInput: 'DD/MM/YYYY'
  },
  display: {
    dateInput: 'DD/MM/YYYY',
    monthYearLabel: 'MMM YYYY',
    dateA11yLabel: 'DD/MM/YYYY',
    monthYearA11yLabel: 'MMMM YYYY'
  }
};

@Injectable()
class PtBrDateAdapter extends NativeDateAdapter {
  override parse(value: unknown): Date | null {
    if (typeof value === 'string') {
      const texto = value.trim();
      const partes = texto.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
      if (partes) {
        const dia = Number(partes[1]);
        const mes = Number(partes[2]) - 1;
        const ano = Number(partes[3]);
        const data = new Date(ano, mes, dia);
        if (data.getFullYear() === ano && data.getMonth() === mes && data.getDate() === dia) {
          return data;
        }
        return null;
      }
    }

    return super.parse(value);
  }

  override format(date: Date, displayFormat: Object): string {
    if (displayFormat === 'DD/MM/YYYY') {
      return this.formatarDataBr(date);
    }

    return super.format(date, displayFormat);
  }

  private formatarDataBr(data: Date): string {
    const dia = String(data.getDate()).padStart(2, '0');
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const ano = data.getFullYear();
    return `${dia}/${mes}/${ano}`;
  }
}

interface DiarioInfo {
  idDiarioClasse: string;
  escola: string;
  anoLetivo: string;
  diretoria: string;
  turma: string;
  municipio: string;
  turno: string;
  disciplina: string;
  professor: string;
  mes: string;
  data: string;
}

interface AlunoFrequencia {
  idAluno: string;
  numero: number;
  nome: string;
  frequencia: string[];
}

interface ConteudoMinistrado {
  idPlanejamentoAula: string;
  periodo: string;
  descricao: string;
  periodoOriginal: string;
  descricaoOriginal: string;
}

interface Avaliacao {
  idAvaliacao: string;
  data: string;
  descricao: string;
  turma: string;
  valor: string;
}

interface DiaDoMes {
  dia: number;
  semana: string;
  dataIso: string;
  fimDeSemana: boolean;
  diaCorrente: boolean;
  passado: boolean;
  futuro: boolean;
  bloqueado: boolean;
  editavel: boolean;
  obrigatorio: boolean;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatDatepickerModule,
    MatDividerModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatProgressBarModule,
    MatTabsModule,
    MatTooltipModule
  ],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss',
  providers: [
    { provide: DateAdapter, useClass: PtBrDateAdapter },
    { provide: MAT_DATE_FORMATS, useValue: PT_BR_DATE_FORMATS },
    { provide: MAT_DATE_LOCALE, useValue: 'pt-BR' }
  ]
})
export class AppComponent implements OnInit {
  diasDoMes: DiaDoMes[] = [];
  linhasVaziasConteudo = Array.from({ length: 2 });
  linhasVaziasAvaliacao = Array.from({ length: 1 });

  carregando = false;
  salvando = false;
  mensagemSalvamento = '';
  formularioBloqueado = false;

  readonly hoje = new Date();
  readonly hojeIso = this.formatarDataIso(this.hoje);

  diarioInfo: DiarioInfo = {
    idDiarioClasse: '',
    escola: '',
    anoLetivo: String(this.hoje.getFullYear()),
    diretoria: '',
    turma: '',
    municipio: '',
    turno: '',
    disciplina: '',
    professor: '',
    mes: this.nomeMes(this.hoje.getMonth() + 1),
    data: this.formatarDataBr(this.hoje)
  };

  alunos: AlunoFrequencia[] = [];
  conteudos: ConteudoMinistrado[] = [];
  avaliacoes: Avaliacao[] = [];
  observacoes: string[] = Array.from({ length: 6 }, () => '');
  assinaturaProfessor = '';
  dataAssinatura: Date | null = new Date(this.hoje.getFullYear(), this.hoje.getMonth(), this.hoje.getDate());

  constructor(private readonly diarioApi: DiarioClasseApiService) {}

  ngOnInit(): void {
    this.carregarDiario();
  }

  carregarDiario(): void {
    this.carregando = true;
    this.diarioApi.carregarDiarioClasse({
      idProfessor: 'prof-001',
      idTurma: 'turma-8a-2026',
      idDisciplina: 'disc-matematica',
      anoLetivo: this.obterAnoLetivo(),
      mes: this.obterNumeroMes(),
      dataReferencia: this.hojeIso
    })
      .pipe(finalize(() => this.carregando = false))
      .subscribe({
        next: (response) => this.preencherTela(response),
        error: () => {
          this.mensagemSalvamento = 'Não foi possível carregar o Diário de Classe.';
        }
      });
  }

  atualizarDiasDoMes(): void {
    const ano = this.obterAnoLetivo();
    const mes = this.obterNumeroMes();
    const quantidadeDias = new Date(ano, mes, 0).getDate();
    const diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];
    const hojeSemHorario = new Date(this.hoje.getFullYear(), this.hoje.getMonth(), this.hoje.getDate());

    this.diasDoMes = Array.from({ length: quantidadeDias }, (_, index) => {
      const dia = index + 1;
      const data = new Date(ano, mes - 1, dia);
      const dataIso = this.formatarDataIso(data);
      const fimDeSemana = data.getDay() === 0 || data.getDay() === 6;
      const diaCorrente = dataIso === this.hojeIso;
      const passado = data < hojeSemHorario;
      const futuro = data > hojeSemHorario;
      const editavel = diaCorrente && !fimDeSemana;

      return {
        dia,
        semana: diasSemana[data.getDay()],
        dataIso,
        fimDeSemana,
        diaCorrente,
        passado,
        futuro,
        bloqueado: !editavel,
        editavel,
        obrigatorio: editavel
      };
    });

    this.alunos.forEach((aluno) => {
      aluno.frequencia = Array.from({ length: quantidadeDias }, (_, index) => aluno.frequencia[index] ?? '');
    });
  }

  trackByDia(_index: number, diaInfo: DiaDoMes): number {
    return diaInfo.dia;
  }

  trackByAluno(_index: number, aluno: AlunoFrequencia): string {
    return aluno.idAluno;
  }

  trackByIndice(index: number): number {
    return index;
  }

  get dataAssinaturaFormatada(): string {
    return this.dataAssinatura ? this.formatarDataBr(this.dataAssinatura) : '';
  }

  get alunosParaPdf(): AlunoFrequencia[] {
    const linhasVazias = Math.max(0, 20 - this.alunos.length);
    return [
      ...this.alunos,
      ...Array.from({ length: linhasVazias }, (_, index) => ({
        idAluno: `vazio-${index}`,
        numero: this.alunos.length + index + 1,
        nome: '',
        frequencia: Array.from({ length: this.diasDoMes.length }, () => '')
      }))
    ];
  }

  get taxaPresenca(): number {
    const marcacoes = this.alunos.flatMap((aluno) => aluno.frequencia).filter(Boolean);
    const presencas = marcacoes.filter((marcacao) => this.ehPresenca(marcacao)).length;
    return marcacoes.length ? Math.round((presencas / marcacoes.length) * 100) : 0;
  }

  get houveAlteracaoPlanejamento(): boolean {
    return this.conteudos.some((conteudo) => this.conteudoAlterado(conteudo));
  }

  get observacaoJustificativa(): string {
    return this.observacoes.join(' ').trim();
  }

  get podeSalvar(): boolean {
    return !this.formularioBloqueado && !this.carregando && !this.salvando && this.validacoesPendentes.length === 0;
  }

  get validacoesPendentes(): string[] {
    const pendencias: string[] = [];

    if (this.formularioBloqueado) {
      return pendencias;
    }

    if (!this.alunos.length) {
      pendencias.push('A lista de alunos da classe precisa estar carregada.');
    }

    const diasObrigatorios = this.diasDoMes.filter((dia) => dia.obrigatorio);
    const frequenciaCompleta = this.alunos.every((aluno) =>
      diasObrigatorios.every((dia) => this.ehFrequenciaValida(aluno.frequencia[dia.dia - 1]))
    );

    if (diasObrigatorios.length && !frequenciaCompleta) {
      pendencias.push('Lance P, . ou F para todos os alunos no dia corrente.');
    }

    const diaAtual = this.diaAtualDoMesSelecionado();
    const diaAtualInfo = diaAtual ? this.diasDoMes[diaAtual - 1] : undefined;
    if (diaAtualInfo?.fimDeSemana) {
      // Finais de semana podem ficar sem lançamento.
    } else if (diaAtualInfo?.editavel) {
      const indiceDiaAtual = diaAtualInfo.dia - 1;
      const lancamentoDoDiaCompleto = this.alunos.every((aluno) => this.ehFrequenciaValida(aluno.frequencia[indiceDiaAtual]));
      if (!lancamentoDoDiaCompleto) {
        pendencias.push('O lançamento de frequência do dia atual é obrigatório.');
      }
    }

    if (this.houveAlteracaoPlanejamento && !this.observacaoJustificativa) {
      pendencias.push('Alteração de período ou descrição do conteúdo exige justificativa em Observações.');
    }

    if (!this.assinaturaProfessor.trim()) {
      pendencias.push('Informe a assinatura do professor.');
    }

    if (!this.dataAssinatura) {
      pendencias.push('Informe a data da assinatura.');
    }

    return pendencias;
  }

  normalizarFrequencia(alunoIndex: number, diaIndex: number, valor: string): void {
    const dia = this.diasDoMes[diaIndex];
    if (!dia?.editavel) {
      return;
    }

    const frequencia = (valor || '').trim().toUpperCase();
    const frequenciaValida = this.ehFrequenciaValida(frequencia);
    this.alunos[alunoIndex].frequencia[diaIndex] = frequenciaValida ? frequencia : '';

    if (frequenciaValida) {
      this.focarProximoAluno(alunoIndex, diaIndex);
    }
  }

  private focarProximoAluno(alunoIndex: number, diaIndex: number): void {
    const proximoAlunoIndex = alunoIndex + 1;
    if (proximoAlunoIndex >= this.alunos.length) {
      return;
    }

    requestAnimationFrame(() => {
      const seletor = `[data-frequency-cell="${proximoAlunoIndex}-${diaIndex}"]`;
      const proximoCampo = document.querySelector<HTMLInputElement>(seletor);
      if (!proximoCampo || proximoCampo.disabled) {
        return;
      }

      proximoCampo.focus();
      proximoCampo.select();
    });
  }

  bloquearCaracterInvalido(event: KeyboardEvent): void {
    const teclasControle = ['Backspace', 'Delete', 'Tab', 'ArrowLeft', 'ArrowRight', 'Home', 'End'];
    if (event.ctrlKey || event.metaKey || teclasControle.includes(event.key)) {
      return;
    }

    const tecla = event.key.toUpperCase();
    if (tecla !== 'P' && tecla !== 'F' && event.key !== '.') {
      event.preventDefault();
    }
  }

  conteudoAlterado(conteudo: ConteudoMinistrado): boolean {
    return conteudo.periodo.trim() !== conteudo.periodoOriginal.trim()
      || conteudo.descricao.trim() !== conteudo.descricaoOriginal.trim();
  }

  salvar(): void {
    if (!this.podeSalvar) {
      return;
    }

    this.salvando = true;
    this.mensagemSalvamento = '';

    this.diarioApi.salvarDiarioClasse(this.montarRequestSalvamento())
      .pipe(finalize(() => this.salvando = false))
      .subscribe({
        next: (response) => {
          this.mensagemSalvamento = response.mensagem;
          this.formularioBloqueado = response.bloqueado ?? response.status === 'SALVO';
        },
        error: () => {
          this.mensagemSalvamento = 'Não foi possível salvar o Diário de Classe.';
        }
      });
  }

  imprimir(): void {
    window.print();
  }

  exportarPdf(): void {
    requestAnimationFrame(() => window.print());
  }

  private preencherTela(response: DiarioClasseResponse): void {
    this.diarioInfo = this.mapearCabecalho(response.cabecalho);
    this.alunos = response.alunos.map((aluno, index) => this.mapearAluno(aluno, index));
    this.conteudos = response.conteudosPlanejados.map((conteudo) => this.mapearConteudo(conteudo));
    this.avaliacoes = response.avaliacoes.map((avaliacao) => this.mapearAvaliacao(avaliacao));
    this.observacoes = response.observacoes.length ? response.observacoes : Array.from({ length: 6 }, () => '');
    this.assinaturaProfessor = response.assinatura?.nomeProfessor ?? '';
    this.dataAssinatura = this.converterDataAssinatura(response.assinatura?.dataAssinatura) ?? new Date(this.hoje.getFullYear(), this.hoje.getMonth(), this.hoje.getDate());
    this.formularioBloqueado = response.bloqueado ?? false;
    this.atualizarDiasDoMes();
  }

  private mapearCabecalho(cabecalho: DiarioClasseCabecalhoResponse): DiarioInfo {
    return {
      idDiarioClasse: cabecalho.idDiarioClasse,
      escola: cabecalho.escola,
      anoLetivo: String(this.hoje.getFullYear()),
      diretoria: cabecalho.diretoriaEnsino,
      turma: cabecalho.turmaSerie,
      municipio: cabecalho.municipio,
      turno: cabecalho.turno,
      disciplina: cabecalho.disciplina,
      professor: cabecalho.professor,
      mes: this.nomeMes(this.hoje.getMonth() + 1),
      data: this.formatarDataBr(this.hoje)
    };
  }

  private mapearAluno(aluno: DiarioClasseAlunoResponse, index: number): AlunoFrequencia {
    const quantidadeDias = new Date(this.obterAnoLetivo(), this.obterNumeroMes(), 0).getDate();
    const frequencia = Array.from({ length: quantidadeDias }, (_, dayIndex) => aluno.frequencias[dayIndex + 1] ?? '');
    return {
      idAluno: aluno.idAluno,
      numero: aluno.numeroChamada ?? index + 1,
      nome: aluno.nome,
      frequencia
    };
  }

  private mapearConteudo(conteudo: ConteudoPlanejadoResponse): ConteudoMinistrado {
    return {
      idPlanejamentoAula: conteudo.idPlanejamentoAula,
      periodo: conteudo.periodo,
      descricao: conteudo.descricao,
      periodoOriginal: conteudo.periodo,
      descricaoOriginal: conteudo.descricao
    };
  }

  private mapearAvaliacao(avaliacao: AvaliacaoDiarioResponse): Avaliacao {
    return {
      idAvaliacao: avaliacao.idAvaliacao,
      data: avaliacao.data,
      descricao: avaliacao.descricao,
      turma: avaliacao.turma,
      valor: avaliacao.valor
    };
  }

  private montarRequestSalvamento(): DiarioClasseSaveRequest {
    return {
      idDiarioClasse: this.diarioInfo.idDiarioClasse,
      dataLancamento: this.hojeIso,
      frequencias: this.alunos.flatMap((aluno) =>
        this.diasDoMes
          .filter((dia) => dia.obrigatorio)
          .map((dia) => ({
            idAluno: aluno.idAluno,
            data: dia.dataIso,
            dia: dia.dia,
            status: aluno.frequencia[dia.dia - 1] as FrequenciaStatus
          }))
      ),
      conteudos: this.conteudos.map((conteudo) => ({
        idPlanejamentoAula: conteudo.idPlanejamentoAula,
        periodo: conteudo.periodo,
        descricao: conteudo.descricao,
        alterado: this.conteudoAlterado(conteudo),
        observacaoJustificativa: this.conteudoAlterado(conteudo) ? this.observacaoJustificativa : undefined
      })),
      observacoes: this.observacoes.filter((observacao) => observacao.trim()),
      assinatura: {
        nomeProfessor: this.assinaturaProfessor,
        dataAssinatura: this.dataAssinaturaFormatada
      }
    };
  }

  private converterDataAssinatura(valor: string | undefined): Date | null {
    if (!valor?.trim()) {
      return null;
    }

    const texto = valor.trim();
    const dataBr = texto.match(/^(\d{1,2})\/(\d{1,2})\/(\d{4})$/);
    if (dataBr) {
      return this.criarDataValida(Number(dataBr[3]), Number(dataBr[2]) - 1, Number(dataBr[1]));
    }

    const dataIso = texto.match(/^(\d{4})-(\d{1,2})-(\d{1,2})$/);
    if (dataIso) {
      return this.criarDataValida(Number(dataIso[1]), Number(dataIso[2]) - 1, Number(dataIso[3]));
    }

    return null;
  }

  private criarDataValida(ano: number, mes: number, dia: number): Date | null {
    const data = new Date(ano, mes, dia);
    return data.getFullYear() === ano && data.getMonth() === mes && data.getDate() === dia ? data : null;
  }

  private diaAtualDoMesSelecionado(): number | null {
    const ano = this.obterAnoLetivo();
    const mes = this.obterNumeroMes();
    const anoAtual = this.hoje.getFullYear();
    const mesAtual = this.hoje.getMonth() + 1;

    return ano === anoAtual && mes === mesAtual ? this.hoje.getDate() : null;
  }

  private obterUltimoDiaObrigatorio(ano: number, mes: number, quantidadeDias: number): number {
    const anoAtual = this.hoje.getFullYear();
    const mesAtual = this.hoje.getMonth() + 1;

    if (ano < anoAtual || (ano === anoAtual && mes < mesAtual)) {
      return quantidadeDias;
    }

    if (ano === anoAtual && mes === mesAtual) {
      return Math.min(this.hoje.getDate(), quantidadeDias);
    }

    return 0;
  }

  private ehFrequenciaValida(valor: string | undefined): boolean {
    return valor === 'P' || valor === '.' || valor === 'F';
  }

  private ehPresenca(valor: string | undefined): boolean {
    return valor === 'P' || valor === '.';
  }

  private obterAnoLetivo(): number {
    const ano = Number.parseInt(this.diarioInfo.anoLetivo, 10);
    return Number.isFinite(ano) ? ano : new Date().getFullYear();
  }

  private obterNumeroMes(): number {
    const mesNormalizado = this.removerAcentos(this.diarioInfo.mes).toLowerCase().trim();
    const mesNumerico = Number.parseInt(mesNormalizado, 10);

    if (mesNumerico >= 1 && mesNumerico <= 12) {
      return mesNumerico;
    }

    const meses: Record<string, number> = {
      janeiro: 1,
      jan: 1,
      fevereiro: 2,
      fev: 2,
      marco: 3,
      mar: 3,
      abril: 4,
      abr: 4,
      maio: 5,
      mai: 5,
      junho: 6,
      jun: 6,
      julho: 7,
      jul: 7,
      agosto: 8,
      ago: 8,
      setembro: 9,
      set: 9,
      outubro: 10,
      out: 10,
      novembro: 11,
      nov: 11,
      dezembro: 12,
      dez: 12
    };

    return meses[mesNormalizado] ?? meses[mesNormalizado.slice(0, 3)] ?? 1;
  }

  private nomeMes(mes: number): string {
    const meses = [
      'Janeiro', 'Fevereiro', 'Março', 'Abril', 'Maio', 'Junho',
      'Julho', 'Agosto', 'Setembro', 'Outubro', 'Novembro', 'Dezembro'
    ];
    return meses[mes - 1] ?? 'Janeiro';
  }

  private formatarDataBr(data: Date): string {
    return new Intl.DateTimeFormat('pt-BR').format(data);
  }

  private formatarDataIso(data: Date): string {
    const ano = data.getFullYear();
    const mes = String(data.getMonth() + 1).padStart(2, '0');
    const dia = String(data.getDate()).padStart(2, '0');
    return `${ano}-${mes}-${dia}`;
  }

  private removerAcentos(valor: string): string {
    return valor.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
  }
}
