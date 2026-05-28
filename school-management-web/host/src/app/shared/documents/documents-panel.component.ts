import { CommonModule } from '@angular/common';
import { Component, Input, OnChanges, SimpleChanges, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { getApiErrorMessage } from '../../core/http/api-error';
import { DocumentEntityType, DocumentRecord } from './document.model';
import { DocumentsService } from './documents.service';

@Component({
  selector: 'app-documents-panel',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatButtonModule,
    MatFormFieldModule,
    MatIconModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
  ],
  templateUrl: './documents-panel.component.html',
  styleUrls: ['./documents-panel.component.scss'],
})
export class DocumentsPanelComponent implements OnChanges {
  @Input({ required: true }) entidadeTipo!: DocumentEntityType;
  @Input({ required: true }) entidadeId!: string;
  @Input() readonly = false;
  @Input() requiredDocumentTypes: string[] = [];
  @Input() availableDocumentTypes: string[] = [
    'RG',
    'CPF',
    'CERTIDAO_NASCIMENTO',
    'COMPROVANTE_RESIDENCIA',
    'HISTORICO_ESCOLAR',
    'DECLARACAO_TRANSFERENCIA',
    'CONFIRMACAO_VAGA_DESTINO',
    'LAUDO',
    'OUTROS',
  ];

  private readonly documentsService = inject(DocumentsService);
  private readonly snackBar = inject(MatSnackBar);

  protected readonly documentos = signal<DocumentRecord[]>([]);
  protected readonly carregando = signal(false);
  protected readonly pendencias = computed(() => {
    const entregues = new Set(this.documentos().map((documento) => documento.tipoDocumento));
    return this.requiredDocumentTypes.filter((tipo) => !entregues.has(tipo));
  });

  protected novoDocumento: {
    tipoDocumento: string;
    numeroDocumento: string;
    nomeArquivoOriginal: string;
    arquivo: File | null;
    observacao: string;
  } = this.emptyDocumento();

  ngOnChanges(changes: SimpleChanges): void {
    if ((changes['entidadeTipo'] || changes['entidadeId']) && this.entidadeTipo && this.entidadeId) {
      this.carregarDocumentos();
    }
  }

  carregarDocumentos(): void {
    this.carregando.set(true);
    this.documentsService.listByEntity(this.entidadeTipo, this.entidadeId).subscribe({
      next: (documentos) => {
        this.documentos.set(documentos);
        this.carregando.set(false);
      },
      error: (error: unknown) => {
        this.carregando.set(false);
        this.snackBar.open(getApiErrorMessage(error, 'Não foi possível carregar documentos.'), 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected criarDocumento(): void {
    if (this.readonly) return;

    const arquivo = this.novoDocumento.arquivo;
    const numeroDocumento = this.novoDocumento.numeroDocumento.trim();
    if (!numeroDocumento || !arquivo) {
      this.snackBar.open('Informe número do documento e selecione o arquivo.', 'Fechar', {
        duration: 3000,
      });
      return;
    }

    this.documentsService
      .upload(
        {
          entidadeTipo: this.entidadeTipo,
          entidadeId: this.entidadeId,
          tipoDocumento: this.novoDocumento.tipoDocumento,
          numeroDocumento,
          observacao: this.optional(this.novoDocumento.observacao),
        },
        arquivo,
      )
      .subscribe({
        next: (documento) => {
          this.documentos.set([documento, ...this.documentos()]);
          this.novoDocumento = this.emptyDocumento();
          this.snackBar.open('Documento cadastrado.', 'Fechar', { duration: 3000 });
        },
        error: (error: unknown) => {
          this.snackBar.open(getApiErrorMessage(error, 'Não foi possível cadastrar documento.'), 'Fechar', {
            duration: 5000,
          });
        },
      });
  }

  protected selecionarArquivoDocumento(event: Event): void {
    const input = event.target as HTMLInputElement;
    const arquivo = input.files?.[0] ?? null;
    this.novoDocumento.arquivo = arquivo;
    this.novoDocumento.nomeArquivoOriginal = arquivo?.name ?? '';
  }

  protected excluirDocumento(idDocumento: string): void {
    if (this.readonly) return;

    this.documentsService.delete(idDocumento).subscribe({
      next: () => {
        this.documentos.set(this.documentos().filter((documento) => documento.id !== idDocumento));
        this.snackBar.open('Documento excluído.', 'Fechar', { duration: 2500 });
      },
      error: (error: unknown) => {
        this.snackBar.open(getApiErrorMessage(error, 'Não foi possível excluir documento.'), 'Fechar', {
          duration: 4000,
        });
      },
    });
  }

  protected labelDocumento(tipo: string): string {
    const labels: Record<string, string> = {
      RG: 'RG',
      CPF: 'CPF',
      CERTIDAO_NASCIMENTO: 'Certidão de nascimento',
      COMPROVANTE_RESIDENCIA: 'Comprovante de residência',
      HISTORICO_ESCOLAR: 'Histórico escolar',
      DECLARACAO_TRANSFERENCIA: 'Declaração de transferência',
      CONFIRMACAO_VAGA_DESTINO: 'Confirmação de vaga destino',
      LAUDO: 'Laudo',
      OUTROS: 'Outros',
    };
    return labels[tipo] ?? tipo;
  }

  protected formatDate(value?: string): string {
    if (!value) return 'Não informado';
    const [date] = value.split('T');
    const [yyyy, mm, dd] = date.split('-');
    return dd && mm && yyyy ? `${dd}/${mm}/${yyyy}` : value;
  }

  private emptyDocumento() {
    return {
      tipoDocumento: this.requiredDocumentTypes[0] ?? this.availableDocumentTypes[0] ?? 'RG',
      numeroDocumento: '',
      nomeArquivoOriginal: '',
      arquivo: null,
      observacao: '',
    };
  }

  private optional(value: string): string | undefined {
    const trimmed = value.trim();
    return trimmed ? trimmed : undefined;
  }
}
