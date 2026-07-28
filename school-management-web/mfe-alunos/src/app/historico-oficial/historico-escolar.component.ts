import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
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
import { AppComponent as HistoricoEscolarReferenciaComponent } from './referencia/app.component';
import { HistoricoEscolarApiService as HistoricoEscolarReferenciaService } from './referencia/core/services/historico-escolar-api.service';
import { HistoricoEscolarBffService } from './historico-escolar.service';

@Component({
  selector: 'app-historico-escolar-oficial', standalone: true,
  imports: [CommonModule, FormsModule, MatButtonModule, MatCardModule, MatDividerModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressBarModule, MatTabsModule, MatTooltipModule],
  templateUrl: './referencia/app.component.html', styleUrl: './referencia/app.component.scss',
  providers: [HistoricoEscolarBffService, { provide: HistoricoEscolarReferenciaService, useExisting: HistoricoEscolarBffService }],
})
export class HistoricoEscolarOficialComponent extends HistoricoEscolarReferenciaComponent {
  override imprimir(): void {
    this.abrirDialogoImpressao();
  }

  override exportarPdf(): void {
    this.abrirDialogoImpressao();
  }

  private abrirDialogoImpressao(): void {
    const tituloOriginal = document.title;
    const regraPagina = document.createElement('style');
    regraPagina.textContent = '@page { size: A4 portrait; margin: 0; }';
    document.head.append(regraPagina);
    document.body.classList.add('historico-escolar-printing');
    document.title = 'Histórico Escolar';
    window.addEventListener('afterprint', () => {
      regraPagina.remove();
      document.body.classList.remove('historico-escolar-printing');
      document.title = tituloOriginal;
    }, { once: true });
    requestAnimationFrame(() => window.print());
  }
}
