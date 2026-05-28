import { CommonModule } from '@angular/common';
import { Component, Input, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';
import { getApiErrorMessage } from '../../core/http/api-error';
import { AuthStateService } from '../../core/auth/auth-state.service';
import { Student } from '../../students/models/student.model';
import { DocumentsPanelComponent } from '../../shared/documents/documents-panel.component';
import {
  Disciplina,
  EscolaOrigemInput,
  HistoricoEscolarItemInput,
  HistoricoEscolar,
  TransferenciaAluno,
} from '../models/student-records.model';
import { StudentRecordsService } from '../services/student-records.service';

interface ComponenteDraft {
  disciplinaId: string;
  componenteCurricular: string;
  anoLetivo: number | null;
  serie: string;
  ciclo: string;
  notaConceito: string;
  totalAulas: number | null;
  cargaHoraria: number | null;
}

@Component({
  selector: 'app-student-records-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatCardModule,
    MatCheckboxModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatTabsModule,
    DocumentsPanelComponent,
  ],
  templateUrl: './student-records-panel.component.html',
  styleUrls: ['./student-records-panel.component.scss'],
})
export class StudentRecordsPanelComponent implements OnInit {
  @Input({ required: true }) aluno!: Student;
  @Input() readonly = false;

  private readonly recordsService = inject(StudentRecordsService);
  private readonly authState = inject(AuthStateService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly carregando = signal(false);
  protected readonly historicos = signal<HistoricoEscolar[]>([]);
  protected readonly transferencias = signal<TransferenciaAluno[]>([]);
  protected readonly disciplinas = signal<Disciplina[]>([]);
  protected readonly componentesCurriculares = signal<HistoricoEscolarItemInput[]>([]);
  protected readonly documentosObrigatoriosAluno = [
    'RG',
    'CPF',
    'CERTIDAO_NASCIMENTO',
    'COMPROVANTE_RESIDENCIA',
  ];
  protected readonly tiposTransferencia = [
    { value: 'ENTRADA', label: 'Entrada' },
    { value: 'SAIDA', label: 'Saída' },
  ];
  protected readonly statusTransferenciaOptions = [
    { value: 'EM_ANDAMENTO', label: 'Em andamento' },
    { value: 'CONFIRMADA', label: 'Confirmada' },
    { value: 'CANCELADA', label: 'Cancelada' },
  ];

  protected novoHistorico = {
    rgRen: '',
    ra: '',
    rm: '',
    municipioNascimento: '',
    estadoNascimento: '',
    paisNascimento: 'BRASIL',
    nomeEscola: '',
    enderecoEscola: '',
    municipioEscola: '',
    cepEscola: '',
    telefoneEscola: '',
    emailEscola: '',
    anoConclusao: null as number | null,
    ensinoConcluido: '',
    dataEmissao: '',
    diretorNome: '',
    diretorRg: '',
    gerenteOrganizacaoNome: '',
    gerenteOrganizacaoRg: '',
    doeNumero: '',
    doeData: '',
    doeVolume: '',
    doePagina: '',
    observacoes: '',
  };

  protected componenteDraft: ComponenteDraft = this.emptyComponenteDraft();

  protected novaTransferencia = {
    tipoTransferencia: 'ENTRADA',
    statusTransferencia: 'EM_ANDAMENTO',
    serieOrigem: '',
    anoLetivoOrigem: '',
    dataTransferencia: '',
    motivoTransferencia: '',
    situacaoOrigem: '',
    documentosEntregues: '',
    observacao: '',
    escolaOrigem: {
      nomeEscola: '',
      codigoInep: '',
      cnpj: '',
      cep: '',
      logradouro: '',
      numero: '',
      complemento: '',
      bairro: '',
      cidade: '',
      uf: '',
    } as EscolaOrigemInput,
  };

  ngOnInit(): void {
    this.carregarTudo();
  }

  carregarTudo(): void {
    this.carregando.set(true);
    forkJoin({
      historicos: this.recordsService.listarHistoricosPorAluno(this.aluno.id),
      transferencias: this.recordsService.listarTransferenciasPorAluno(this.aluno.id),
      disciplinas: this.recordsService.listarDisciplinas(),
    }).subscribe({
      next: ({ historicos, transferencias, disciplinas }) => {
        this.historicos.set(this.filtrarHistoricosDoAluno(historicos));
        this.transferencias.set(transferencias);
        this.disciplinas.set(disciplinas);
        this.carregando.set(false);
      },
      error: (error: unknown) => {
        this.carregando.set(false);
        this.snackBar.open(
          getApiErrorMessage(error, 'Não foi possível carregar dados escolares do aluno.'),
          'Fechar',
          { duration: 5000 },
        );
      },
    });
  }

  protected adicionarComponenteAoHistorico(): void {
    if (this.readonly) return;

    const draft = this.componenteDraft;
    const componenteCurricular = draft.disciplinaId
      ? this.disciplinaNome(draft.disciplinaId)
      : draft.componenteCurricular.trim();

    if (!componenteCurricular) {
      this.snackBar.open('Informe o componente curricular.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    const componenteJaIncluido = this.componentesCurriculares().some(
      (item) =>
        item.componenteCurricular.trim().toUpperCase() === componenteCurricular.toUpperCase() &&
        (item.anoLetivo ?? null) === draft.anoLetivo &&
        (item.serie ?? '').trim().toUpperCase() === draft.serie.trim().toUpperCase(),
    );
    if (componenteJaIncluido) {
      this.snackBar.open('Este componente já foi incluído para o mesmo ano e série.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.componentesCurriculares.set([
      ...this.componentesCurriculares(),
      {
        componenteCurricular,
        anoLetivo: draft.anoLetivo ?? undefined,
        serie: this.optional(draft.serie),
        ciclo: this.optional(draft.ciclo),
        notaConceito: this.optional(draft.notaConceito),
        totalAulas: draft.totalAulas ?? undefined,
        cargaHoraria: draft.cargaHoraria ?? undefined,
      },
    ]);
    this.componenteDraft = this.emptyComponenteDraft();
  }

  protected removerComponenteDoHistorico(index: number): void {
    if (this.readonly) return;
    this.componentesCurriculares.set(this.componentesCurriculares().filter((_, i) => i !== index));
  }

  protected criarHistorico(): void {
    if (this.readonly) return;

    if (this.componentesCurriculares().length === 0) {
      this.snackBar.open('Informe ao menos um componente curricular.', 'Fechar', { duration: 3000 });
      return;
    }

    this.recordsService
      .criarHistorico({
        nomeAluno: this.aluno.nomeCompleto,
        rgRen: this.optional(this.novoHistorico.rgRen || this.aluno.rg || ''),
        ra: this.optional(this.novoHistorico.ra),
        rm: this.optional(this.novoHistorico.rm),
        dataNascimento: this.aluno.dataNascimento,
        municipioNascimento: this.optional(this.novoHistorico.municipioNascimento || this.aluno.naturalidade || ''),
        estadoNascimento: this.optional(this.novoHistorico.estadoNascimento || this.aluno.uf || ''),
        paisNascimento: this.optional(this.novoHistorico.paisNascimento || this.aluno.nacionalidade || ''),
        nomeEscola: this.optional(this.novoHistorico.nomeEscola),
        enderecoEscola: this.optional(this.novoHistorico.enderecoEscola),
        municipioEscola: this.optional(this.novoHistorico.municipioEscola),
        cepEscola: this.optional(this.onlyDigits(this.novoHistorico.cepEscola)),
        telefoneEscola: this.optional(this.novoHistorico.telefoneEscola),
        emailEscola: this.optional(this.novoHistorico.emailEscola),
        anoConclusao: this.novoHistorico.anoConclusao ?? undefined,
        ensinoConcluido: this.optional(this.novoHistorico.ensinoConcluido),
        dataEmissao: this.optional(this.novoHistorico.dataEmissao),
        diretorNome: this.optional(this.novoHistorico.diretorNome),
        diretorRg: this.optional(this.novoHistorico.diretorRg),
        gerenteOrganizacaoNome: this.optional(this.novoHistorico.gerenteOrganizacaoNome),
        gerenteOrganizacaoRg: this.optional(this.novoHistorico.gerenteOrganizacaoRg),
        doeNumero: this.optional(this.novoHistorico.doeNumero),
        doeData: this.optional(this.novoHistorico.doeData),
        doeVolume: this.optional(this.novoHistorico.doeVolume),
        doePagina: this.optional(this.novoHistorico.doePagina),
        observacoes: this.optional(this.novoHistorico.observacoes),
        componentesCurriculares: this.componentesCurriculares(),
      })
      .subscribe({
        next: (historico) => {
          this.historicos.set([historico, ...this.historicos()]);
          this.resetHistorico();
          this.componentesCurriculares.set([]);
          this.snackBar.open('Histórico escolar cadastrado.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível cadastrar histórico escolar.'),
            'Fechar',
            { duration: 5000 },
          );
        },
      });
  }

  protected buscarCepEscola(): void {
    if (this.readonly) return;

    const cep = this.onlyDigits(this.novaTransferencia.escolaOrigem.cep ?? '');
    if (cep.length !== 8) {
      this.snackBar.open('Informe um CEP com 8 dígitos.', 'Fechar', { duration: 3000 });
      return;
    }

    this.recordsService.consultarCep(cep).subscribe({
      next: (endereco) => {
        this.novaTransferencia.escolaOrigem = {
          ...this.novaTransferencia.escolaOrigem,
          cep: this.formatCep(endereco.cep),
          logradouro: endereco.logradouro,
          bairro: endereco.bairro,
          cidade: endereco.cidade,
          uf: endereco.uf,
          complemento:
            this.novaTransferencia.escolaOrigem.complemento || endereco.complemento || '',
        };
      },
      error: (error: unknown) => {
        this.snackBar.open(getApiErrorMessage(error, 'Não foi possível consultar o CEP.'), 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected criarTransferencia(): void {
    if (this.readonly) return;

    const escola = this.novaTransferencia.escolaOrigem;
    if (!escola.nomeEscola.trim() || !this.novaTransferencia.serieOrigem.trim()) {
      this.snackBar.open('Informe escola de origem e série de origem.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    if (this.optional(this.onlyDigits(escola.cep ?? '')) && !this.optional(escola.numero ?? '')) {
      this.snackBar.open('Número da escola é obrigatório quando CEP é informado.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.recordsService
      .criarTransferencia({
        alunoId: this.aluno.id,
        escolaOrigem: {
          nomeEscola: escola.nomeEscola.trim(),
          codigoInep: this.optional(escola.codigoInep ?? ''),
          cnpj: this.optional(escola.cnpj ?? ''),
          cep: this.optional(this.onlyDigits(escola.cep ?? '')),
          logradouro: this.optional(escola.logradouro ?? ''),
          numero: this.optional(escola.numero ?? ''),
          complemento: this.optional(escola.complemento ?? ''),
          bairro: this.optional(escola.bairro ?? ''),
          cidade: this.optional(escola.cidade ?? ''),
          uf: this.optional(escola.uf ?? '')?.toUpperCase(),
        },
        serieOrigem: this.novaTransferencia.serieOrigem.trim(),
        anoLetivoOrigem: this.novaTransferencia.anoLetivoOrigem.trim(),
        dataTransferencia: this.optional(this.novaTransferencia.dataTransferencia),
        motivoTransferencia: this.optional(this.novaTransferencia.motivoTransferencia),
        situacaoOrigem: this.optional(this.novaTransferencia.situacaoOrigem),
        documentosEntregues: this.optional(this.novaTransferencia.documentosEntregues),
        tipoTransferencia: this.novaTransferencia.tipoTransferencia,
        statusTransferencia: this.novaTransferencia.statusTransferencia,
        usuarioOperacao: this.usuarioOperacao(),
        observacao: this.optional(this.novaTransferencia.observacao),
      })
      .subscribe({
        next: (transferencia) => {
          this.transferencias.set([transferencia, ...this.transferencias()]);
          this.resetTransferencia();
          this.snackBar.open('Transferência cadastrada.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.snackBar.open(
            getApiErrorMessage(error, 'Não foi possível cadastrar transferência.'),
            'Fechar',
            { duration: 5000 },
          );
        },
      });
  }

  protected disciplinaNome(id: string): string {
    return this.disciplinas().find((item) => item.id === id)?.nome ?? id;
  }

  protected formatDate(value?: string): string {
    if (!value) return 'Não informado';
    const [date] = value.split('T');
    const [yyyy, mm, dd] = date.split('-');
    return dd && mm && yyyy ? `${dd}/${mm}/${yyyy}` : value;
  }

  protected formatDateTime(value?: string): string {
    if (!value) return 'Não informado';
    const [date, time = ''] = value.split('T');
    const [yyyy, mm, dd] = date.split('-');
    const [hh = '00', min = '00'] = time.split(':');
    return dd && mm && yyyy ? `${dd}/${mm}/${yyyy} ${hh}:${min}` : value;
  }

  protected labelTipoTransferencia(value?: string): string {
    return this.tiposTransferencia.find((item) => item.value === value)?.label ?? 'Entrada';
  }

  protected labelStatusTransferencia(value?: string): string {
    return this.statusTransferenciaOptions.find((item) => item.value === value)?.label ?? 'Em andamento';
  }

  protected onCepEscolaInput(): void {
    const cep = this.novaTransferencia.escolaOrigem.cep ?? '';
    this.novaTransferencia.escolaOrigem.cep = this.formatCep(cep);
  }

  protected onUfEscolaInput(): void {
    const uf = this.novaTransferencia.escolaOrigem.uf ?? '';
    this.novaTransferencia.escolaOrigem.uf = uf.toUpperCase().slice(0, 2);
  }

  private emptyComponenteDraft(): ComponenteDraft {
    return {
      disciplinaId: '',
      componenteCurricular: '',
      anoLetivo: null,
      serie: '',
      ciclo: '',
      notaConceito: '',
      totalAulas: null,
      cargaHoraria: null,
    };
  }

  private resetHistorico(): void {
    this.novoHistorico = {
      rgRen: '',
      ra: '',
      rm: '',
      municipioNascimento: '',
      estadoNascimento: '',
      paisNascimento: 'BRASIL',
      nomeEscola: '',
      enderecoEscola: '',
      municipioEscola: '',
      cepEscola: '',
      telefoneEscola: '',
      emailEscola: '',
      anoConclusao: null,
      ensinoConcluido: '',
      dataEmissao: '',
      diretorNome: '',
      diretorRg: '',
      gerenteOrganizacaoNome: '',
      gerenteOrganizacaoRg: '',
      doeNumero: '',
      doeData: '',
      doeVolume: '',
      doePagina: '',
      observacoes: '',
    };
  }

  private filtrarHistoricosDoAluno(historicos: HistoricoEscolar[]): HistoricoEscolar[] {
    const nomeAluno = this.aluno.nomeCompleto.trim().toUpperCase();
    return historicos.filter((historico) => historico.nomeAluno?.trim().toUpperCase() === nomeAluno);
  }

  private resetTransferencia(): void {
    this.novaTransferencia = {
      tipoTransferencia: 'ENTRADA',
      statusTransferencia: 'EM_ANDAMENTO',
      serieOrigem: '',
      anoLetivoOrigem: '',
      dataTransferencia: '',
      motivoTransferencia: '',
      situacaoOrigem: '',
      documentosEntregues: '',
      observacao: '',
      escolaOrigem: {
        nomeEscola: '',
        codigoInep: '',
        cnpj: '',
        cep: '',
        logradouro: '',
        numero: '',
        complemento: '',
        bairro: '',
        cidade: '',
        uf: '',
      },
    };
  }

  private optional(value: string): string | undefined {
    const trimmed = value.trim();
    return trimmed ? trimmed : undefined;
  }

  private onlyDigits(value: string): string {
    return value.replace(/\D/g, '');
  }

  private formatCep(value: string): string {
    const digits = this.onlyDigits(value).slice(0, 8);
    if (digits.length <= 5) return digits;
    return `${digits.slice(0, 5)}-${digits.slice(5)}`;
  }

  private usuarioOperacao(): string {
    const usuario = this.authState.getUsuario();
    return usuario?.nome || usuario?.usuario || 'Usuário não identificado';
  }
}
