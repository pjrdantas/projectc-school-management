package br.com.escola.pedagogicalservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

class HistoricoEscolarPdfImportServiceTest {

    private final HistoricoEscolarPdfImportService service = new HistoricoEscolarPdfImportService();

    @Test
    void deveExtrairCamposBasicosEManterHistoricoPendente() throws Exception {
        var arquivo = new MockMultipartFile("arquivo", "historico.pdf", "application/pdf",
                pdfTextual("NOME DO ALUNO: Maria da Silva\nESCOLA: Escola Estadual Exemplo"));

        var response = service.importar(arquivo);

        assertThat(response.nomeArquivo()).isEqualTo("historico.pdf");
        assertThat(response.historico().contexto().status()).isEqualTo("PENDENTE");
        assertThat(response.historico().contexto().bloqueado()).isFalse();
        assertThat(response.historico().aluno().nome()).isEqualTo("Maria da Silva");
        assertThat(response.historico().cabecalho().escola()).isEqualTo("Escola Estadual Exemplo");
        assertThat(response.avisos()).isNotEmpty();
    }

    private byte[] pdfTextual(String texto) throws Exception {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            try (PDPageContentStream stream = new PDPageContentStream(document, document.getPage(0))) {
                stream.beginText();
                stream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                stream.newLineAtOffset(72, 720);
                for (String linha : texto.split("\\n")) { stream.showText(linha); stream.newLineAtOffset(0, -16); }
                stream.endText();
            }
            document.save(output);
            return output.toByteArray();
        }
    }
}
