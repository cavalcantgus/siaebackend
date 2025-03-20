package com.siae.relatorios;

import java.awt.*;
import java.math.BigDecimal;
import java.text.Collator;
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
    DeviceRgb customTotalGeralColor = new DeviceRgb(0, 255, 255);
    DeviceRgb customGray = new DeviceRgb(169, 169, 169);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
    DateTimeFormatter monthNumberFormat = DateTimeFormatter.ofPattern("MM");
    DateTimeFormatter yearNumberFormat = DateTimeFormatter.ofPattern("yyyy");

    PdfFont regularFont;
    PdfFont boldFont;
    NumberFormat currencyBr = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

    public byte[] createPdf(List<Entrega> entregas, String mes, String ano) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        List<Entrega> entregasFiltradas = new ArrayList<>(entregas.stream()
                .filter((entrega -> entrega.getDataDaEntrega().format(monthNumberFormat).equals(mes) && entrega.getDataDaEntrega().format(yearNumberFormat).equals(ano)))
                .toList());

        entregasFiltradas.sort((entrega1, entrega2) -> {
            Collator collator = Collator.getInstance(new Locale("pt", "BR"));
            return collator.compare(entrega1.getProdutor().getNome(), entrega2.getProdutor().getNome());
        });

        entregasFiltradas.forEach(entrega -> {
            System.out.println(entrega.getDataDaEntrega() + " - " + entrega.getProdutor().getNome());
        });

        Map<Produtor, Map<Produto, List<DetalhesEntrega>>> entregasPorProd = new HashMap<>();

        BigDecimal totalGeral = BigDecimal.valueOf(0);

        for (Entrega entrega : entregasFiltradas) {
            Produtor produtor = entrega.getProdutor();
            List<DetalhesEntrega> detalhesEntregas = entrega.getDetalhesEntrega();

            BigDecimal total = detalhesEntregas.stream()
                    .map(DetalhesEntrega::getTotal)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalGeral = totalGeral.add(total);

            Map<Produto, List<DetalhesEntrega>> entregasPorProduto = entregasPorProd.computeIfAbsent(produtor, k -> new HashMap<>());
            for(DetalhesEntrega detalhesEntrega : detalhesEntregas) {
                Produto produto = detalhesEntrega.getProduto();

                entregasPorProduto
                        .computeIfAbsent(produto, k -> new ArrayList<>())
                        .add(detalhesEntrega);
            }

        }

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            regularFont = PdfFontFactory.createFont("basic_fonts/static/Lora-Regular.ttf", "Identity-H");
            boldFont = PdfFontFactory.createFont("basic_fonts/static/Lora-Bold.ttf", "Identity-H");

            addMainHeader(document, "Prefeitura Municipal de Colinas", -5, regularFont);
            addMainHeader(document, "Secretaria Municipal de Educação", -5, regularFont);
            addMainHeader(document, "Secretaria Municipal de Agricultura", -5, regularFont);
            addMainHeader(document, "Departamento de Alimentação Escolar-DAE", -5, regularFont);
            addMainHeader(document, "RELATORIO AGRICULTURA FAMILIAR", -5, regularFont);

            addMainHeader(document, "MÊS: " + entregas.get(0).getDataDaEntrega().format(monthFormatter).toUpperCase(), 10, regularFont);

            for (Map.Entry<Produtor, Map<Produto, List<DetalhesEntrega>>> entry : entregasPorProd.entrySet()) {
                BigDecimal totalQuantidadeGeral = BigDecimal.ZERO;
                Produtor produtor = entry.getKey();
                Map<Produto, List<DetalhesEntrega>> produtos = entry.getValue();

                // Criar uma única tabela para o produtor
                Table tableEntrega = createTableHeader();

                boolean isFirstLine = true;

                for (Map.Entry<Produto, List<DetalhesEntrega>> produtoEntry : produtos.entrySet()) {
                    Produto produto = produtoEntry.getKey();
                    List<DetalhesEntrega> detalhesEntregas = produtoEntry.getValue();

                    BigDecimal totalQuantidade = detalhesEntregas.stream()
                            .map(DetalhesEntrega::getQuantidade)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);

                    totalQuantidadeGeral = totalQuantidadeGeral.add(totalQuantidade.multiply(produto.getPrecoMedio()));

                    boolean isFirstLineProd = true;

                    for (DetalhesEntrega d : detalhesEntregas) {
                        // Na primeira linha, adiciona os dados do produtor
                        if (isFirstLine) {
                            tableEntrega.addCell(createdStyledCell(produtor.getNome(), regularFont).setBorderBottom(null));
                            tableEntrega.addCell(createdStyledCell(produtor.getCpf(), regularFont).setBorderBottom(null));
                            tableEntrega.addCell(createdStyledCell(produtor.getCaf(), regularFont).setBorderBottom(null));
                            isFirstLine = false;
                        } else {
                            // Para as linhas seguintes, adiciona células vazias nas colunas do produtor
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                        }

                        // Adiciona os detalhes do produto apenas na primeira linha do grupo
                        if (isFirstLineProd) {
                            tableEntrega.addCell(createdStyledCell(produto.getDescricao(), regularFont).setBorderBottom(null));
                            tableEntrega.addCell(createdStyledCell(produto.getUnidade(), regularFont).setBorderBottom(null));
                            tableEntrega.addCell(createdStyledCell(totalQuantidade.toString(), regularFont).setBorderBottom(null));
                            tableEntrega.addCell(createdStyledCell(currencyBr.format(produto.getPrecoMedio()), regularFont).setBorderBottom(null));
                            tableEntrega.addCell(createdStyledCell(currencyBr.format(totalQuantidade.multiply(produto.getPrecoMedio())), regularFont).setBorderBottom(null));
                            isFirstLineProd = false;
                        } else {
                            // Preenche as células vazias nas colunas do produto
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                            tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                        }
                    }

                }

                tableEntrega.setWidth(UnitValue.createPercentValue(100));
                tableEntrega.setKeepTogether(true);

                // Adiciona a tabela ao documento
                document.add(tableEntrega);
                addTotalTable(document, totalQuantidadeGeral);
            }
            addParagraph(document, "", regularFont);
            addTotalGeral(document, totalGeral);
            addFooter(document);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void addFooter(Document document) {

        Paragraph text = new Paragraph("___________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFixedPosition(40, 180, UnitValue.createPercentValue(100));
        document.add(text);

        Paragraph signature = new Paragraph("ASSINATURA")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10)
                .setFixedPosition(40, 140, UnitValue.createPercentValue(100));
        document.add(signature);

    }

    private Table createTableHeader() {
        UnitValue[] columnWidthsEntrega = {
                UnitValue.createPointValue(100), // PRODUTOR
                UnitValue.createPointValue(80),  // CPF
                UnitValue.createPointValue(80),  // CAF
                UnitValue.createPointValue(120), // PRODUTO
                UnitValue.createPointValue(40),  // UND
                UnitValue.createPointValue(60),  // QTD
                UnitValue.createPointValue(80),  // R$ / UNT
                UnitValue.createPointValue(100)  // TOTAL
        };

        Table tableEntrega = new Table(columnWidthsEntrega);
        tableEntrega.addHeaderCell(createdStyledHeader("PRODUTOR", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("CPF", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("CAF", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("PRODUTO", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("UND", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("QTD", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("R$ / UNT", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("TOTAL", boldFont, customGray));
        tableEntrega.setWidth(UnitValue.createPercentValue(100));
        tableEntrega.setKeepTogether(true);
        return tableEntrega;
    }

    private void addTotalGeral(Document document, BigDecimal totalGeral) {
        UnitValue[] columnWidthsTotalGeral = {UnitValue.createPercentValue(83.33f),
                UnitValue.createPercentValue(16.66f)};

        Table tableTotalGeral = new Table(columnWidthsTotalGeral);

        tableTotalGeral.addCell(createdStyledHeader("TOTAL GERAL", boldFont, customTotalGeralColor));
        tableTotalGeral.addCell(createdStyledCell(currencyBr.format(totalGeral), regularFont));

        tableTotalGeral.setWidth(UnitValue.createPercentValue(100));  // Faz a tabela ocupar 100% da largura disponível
        tableTotalGeral.setKeepTogether(true);  // Garante que a tabela não seja dividida em várias páginas

        document.add(tableTotalGeral);
    }

    private void addTotalTable(Document document, BigDecimal totalQuantidadeGeral) {

        UnitValue[] columnWidthsTotalGeral = {
                UnitValue.createPercentValue(83.33f),
                UnitValue.createPercentValue(16.66f)
        };

        Table tableTotal = new Table(columnWidthsTotalGeral);

        tableTotal.addCell(createdStyledHeader("TOTAL / PRODUTOR", boldFont, customTotalColor));
        tableTotal.addCell(createdStyledCell(currencyBr.format(totalQuantidadeGeral), regularFont));

        tableTotal.setWidth(UnitValue.createPercentValue(100));  // Faz a tabela ocupar 100% da largura disponível
        tableTotal.setKeepTogether(true);  // Garante que a tabela não seja dividida em várias páginas

        document.add(tableTotal);
    }


    private void addTable(Document document, Produtor produtor, Produto produto,
                          List<DetalhesEntrega> detalhesEntregas) {


        BigDecimal totalQuantidade = detalhesEntregas.stream()
                .map(DetalhesEntrega::getQuantidade) // Extrai a quantidade
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Definindo as larguras das colunas para ocupar 100% da página
        UnitValue[] columnWidthsEntrega = {
                UnitValue.createPercentValue(20f),
                UnitValue.createPercentValue(12f),
                UnitValue.createPercentValue(12f),
                UnitValue.createPercentValue(18f),
                UnitValue.createPercentValue(6f),
                UnitValue.createPercentValue(8f),
                UnitValue.createPercentValue(12f),
                UnitValue.createPercentValue(12f)
        };

        Table tableEntrega = new Table(columnWidthsEntrega);
        tableEntrega.addHeaderCell(createdStyledHeader("PRODUTOR", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("CPF", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("CAF", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("PRODUTO", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("UND", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("QTD", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("R$ / UNT", boldFont, customGray));
        tableEntrega.addHeaderCell(createdStyledHeader("TOTAL", boldFont, customGray ));

        boolean isFirstLine = true;
        boolean isFirstLineProd = true;

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
            tableEntrega.addCell(createdStyledCell(produtor.getCpf(), regularFont));
            tableEntrega.addCell(createdStyledCell(produtor.getCaf(), regularFont));
            if(isFirstLineProd) {
                tableEntrega.addCell(createdStyledCell(produto.getDescricao(),
                        regularFont));
                tableEntrega.addCell(createdStyledCell(produto.getUnidade(), regularFont));
                tableEntrega.addCell(createdStyledCell(totalQuantidade.toString(), regularFont));
                tableEntrega.addCell(createdStyledCell(currencyBr.format(produto.getPrecoMedio()),
                        regularFont));
                tableEntrega.addCell(createdStyledCell(currencyBr.format(totalQuantidade.multiply(produto.getPrecoMedio())),
                        regularFont));
                isFirstLineProd = false;
            }
            else {
                tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                tableEntrega.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
            }


        }

        tableEntrega.setWidth(UnitValue.createPercentValue(100));  // Faz a tabela ocupar 100% da largura disponível
        tableEntrega.setKeepTogether(true);  // Garante que a tabela não seja dividida em várias páginas

        document.add(tableEntrega);
    }


    private Cell createdStyledCell(String content, PdfFont font) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(ColorConstants.WHITE)
                .setFont(font)
                .setFontSize(8)
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