package com.siae.relatorios;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.siae.entities.*;
import com.siae.services.ProdutoService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.Collator;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RelacaoEntregaProdutor {

    private final ProdutoService produtoService;
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

    public RelacaoEntregaProdutor(ProdutoService produtoService) {
        this.produtoService = produtoService;
    }

    public byte[] createPdf(List<Entrega> entregas, String mes, String ano) {
        System.out.println("ENTREGAS: " + entregas.size());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        List<Entrega> entregasFiltradas = new ArrayList<>(entregas.stream()
                .filter((entrega -> entrega.getDataDaEntrega().format(monthNumberFormat).equals(mes) && entrega.getDataDaEntrega().format(yearNumberFormat).equals(ano)))
                .toList());

        System.out.println("ENTREGAS FILTRADAS: " + entregasFiltradas.size());
        Map<Long, List<DetalhesEntrega>> entregasPorProduto = new HashMap<>();

        for(Entrega entrega : entregasFiltradas){
            for(DetalhesEntrega detalheEntrega : entrega.getDetalhesEntrega()) {
                Long produtoId = detalheEntrega.getProduto().getId();

                entregasPorProduto
                        .computeIfAbsent(produtoId, k -> new ArrayList<>())
                        .add(detalheEntrega);
            }
        }

        System.out.println("ENTREGAS POR PRODUTOR: " + entregasPorProduto.size());


        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            pdfDoc.setDefaultPageSize(new PageSize(395, 700).rotate());
            Document document = new Document(pdfDoc);

            regularFont = PdfFontFactory.createFont("fonts/static/Roboto-Regular.ttf", "Identity-H");
            boldFont = PdfFontFactory.createFont("fonts/static/Roboto-Bold.ttf", "Identity-H");

            addMainHeader(document, "Programa de Aquisição de Alimentos", 0, boldFont);
//            addMainHeader(document, "Data da Entrega " + entrega.getDataDaEntrega().format(formatter), 0, regularFont);


            addTable(document, entregas.get(0).getProdutor(), entregasPorProduto);

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

    private void addTable(Document document, Produtor produtor, Map<Long, List<DetalhesEntrega>> entregasPorProduto) {
        System.out.println("CHAMOU O MÉTODO");


        float[] columnWidthsRelacao = {1};
        float[] columnWidthsProdutorNome = {1};
        float[] columnWidthsProdutos = {1, 1, 1, 1, 1};
        float[] columnWidthsProdutorDocs = {1, 1};
        float[] columnWidthsTotalGeral = {4000f, 450f};

        // Nome do Produtor
        Table tableProdutorNome = new Table(columnWidthsProdutorNome);
        tableProdutorNome.addHeaderCell(createdStyledHeader("I – IDENTIFICAÇÃO DO AGRICULTOR/ FORNECEDOR", boldFont));
        tableProdutorNome.addCell(createdStyledCell("AGRICULTOR: " + produtor.getNome(), regularFont));
        tableProdutorNome.setWidth(UnitValue.createPercentValue(100));

        Table tableProdutorDocs = new Table(columnWidthsProdutorDocs);
        tableProdutorDocs.addCell(createdStyledCell("5 - N° da CAF - N° " + produtor.getCaf(),
                regularFont));
        tableProdutorDocs.addCell(createdStyledCell("6 - CPF N° " + produtor.getCpf(),
                regularFont));
        tableProdutorDocs.setWidth(UnitValue.createPercentValue(100));

        Table tableRelacao = new Table(columnWidthsRelacao);
        tableRelacao.addHeaderCell(createdStyledHeader("II - RELAÇÃO DE PRODUTOS", boldFont));
        tableRelacao.setWidth(UnitValue.createPercentValue(100));

        Table tableProdutos = new Table(columnWidthsProdutos);
        tableProdutos.addHeaderCell(createdStyledCell("PRODUTOS", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("UNIDADES", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("QUANTIDADE ENTREGUE", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("PREÇO UNIT. R$", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("VALOR TOTAL R$", regularFont));
        BigDecimal totalQuantidadeGeral = BigDecimal.ZERO;

        for(Map.Entry<Long, List<DetalhesEntrega>>  entry : entregasPorProduto.entrySet()) {
            Long produtoId = entry.getKey();
            Produto produto = produtoService.findById(produtoId);
            System.out.println("PRODUTO: " + produto);
            List<DetalhesEntrega> detalhes = entry.getValue();
            BigDecimal totalQuantidade = detalhes.stream()
                    .map(DetalhesEntrega::getQuantidade)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            totalQuantidadeGeral = totalQuantidadeGeral.add(totalQuantidade.multiply(produto.getPrecoMedio()));

            boolean isFirstLineProd = true;

            for(DetalhesEntrega d : detalhes) {

                if (isFirstLineProd) {
                    tableProdutos.addCell(createdStyledCell(d.getProduto().getDescricao(), regularFont).setBorderBottom(null));
                    tableProdutos.addCell(createdStyledCell(d.getProduto().getUnidade(), regularFont).setBorderBottom(null));
                    tableProdutos.addCell(createdStyledCell(totalQuantidade.toString(), regularFont).setBorderBottom(null));
                    tableProdutos.addCell(createdStyledCell(currencyBr.format(d.getProduto().getPrecoMedio()), regularFont).setBorderBottom(null));
                    tableProdutos.addCell(createdStyledCell(currencyBr.format(totalQuantidade.multiply(d.getProduto().getPrecoMedio())), regularFont).setBorderBottom(null));
                    isFirstLineProd = false;
                } else {
                    // Preenche as células vazias nas colunas do produto
                    tableProdutos.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                    tableProdutos.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                    tableProdutos.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                    tableProdutos.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                    tableProdutos.addCell(new Cell().setBorderBottom(null).setBorderTop(null));
                }
            }
        }

        tableProdutos.setWidth(UnitValue.createPercentValue(100));
        tableProdutos.setKeepTogether(true);

        Table tableTotalGeral = new Table(columnWidthsTotalGeral);
        tableTotalGeral.addCell(createdStyledCell("TOTAL", regularFont));
        tableTotalGeral.addCell(createdStyledCell("R" + currencyBr.format(totalQuantidadeGeral), regularFont));
//

        tableProdutorNome.setKeepTogether(true);
        tableProdutorDocs.setKeepTogether(true);
        tableProdutos.setKeepTogether(true);
        tableRelacao.setKeepTogether(true);
        tableTotalGeral.setKeepTogether(false);
//
        document.add(tableProdutorNome);
        document.add(tableProdutorDocs);
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
