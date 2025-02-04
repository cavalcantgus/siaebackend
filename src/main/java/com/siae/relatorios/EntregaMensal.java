package com.siae.relatorios;

import java.awt.*;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import com.itextpdf.kernel.colors.DeviceRgb;
import com.siae.entities.*;
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

@Service
public class EntregaMensal {

    DeviceRgb customTotalColor = new DeviceRgb(255, 215, 0);
    DeviceRgb customGray = new DeviceRgb(169, 169, 169);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
    DateTimeFormatter monthNumberFormat = DateTimeFormatter.ofPattern("MM");
    DateTimeFormatter yearNumberFormat = DateTimeFormatter.ofPattern("yyyy");

    PdfFont regularFont;
    PdfFont boldFont;
    NumberFormat currencyBr = NumberFormat.getCurrencyInstance();

    public byte[] createPdf(List<Entrega> entregas, String mes, String ano) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        List<Entrega> entregasFiltradas = new ArrayList<>(entregas.stream()
                .filter((entrega -> entrega.getDataDaEntrega().format(monthNumberFormat).equals(mes) && entrega.getDataDaEntrega().format(yearNumberFormat).equals(ano)))
                .toList());

        Map<Produtor, List<DetalhesEntrega>> entregasPorProd = new HashMap<>();

        for (Entrega entrega : entregasFiltradas) {
            Produtor produtor = entrega.getProdutor();
            List<DetalhesEntrega> detalhesEntregas = entrega.getDetalhesEntrega();

            entregasPorProd
                    .computeIfAbsent(produtor, k -> new ArrayList<>())
                    .addAll(detalhesEntregas);
        }

        entregasFiltradas.sort((entrega1, entrega2) -> {
            return entrega1.getProdutor().getNome().compareToIgnoreCase(entrega2.getProdutor().getNome());
        });

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

            addMainHeader(document, "MÊS: " + entregas.get(0).getDataDaEntrega().format(monthFormatter).toUpperCase(), 10, regularFont);

            for (Map.Entry<Produtor, List<DetalhesEntrega>> entry : entregasPorProd.entrySet()) {
                Produtor produtor = entry.getKey();
                List<DetalhesEntrega> detalhesEntregas = entry.getValue();
                addTable(document, produtor, detalhesEntregas);
                addTotalTable(document, detalhesEntregas);
            }

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

    private void addTotalTable(Document document, List<DetalhesEntrega> detalhesEntregas) {
        UnitValue[] columnWidthsTotalGeral = {UnitValue.createPercentValue(83.33f),
                UnitValue.createPercentValue(16.66f)};

        Table tableTotal = new Table(columnWidthsTotalGeral);
        BigDecimal total = detalhesEntregas.stream()
                .map(DetalhesEntrega::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        tableTotal.addCell(createdStyledHeader("TOTAL / PRODUTOR", boldFont, customTotalColor));
        tableTotal.addCell(createdStyledCell("R" + currencyBr.format(total), regularFont));

        tableTotal.setWidth(UnitValue.createPercentValue(100));  // Faz a tabela ocupar 100% da largura disponível
        tableTotal.setKeepTogether(true);  // Garante que a tabela não seja dividida em várias páginas

        document.add(tableTotal);
    }

    private void addTable(Document document, Produtor produtor, List<DetalhesEntrega> detalhesEntregas) {
        // Definindo as larguras das colunas para ocupar 100% da página
        UnitValue[] columnWidthsEntrega = {UnitValue.createPercentValue(16.66f),
                UnitValue.createPercentValue(16.66f),
                UnitValue.createPercentValue(16.66f),
                UnitValue.createPercentValue(16.66f),
                UnitValue.createPercentValue(16.66f),
                UnitValue.createPercentValue(16.66f)};

        Table tableEntrega = new Table(columnWidthsEntrega);
        tableEntrega.addHeaderCell(createdStyledHeader("PRODUTOR", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("PRODUTO", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("UND", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("QTD", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("R$ / UNT", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("TOTAL", boldFont, customGray ));

        boolean isFirstLine = true;

        // Itera pelos detalhes de entrega associados a este produtor
        for (DetalhesEntrega d : detalhesEntregas) {
            // Na primeira linha de cada grupo, adiciona o nome do produtor
            if (isFirstLine) {
                tableEntrega.addCell(createdStyledCell(produtor.getNome(), regularFont).setBorderBottom(null));
                isFirstLine = false; // Marca que não é mais a primeira linha
            } else {
                // Para as linhas seguintes, adiciona uma célula vazia na coluna do nome do produtor
                tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
            }

            // Adiciona os detalhes do produto
            tableEntrega.addCell(createdStyledCell(d.getProduto().getDescricao(), regularFont));
            tableEntrega.addCell(createdStyledCell(d.getProduto().getUnidade(), regularFont));
            tableEntrega.addCell(createdStyledCell(d.getQuantidade().toString(), regularFont));
            tableEntrega.addCell(createdStyledCell("R" + currencyBr.format(d.getProduto().getPrecoMedio()), regularFont));
            tableEntrega.addCell(createdStyledCell("R" + currencyBr.format(d.getTotal()), regularFont));
        }

        tableEntrega.setWidth(UnitValue.createPercentValue(100));  // Faz a tabela ocupar 100% da largura disponível
        tableEntrega.setKeepTogether(true);  // Garante que a tabela não seja dividida em várias páginas

        document.add(tableEntrega);
    }


    private Cell createdStyledCell(String content, PdfFont font) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(ColorConstants.WHITE)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.LEFT);
    }

    private Cell createdStyledHeader(String content, PdfFont font, DeviceRgb color) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(color)
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