import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { finalize } from 'rxjs';
import { DiarioClasse, DiarioClasseSalvar, DiarioClasseService, FrequenciaStatus } from './diario-classe.service';

@Component({ selector: 'app-diario-classe', standalone: true, imports: [CommonModule, FormsModule, MatButtonModule, MatCardModule, MatIconModule, MatInputModule, MatProgressBarModule, MatSnackBarModule], templateUrl: './diario-classe.component.html', styleUrl: './diario-classe.component.scss' })
export class DiarioClasseComponent implements OnInit {
  private readonly route = inject(ActivatedRoute); private readonly service = inject(DiarioClasseService); private readonly snackBar = inject(MatSnackBar);
  readonly carregando = signal(false); readonly salvando = signal(false); readonly diario = signal<DiarioClasse | null>(null);
  readonly erro = signal(''); observacoes = ''; assinatura = ''; dataAssinatura = '';
  private parametros!: { idProfessor: string; idTurma: string; idDisciplina: string; anoLetivo: number; mes: number; dataReferencia: string };

  ngOnInit(): void {
    const query = this.route.snapshot.queryParamMap; const hoje = new Date();
    const idProfessor = query.get('idProfessor'); const idTurma = query.get('idTurma'); const idDisciplina = query.get('idDisciplina');
    if (!idProfessor || !idTurma || !idDisciplina) { this.erro.set('Informe professor, turma e disciplina para abrir o Diário de Classe.'); return; }
    this.parametros = { idProfessor, idTurma, idDisciplina, anoLetivo: Number(query.get('anoLetivo') ?? hoje.getFullYear()), mes: Number(query.get('mes') ?? hoje.getMonth() + 1), dataReferencia: query.get('dataReferencia') ?? this.iso(hoje) };
    this.carregar();
  }
  carregar(): void { this.carregando.set(true); this.service.carregar(this.parametros).pipe(finalize(() => this.carregando.set(false))).subscribe({ next: d => { const dia = new Date(`${this.parametros.dataReferencia}T12:00:00`).getDate(); this.diario.set({ ...d, alunos: d.alunos.map(aluno => ({ ...aluno, frequencias: { ...aluno.frequencias, [dia]: aluno.frequencias[dia] || '.' } })) }); this.observacoes = d.observacoes.join('\n'); this.assinatura = d.assinatura?.nomeProfessor ?? ''; this.dataAssinatura = d.assinatura?.dataAssinatura ?? this.br(new Date()); }, error: () => this.erro.set('Não foi possível carregar o Diário de Classe.') }); }
  marcar(aluno: DiarioClasse['alunos'][number], status: FrequenciaStatus): void { const d = this.diario(); if (!d || d.bloqueado) return; const dia = new Date(`${this.parametros.dataReferencia}T12:00:00`).getDate(); aluno.frequencias[dia] = status; this.diario.set({ ...d, alunos: [...d.alunos] }); }
  salvar(): void { const d = this.diario(); if (!d || d.bloqueado || !this.assinatura.trim()) { this.snackBar.open('Informe a assinatura do professor antes de salvar.', 'Fechar', { duration: 3500 }); return; } const dia = new Date(`${this.parametros.dataReferencia}T12:00:00`).getDate(); if (d.alunos.some(a => !a.frequencias[dia])) { this.snackBar.open('Lance a frequência de todos os alunos.', 'Fechar', { duration: 3500 }); return; } const request: DiarioClasseSalvar = { idDiarioClasse: d.cabecalho.idDiarioClasse, dataLancamento: this.parametros.dataReferencia, frequencias: d.alunos.map(a => ({ idAluno: a.idAluno, data: this.parametros.dataReferencia, dia, situacao: this.situacao(a.frequencias[dia] as FrequenciaStatus) })), conteudos: d.conteudosPlanejados.map(c => ({ ...c, alterado: false })), observacoes: this.observacoes.split('\n').map(x => x.trim()).filter(Boolean), assinatura: { nomeProfessor: this.assinatura.trim(), dataAssinatura: this.dataAssinatura } }; this.salvando.set(true); this.service.salvar(d.cabecalho.idDiarioClasse, request).pipe(finalize(() => this.salvando.set(false))).subscribe({ next: r => { this.diario.set({ ...d, bloqueado: r.bloqueado ?? true }); this.snackBar.open(r.mensagem || 'Diário salvo com sucesso.', 'Fechar', { duration: 4000 }); }, error: () => this.snackBar.open('Não foi possível salvar o Diário de Classe.', 'Fechar', { duration: 4000 }) }); }
  imprimir(): void { window.print(); } private situacao(status: FrequenciaStatus): 'PRESENTE' | 'FALTA_JUSTIFICADA' | 'FALTA' { return status === 'F' ? 'FALTA' : status === 'J' ? 'FALTA_JUSTIFICADA' : 'PRESENTE'; } private iso(d: Date): string { return d.toISOString().slice(0, 10); } private br(d: Date): string { return d.toLocaleDateString('pt-BR'); }
}
