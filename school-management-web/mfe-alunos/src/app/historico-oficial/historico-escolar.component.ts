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
import { MatTabsModule } from '@angular/material/tabs';
import { finalize } from 'rxjs';
import { HistoricoEscolar, HistoricoEscolarOficialService } from './historico-escolar.service';

@Component({ selector: 'app-historico-escolar-oficial', standalone: true, imports: [CommonModule, FormsModule, MatButtonModule, MatCardModule, MatIconModule, MatInputModule, MatProgressBarModule, MatSnackBarModule, MatTabsModule], templateUrl: './historico-escolar.component.html', styleUrl: './historico-escolar.component.scss' })
export class HistoricoEscolarOficialComponent implements OnInit {
  private readonly route = inject(ActivatedRoute); private readonly service = inject(HistoricoEscolarOficialService); private readonly snackBar = inject(MatSnackBar);
  readonly historico = signal<HistoricoEscolar | null>(null); readonly carregando = signal(false); readonly salvando = signal(false); readonly erro = signal('');
  ngOnInit(): void { const id = this.route.snapshot.paramMap.get('id'); if (id) { this.buscar(() => this.service.carregar(id)); return; } const query = this.route.snapshot.queryParamMap; const aluno = query.get('idAluno'); const matricula = query.get('idMatricula'); if (!aluno || !matricula) { this.erro.set('Abra o novo Histórico Escolar a partir de um aluno e sua matrícula.'); return; } this.buscar(() => this.service.novo(aluno, matricula)); }
  salvar(): void { const h = this.historico(); if (!h || h.contexto.bloqueado) return; this.salvando.set(true); this.service.salvar(h).pipe(finalize(() => this.salvando.set(false))).subscribe({ next: r => { this.historico.set({ ...h, contexto: { ...h.contexto, idHistoricoEscolar: r.idHistoricoEscolar, status: r.status } }); this.snackBar.open(r.mensagem, 'Fechar', { duration: 4500 }); }, error: () => this.snackBar.open('Não foi possível salvar o Histórico Escolar.', 'Fechar', { duration: 4500 }) }); }
  importar(event: Event): void { const arquivo = (event.target as HTMLInputElement).files?.[0]; if (!arquivo) return; this.service.importar(arquivo).subscribe({ next: r => { this.historico.set(r.historico); this.snackBar.open(`PDF importado (${r.confiancaGeral}% de confiança). Confira os dados antes de salvar.`, 'Fechar', { duration: 6000 }); }, error: () => this.snackBar.open('Não foi possível importar o PDF.', 'Fechar', { duration: 4500 }) }); }
  imprimir(): void { window.print(); } private buscar(load: () => ReturnType<HistoricoEscolarOficialService['carregar']>): void { this.carregando.set(true); load().pipe(finalize(() => this.carregando.set(false))).subscribe({ next: h => this.historico.set(h), error: () => this.erro.set('Não foi possível carregar o Histórico Escolar.') }); }
}
