package com.siae.relatorios;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.layout.Document;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.siae.entities.DetalhesEntrega;
import com.siae.entities.Entrega;
import com.siae.entities.ProjetoDeVenda;
import com.siae.entities.ProjetoProduto;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class ComprovanteDeRecebimento {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
    PdfFont regularFont;
    PdfFont boldFont;
    NumberFormat currencyBr = NumberFormat.getCurrencyInstance();
    public byte[] createPdf(Entrega entrega) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(new PageSize(395, 700).rotate());
            Document document = new Document(pdfDoc);

            regularFont = PdfFontFactory.createFont("fonts/static/Roboto-Regular.ttf", "Identity-H");
            boldFont = PdfFontFactory.createFont("fonts/static/Roboto-Bold.ttf", "Identity-H");

            addMainHeader(document, "Programa de Aquisição de Alimentos", 0, boldFont);
            addMainHeader(document, "Data da Entrega " + entrega.getDataDaEntrega().format(formatter), 0, regularFont);


            addTable(document, entrega);

            addFooter(document);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void addFooter(Document document) {

        Paragraph assign = new Paragraph("___________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFixedPosition(40, 90, UnitValue.createPercentValue(100));
        document.add(assign);

        Paragraph name = new Paragraph("RECEBEDOR")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFixedPosition(40, 70, UnitValue.createPercentValue(100));
        document.add(name);

    }

    private void addTable(Document document, Entrega entrega) {
        float[] columnWidthsRelacao = {1};
        float[] columnWidthsProdutorNome = {1};
        float[] columnWidthsProdutos = {1, 1, 1, 1, 1};
        float[] columnWidthsTotalGeral = {4000f, 450f};

        // Nome do Produtor
        Table tableProdutorNome = new Table(columnWidthsProdutorNome);
        tableProdutorNome.addHeaderCell(createdStyledHeader("I – IDENTIFICAÇÃO DO AGRICULTOR/ FORNECEDOR", boldFont));
        tableProdutorNome.addCell(createdStyledCell("AGRICULTOR: " + entrega.getProdutor().getNome(), regularFont));
        tableProdutorNome.setWidth(UnitValue.createPercentValue(100));

        Table tableRelacao = new Table(columnWidthsRelacao);
        tableRelacao.addHeaderCell(createdStyledHeader("II - RELAÇÃO DE PRODUTOS", boldFont));
        tableRelacao.setWidth(UnitValue.createPercentValue(100));

        Table tableProdutos = new Table(columnWidthsProdutos);
        tableProdutos.addHeaderCell(createdStyledCell("PRODUTOS", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("UNIDADES", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("QUANTIDADE ENTREGUE", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("PREÇO UNIT. R$", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("VALOR TOTAL R$", regularFont));

        for(DetalhesEntrega d : entrega.getDetalhesEntrega()) {
            tableProdutos.addCell(createdStyledCell(d.getProduto().getDescricao(), regularFont));
            tableProdutos.addCell(createdStyledCell(d.getProduto().getUnidade(), regularFont));
            tableProdutos.addCell(createdStyledCell(d.getQuantidade().toString(), regularFont));
            tableProdutos.addCell(createdStyledCell("R" + currencyBr.format(d.getProduto().getPrecoMedio()), regularFont));
            tableProdutos.addCell(createdStyledCell("R" + currencyBr.format(d.getTotal()), regularFont));
            tableProdutos.setWidth(UnitValue.createPercentValue(100));
        }

        Table tableTotalGeral = new Table(columnWidthsTotalGeral);
        tableTotalGeral.addCell(createdStyledCell("TOTAL", regularFont));
        tableTotalGeral.addCell(createdStyledCell("R" + currencyBr.format(entrega.getTotal()), regularFont));
//

        tableProdutorNome.setKeepTogether(true);
        tableProdutos.setKeepTogether(true);
        tableRelacao.setKeepTogether(true);
        tableTotalGeral.setKeepTogether(false);
//
        document.add(tableProdutorNome);
        document.add(tableRelacao);
        document.add(tableProdutos);
        document.add(tableTotalGeral);
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
