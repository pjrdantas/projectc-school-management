package br.com.escola.pedagogicalservice.application.service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarPdfImportResponse;
import br.com.escola.pedagogicalservice.application.dto.HistoricoEscolarTelaResponse;
import br.com.escola.pedagogicalservice.application.exception.InvalidRequestContextException;

@Service
public class HistoricoEscolarPdfImportService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Pattern ALUNO = Pattern.compile("(?im)^(?:nome(?: do aluno)?|aluno)\\s*[:\\-]\\s*(.+)$");
    private static final Pattern ESCOLA = Pattern.compile("(?im)^(?:escola|unidade escolar)\\s*[:\\-]\\s*(.+)$");

    public HistoricoEscolarPdfImportResponse importar(MultipartFile arquivo) {
        validar(arquivo);
        String texto = extrairTexto(arquivo);
        String nomeAluno = campo(texto, ALUNO);
        String escola = campo(texto, ESCOLA);
        boolean possuiTexto = !texto.isBlank();
        int confianca = possuiTexto ? 35 + (nomeAluno == null ? 0 : 30) + (escola == null ? 0 : 20) : 0;
        List<String> avisos = possuiTexto
                ? List.of("Confira os campos extraídos antes de salvar.", "Assinaturas e carimbos não são importados automaticamente.", "Campos não identificados foram mantidos em branco para conferência manual.")
                : List.of("Não foi possível extrair texto do PDF. O documento pode ser digitalizado e exigir OCR.", "Nenhum dado foi persistido; confira e preencha manualmente antes de salvar.");
        return new HistoricoEscolarPdfImportResponse(historico(nomeAluno, escola), arquivo.getOriginalFilename(), confianca, avisos, Instant.now());
    }

    private void validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) throw new InvalidRequestContextException("Envie um arquivo PDF para importar o Histórico Escolar");
        if (arquivo.getSize() > MAX_FILE_SIZE) throw new InvalidRequestContextException("O PDF do Histórico Escolar deve ter no máximo 10 MB");
        String nome = arquivo.getOriginalFilename() == null ? "" : arquivo.getOriginalFilename().toLowerCase();
        String tipo = arquivo.getContentType() == null ? "" : arquivo.getContentType().toLowerCase();
        if (!nome.endsWith(".pdf") && !"application/pdf".equals(tipo)) throw new InvalidRequestContextException("A importação aceita somente arquivos PDF");
    }

    private String extrairTexto(MultipartFile arquivo) {
        try {
            byte[] bytes = arquivo.getBytes();
            if (bytes.length < 5 || bytes[0] != '%' || bytes[1] != 'P' || bytes[2] != 'D' || bytes[3] != 'F') throw new InvalidRequestContextException("O arquivo enviado não é um PDF válido");
            try (PDDocument document = Loader.loadPDF(bytes)) { return new PDFTextStripper().getText(document).replace('\u0000', ' ').trim(); }
        } catch (IOException exception) {
            throw new InvalidRequestContextException("Não foi possível ler o PDF do Histórico Escolar");
        }
    }

    private String campo(String texto, Pattern pattern) { Matcher matcher = pattern.matcher(texto); return matcher.find() ? matcher.group(1).trim() : null; }

    private HistoricoEscolarTelaResponse historico(String nomeAluno, String escola) {
        return new HistoricoEscolarTelaResponse(
                new HistoricoEscolarTelaResponse.Contexto(null, null, null, "CADASTRO", "PENDENTE", null, null, escola, null, false),
                new HistoricoEscolarTelaResponse.Cabecalho(null, null, null, escola, null, null, null, null, null, null, null, null, null),
                new HistoricoEscolarTelaResponse.Aluno(nomeAluno, null, null, null, null, null, null),
                List.of(), List.of(), List.of(), new HistoricoEscolarTelaResponse.Totais(List.of(), List.of(), List.of(), List.of()), List.of(),
                "Dados importados de PDF. Conferir antes de salvar.",
                new HistoricoEscolarTelaResponse.Certificado(null, null, escola, null, null, null, null, null, null, null, null),
                List.of(new HistoricoEscolarTelaResponse.Pendencia("CONFERIR_IMPORTACAO_PDF", "ATENCAO", "Cadastro", "Confira os dados importados antes de salvar.")));
    }
}
