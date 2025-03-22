package com.siae.relatorios;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.siae.entities.Ata;
import com.siae.entities.Produtor;
import com.siae.entities.ProjetoDeVenda;
import com.siae.entities.ProjetoProduto;
import com.siae.services.ProdutorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.text.Collator;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;


@Service
public class AtaDaChamadaPDF {


    private final ProdutorService  produtorService;

    @Autowired
    public AtaDaChamadaPDF(ProdutorService produtorService) {
        this.produtorService = produtorService;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
    DateTimeFormatter yearNumberFormat = DateTimeFormatter.ofPattern("yyyy");
    DateTimeFormatter dayNumberFormat = DateTimeFormatter.ofPattern("dd");
    PdfFont regularFont;
    PdfFont boldFont;
    NumberFormat currencyBr = NumberFormat.getCurrencyInstance();

    TextAlignment right = TextAlignment.RIGHT;
    TextAlignment left = TextAlignment.LEFT;
    TextAlignment center = TextAlignment.CENTER;
    TextAlignment justified = TextAlignment.JUSTIFIED;
    public byte[] createPdf(Ata ata) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        List<Produtor> produtores = produtorService.findAll();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            regularFont = PdfFontFactory.createFont("fonts/static/Roboto-Regular.ttf", "Identity-H");
            boldFont = PdfFontFactory.createFont("fonts/static/Roboto-Bold.ttf", "Identity-H");

            Paragraph paragraph1 = new Paragraph()
                    .add(new Text("Estado do Maranhão")
                            .setFont(boldFont)).setTextAlignment(center).setFontSize(10)
                    .setFontColor(ColorConstants.GRAY);

            Paragraph paragraphh = new Paragraph()
                    .add(new Text("MUNICÍPIO DE COLINAS")
                            .setFont(boldFont)).setTextAlignment(center).setFontSize(10);

            Paragraph paragraph = new Paragraph()
                    .add(new Text("Ao " + ata.getData().format(dayNumberFormat) + " (" + converterNumeroParaExtenso(Integer.parseInt(ata.getData().format(dayNumberFormat))) + ")" +
                            "  " +
                            "dia" +
                            " do mês de " + ata.getData().format(monthFormatter) + " de 2025, ")
                            .setFont(boldFont)).setTextAlignment(justified).setFontSize(10) // Deixa essa parte em negrito
                    .add(new Text("às 9:00 (nove horas), na sala de reunião desta Prefeitura " +
                            "Municipal, nesta cidade de Colinas - Maranhão, reuniu-se a CPL, " +
                            "instituída pela Portaria Nº 39/2025, de 01.01." + ata.getData().format(yearNumberFormat) +
                            ", por ato " +
                            "da " +
                            "Senhor Prefeito, ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10) // Texto normal
                    .add(new Text(ata.getPrefeito()).setFont(boldFont)).setTextAlignment(justified).setFontSize(10) // Nome do contratante em negrito
                    .add(new Text(", conforme exigência ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("da Lei Federal Nº8.666/93 e suas posteriores alterações, o " +
                            "“Aviso de Licitação, ").setFont(boldFont)).setTextAlignment(justified).setFontSize(10) // Nome do fornecedor em negrito
                    .add(new Text("foi afixado no quadro de aviso da Prefeitura Municipal, Secretaria Municipal de Saúde e Secretaria Municipal de Educação, Cultura e Desporto e Lazer, bem como entregue o Edital diretamente aos  Agricultores Familiar Individual, conforme Mapa de Controle de Entrega do Edital da Chamada Pública, devidamente assinado e datado pelos seus representantes legais, apenso ao presente Processo. No dia e hora marcada para abertura do Processo de Dispensa de Licitação compareceram os agricultores conforme discriminação abaixo relacionados:")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10);

            Paragraph second = new Paragraph()
                    .add(new Text("Deu-se prosseguimento com o recebimento dos Envelopes Nº 01 e Nº 02 solicitou a representante da referida Associação que entregasse a credencial e o(s) envelope(s) Deu-se prosseguimento com o recebimento dos Envelopes Nº 01 e Nº 02 solicitou a representante da referida Associação que entregasse a credencial e o(s) envelope(s) ")
                    .setFont(regularFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("nº. 01 e nº.   02. ").setFont(boldFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("Após a análise da documentação apresentada, pela(s) agricultores ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("Familiar Individual ")
                            .setFont(boldFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("foram considerados devidamente habilitados, o Presidente Comissão Permanente de Licitação/CPL, autorizou a abertura do envelope nº ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("2 – Projetos de Vendas/Proposta de Preços, ")
                            .setFont(boldFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("sendo consideradas  válidas, uma vez que os valores propostos, encontram - se abaixo do valor estimado na pesquisa de preços de mercado, ainda na mesma sessão foram analisadas os Projetos de Vendas e a Documentação dos Agricultores Familiar Individual, os quais encontram-se aptos a fornecerem os alimentos através da Associação acima referenciada . O resultado da Apuração e Classificação da Proposta de Preços, será afixado no quadro de avisos da Prefeitura Municipal de Colinas. Os autos do processo continuam com vista fraqueados aos interessados. Eu, ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("Carlos dos Santos ")
                            .setFont(boldFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text("lavrei a presente ata, que após lida e achada conforme vai assinada pelo presidente, pelos membros da Comissão Permanente de Licitação/CPL, e representantes legais da Associação Agricultores Individuais associados, conforme detalhamento abaixo:")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10);

            try {
                ClassLoader classLoader = getClass().getClassLoader();
                URL imageUrl = classLoader.getResource("images/colinas.png");
                URL image2Url = classLoader.getResource("images/unicef.png");

                if (imageUrl == null || image2Url == null) {
                    throw new RuntimeException("Imagem não encontrada!");
                }

                ImageData imageData = ImageDataFactory.create(imageUrl);
                ImageData imageData1 = ImageDataFactory.create(image2Url);

                Image image = new Image(imageData);
                Image image1 = new Image(imageData1);

                // Definir tamanho da imagem
                float imgWidth = 100;  // Largura da imagem
                float imgHeight = 50;  // Altura da imagem

                float imgWidth1 = 80;
                float imgHeight1 = 30;

                // Ajustar espaçamento (margens)
                float marginLeft = 50; // Espaçamento da esquerda
                float marginTop = 50;  // Espaçamento do topo
                float marginRight = 470;

                float pageHeight = pdfDoc.getDefaultPageSize().getHeight();
                float y = pageHeight - imgHeight - marginTop; // Posição vertical ajustada
                float y1 = pageHeight - imgHeight1 - marginTop;

                // Posicionar a imagem com margem
                image.setFixedPosition(marginLeft, y, imgWidth);
                image1.setFixedPosition(marginRight, (y1 - 30), imgWidth1);

                document.add(image);
                document.add(image1);

            } catch (Exception e) {
                e.printStackTrace();
            }

            document.add(paragraph1);
            document.add(paragraphh);
            addParagraph(document, "ATA DA SESSÃO DE ABERTURA DA CHAMADA PUBLICA NÚMERO 01 (UM) " +
                    "DE DOIS MIL E VINTE E TRÊS, POR “DISPENSA DE LICITAÇÃO Nº 01/" + ata.getData().format(yearNumberFormat) + "/CPL, " +
                    "CUJO " +
                    "OBJETO É A AQUISIÇÃO DE GÊNEROS ALIMENTÍCIOS ATRAVÉS DE GRUPOS FORMAIS DA " +
                    "AGRICULTURA FAMILIAR E DE EMPREENDEDORES FAMILIARES RURAIS CONSTITUÍDOS EM " +
                    "COOPERATIVAS E ASSOCIAÇÕES OU GRUPOS INFORMAIS DE AGRICULTORES FAMILIARES, " +
                    "PARA O ATENDIMENTO AO PROGRAMA NACIONAL DE ALIMENTAÇÃO ESCOLAR/PNAE, DURANTE" +
                    " O ANO LETIVO DE " + ata.getData().format(yearNumberFormat) +", CUMPRINDO A " +
                    "LEI FEDERAL 11.947/2009,  EM CONFORMIDADE" +
                    " COM AS  ESPECIFICAÇÕES E QUANTIDADES CONTIDAS NO ANEXO I PARTE INTEGRANTE " +
                    "DO EDITAL E CLAUSULAS CONTRATUAIS  ", boldFont, 35);
            document.add(paragraph);
            addTable(document, produtores);
            document.add(second);
            addMainHeader(document, "PRESIDENTE", 30, boldFont);
            addMainHeader(document, ata.getPresidente().toUpperCase(), 0, boldFont);
            addParagraph(document, "MEMBROS", boldFont, 0);
            int count = 1;
            for(String membro  : ata.getMembros()) {
                addParagraph(document, count + " - " + membro.toUpperCase(), boldFont, 0);
            }
            addTableSignature(document, produtores);
            addFooter(document, ata);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void addFooter(Document document, Ata ata) {
        Paragraph footer =
                new Paragraph("COLINAS (MA), " + ata.getData().format(formatter).toUpperCase())
                .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(10).setMarginTop(50);
        document.add(footer);

        Paragraph assign = new Paragraph("___________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(assign);

        Paragraph name = new Paragraph(ata.getNutricionista())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(name);

        Paragraph assign1 = new Paragraph("___________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(assign1);

        Paragraph name1 = new Paragraph(ata.getNutricionista())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(name1);

    }

    private void addTable(Document document, List<Produtor> produtores) {
        float[] columnWidthsProdutor = {1, 1, 1};

        Table tableProdutor = new Table(columnWidthsProdutor);
        tableProdutor.addHeaderCell(createdStyledHeader("N°", regularFont));
        tableProdutor.addHeaderCell(createdStyledHeader("NOME PRODUTOR", regularFont));
        tableProdutor.addHeaderCell(createdStyledHeader("CAF", regularFont));
        int count = 1;
        for(Produtor p : produtores) {
            tableProdutor.addCell(createdStyledCell(count + "", regularFont));
            tableProdutor.addCell(createdStyledCell(p.getNome(), regularFont));
            tableProdutor.addCell(createdStyledCell(p.getCaf(), regularFont));
            tableProdutor.setWidth(UnitValue.createPercentValue(100));
            count++;
        }

        tableProdutor.setKeepTogether(true);

        document.add(tableProdutor);
    }

    private void addTableSignature(Document document, List<Produtor> produtores) {
        UnitValue[] columnWidthsProdutor = {
                UnitValue.createPercentValue(4f),
                UnitValue.createPercentValue(43f),
                UnitValue.createPercentValue(43f),
        };

        Table tableProdutor = new Table(columnWidthsProdutor);
        tableProdutor.addHeaderCell(createdStyledHeader("N°", boldFont));
        tableProdutor.addHeaderCell(createdStyledHeader("NOME PRODUTOR", boldFont));
        tableProdutor.addHeaderCell(createdStyledHeader("ASSINATURA", boldFont));
        int count = 1;
        for(Produtor p : produtores) {
            tableProdutor.addCell(createdStyledCell(count + "", boldFont));
            tableProdutor.addCell(createdStyledCell(p.getNome(), boldFont));
            tableProdutor.addCell(createdStyledCell("", boldFont));
            tableProdutor.setWidth(UnitValue.createPercentValue(100));
            count++;
        }

        tableProdutor.setKeepTogether(true);

        document.add(tableProdutor);
    }

    private Cell createdStyledCell(String content, PdfFont font) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(ColorConstants.WHITE)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private Cell createdStyledHeader(String content, PdfFont font) {
        return new Cell().add(new Paragraph(content))
                .setBackgroundColor(ColorConstants.GRAY)
                .setFont(font)
                .setFontSize(10)
                .setTextAlignment(TextAlignment.CENTER);
    }

    private void addMainHeader(Document document, String headerText, int spacing, PdfFont font) {
        Paragraph header = new Paragraph(headerText)
                .setTextAlignment(TextAlignment.CENTER)
                .setFont(font)
                .setFontSize(10)
                .setMarginTop(spacing);
        document.add(header);
    }

    private void addParagraph(Document document, String text, PdfFont font, float margem) {
        Paragraph header = new Paragraph(text)
                .setTextAlignment(TextAlignment.JUSTIFIED)
                .setFont(font)
                .setFontSize(10)
                .setMarginTop(margem);
        document.add(header);
    }

    public static String converterNumeroParaExtenso(int numero) {
        String[] numerosPorExtenso = {
                "", "um", "dois", "três", "quatro", "cinco", "seis", "sete", "oito", "nove", "dez",
                "onze", "doze", "treze", "quatorze", "quinze", "dezesseis", "dezessete", "dezoito", "dezenove",
                "vinte", "vinte e um", "vinte e dois", "vinte e três", "vinte e quatro", "vinte e cinco",
                "vinte e seis", "vinte e sete", "vinte e oito", "vinte e nove", "trinta", "trinta e um"
        };

        return numerosPorExtenso[numero];
    }
}
