package com.siae.relatorios;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import com.siae.entities.DetalhesEntrega;
import com.siae.entities.Entrega;
import org.springframework.stereotype.Service;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.siae.entities.ProjetoDeVenda;
import com.siae.entities.ProjetoProduto;

@Service
public class EntregaMensal {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
    DateTimeFormatter monthNumberFormat = DateTimeFormatter.ofPattern("MM");
    DateTimeFormatter yearNumberFormat = DateTimeFormatter.ofPattern("yyyy");

    PdfFont regularFont;
    PdfFont boldFont;
    NumberFormat currencyBr = NumberFormat.getCurrencyInstance();

    public byte[] createPdf(List<Entrega> entregas, String mes, String ano) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        List<Entrega> entregasFiltradas = entregas.stream()
                .filter((entrega -> entrega.getDataDaEntrega().format(monthNumberFormat).equals(mes) && entrega.getDataDaEntrega().format(yearNumberFormat).equals(ano)))
                .toList();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            regularFont = PdfFontFactory.createFont("fonts/static/Roboto-Regular.ttf", "Identity-H");
            boldFont = PdfFontFactory.createFont("fonts/static/Roboto-Bold.ttf", "Identity-H");

            addMainHeader(document, "Prefeitura Municipal de Colinas", 0, regularFont);
            addMainHeader(document, "Secretaria Municipal de Educação", 0, regularFont);
            addMainHeader(document, "Secretaria Municipal de Agricultura", 0, regularFont);
            addMainHeader(document, "Departamento de Alimentação Escolar-DAE", 0, regularFont);
            addMainHeader(document, "RELATORIO AGRICULTURA FAMILIAR", 0, regularFont);
            addMainHeader(document, "RELATORIO AGRICULTURA FAMILIAR", 0, regularFont);

            addParagraph(document, "MÊS :", regularFont);

            addTable(document, entregasFiltradas);

//            addFooter(document, entregas);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

//    private void addFooter(Document document, ProjetoDeVenda projeto) {
//        Paragraph footer = new Paragraph("COLINAS (MA), " + projeto.getDataProjeto().format(formatter).toUpperCase())
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(10)
//                .setFixedPosition(40, 120, UnitValue.createPercentValue(100));
//        document.add(footer);
//
//        Paragraph assign = new Paragraph("___________________________________________")
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(10)
//                .setFixedPosition(40, 90, UnitValue.createPercentValue(100));
//        document.add(assign);
//
//        Paragraph name = new Paragraph(projeto.getProdutor().getNome())
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(10)
//                .setFixedPosition(40, 70, UnitValue.createPercentValue(100));
//        document.add(name);
//
//        Paragraph cpf = new Paragraph("CPF Nº " + projeto.getProdutor().getCpf())
//                .setTextAlignment(TextAlignment.CENTER)
//                .setFontSize(10)
//                .setFixedPosition(40, 50, UnitValue.createPercentValue(100));
//        document.add(cpf);
//
//    }

    private void addTable(Document document, List<Entrega> entregasFiltradas) {
        if(entregasFiltradas.isEmpty()) {
            throw new RuntimeException();
        }
        float[] columnWidthsEntrega = {1, 1, 1, 1, 1, 1, 1};
        Table tableEntrega = new Table(columnWidthsEntrega);
        tableEntrega.addHeaderCell(createdStyledCell("PRODUTOR", boldFont));
        tableEntrega.addHeaderCell(createdStyledCell("PRODUTO", boldFont));
        tableEntrega.addHeaderCell(createdStyledCell("UND", boldFont));
        tableEntrega.addHeaderCell(createdStyledCell("QTD", boldFont));
        tableEntrega.addHeaderCell(createdStyledCell("R$ / UNT", boldFont));
        tableEntrega.addHeaderCell(createdStyledCell("TOTAL", boldFont));
        tableEntrega.addHeaderCell(createdStyledCell("TOTAL / PRODUTOR", boldFont));

        for (Entrega e : entregasFiltradas) {   
            for (DetalhesEntrega d : e.getDetalhesEntrega()) {
                tableEntrega.addCell(createdStyledCell(e.getProdutor().getNome(), regularFont));
                tableEntrega.addCell(createdStyledCell(d.getProduto().getDescricao(), regularFont));
                tableEntrega.addCell(createdStyledCell(d.getProduto().getUnidade(), regularFont));
                tableEntrega.addCell(createdStyledCell(d.getQuantidade().toString(), regularFont));
                tableEntrega.addCell(createdStyledCell("R" + currencyBr.format(d.getProduto().getPrecoMedio()), regularFont));
                tableEntrega.addCell(createdStyledCell("R" + currencyBr.format(d.getTotal()), regularFont));
                tableEntrega.addCell(createdStyledCell("R" + currencyBr.format(e.getTotal()), regularFont));
                tableEntrega.setWidth(UnitValue.createPercentValue(100));
            }

        }



        tableEntrega.setKeepTogether(true);

        document.add(tableEntrega);

    }

    private Cell createdStyledCell(String content, PdfFont font) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(ColorConstants.WHITE)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Cell createdStyledHeader(String content, PdfFont font) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(ColorConstants.CYAN)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private void addMainHeader(Document document, String headerText, int spacing, PdfFont font) {
        Paragraph header = new Paragraph(headerText)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(font)
                .setFontSize(10)
                .setMarginTop(spacing);
        document.add(header);
    }

    private void addParagraph(Document document, String text, PdfFont font) {
        Paragraph header = new Paragraph(text)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFont(font)
                .setFontSize(10);
        document.add(header);
    }
}
;