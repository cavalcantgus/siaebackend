package com.siae.relatorios;

import java.net.URL;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.layout.element.*;
import com.siae.entities.Contrato;
import com.siae.entities.Produtor;
import com.siae.services.ProdutorService;
import com.siae.services.ProjetoDeVendaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.siae.entities.ProjetoDeVenda;
import com.siae.entities.ProjetoProduto;

@Service
public class ContratoPDF {

    private final ProdutorService produtorService;
    private final ProjetoDeVendaService projetoDeVendaService;

    @Autowired
    public ContratoPDF(ProdutorService produtorService,
                       ProjetoDeVendaService projetoDeVendaService) {
        this.produtorService = produtorService;
        this.projetoDeVendaService = projetoDeVendaService;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
    DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
    DateTimeFormatter yearNumberFormat = DateTimeFormatter.ofPattern("yyyy");
    PdfFont regularFont;
    PdfFont boldFont;
    NumberFormat currencyBr = NumberFormat.getCurrencyInstance();

    TextAlignment right = TextAlignment.RIGHT;
    TextAlignment left = TextAlignment.LEFT;
    TextAlignment center = TextAlignment.CENTER;
    TextAlignment justified = TextAlignment.JUSTIFIED;


    public byte[] createPdf(Contrato contrato) {

        Long produtorId = contrato.getProdutor().getId();
        ProjetoDeVenda projetoDeVenda = projetoDeVendaService.findByProdutorId(produtorId);

        final String clausula1 = "CLÁUSULA PRIMEIRA:\n" +
                "É objeto desta contratação a aquisição de GÊNEROS ALIMENTÍCIOS DA AGRICULTURA FAMILIAR PARA ALIMENTAÇÃO ESCOLAR, " +
                "para alunos da rede de educação básica pública, verba FNDE/PNAE, 1° e 2° semestre de 2024, descritos no quadro " +
                "previsto na Cláusula Quarta, todos de acordo com a " + "CHAMADA PÚBLICA Nº 01/" + projetoDeVenda.getDataProjeto().format(yearNumberFormat) + "/CPL " +
                "por DISPENSA DE LICITAÇÃO Nº 01/" + projetoDeVenda.getDataProjeto().format(yearNumberFormat) + "/CPL," +
                "o qual fica fazendo parte integrante do presente contrato, independentemente de " +
                "anexação ou transcrição.\n";

        final String clausula2 = "CLÁUSULA SEGUNDA:\n" +
                "O CONTRATADO se compromete a fornecer os gêneros alimentícios da Agricultura " +
                "Familiar ao CONTRATANTE conforme descrito na Cláusula Quarta deste Contrato.\n";

        final String clausula3 = "CLÁUSULA TERCEIRA:\n" +
                "O limite individual de venda de gêneros alimentícios do CONTRATADO, será de até " +
                "R$ 40.000,00 (quarenta mil reais) por DAP por ano civil, referente à sua produção, conforme a legislação do Programa Nacional de Alimentação Escolar.\n";

        final String clausula4 = "CLÁUSULA QUARTA:\n" +
                "Pelo fornecimento dos gêneros alimentícios, nos quantitativos descritos abaixo (no quadro), de Gêneros " +
                "Alimentícios da Agricultura Familiar, o (a) CONTRATADO (A) receberá o valor " +
                "total de R$ ," + projetoDeVenda.getTotal() +
                "a) O recebimento das mercadorias dar-se-á mediante apresentação do Termo de Recebimento e das Notas " +
                "Fiscais de Venda pela pessoa responsável pela alimentação no local de entrega, consoante anexo deste Contrato.\n" +
                "b) O preço de aquisição é o preço pago ao fornecedor da agricultura familiar e no cálculo do preço já devem " +
                "estar incluídas as despesas com frete, recursos humanos e materiais, assim como com os encargos fiscais, " +
                "sociais, comerciais, trabalhistas e previdenciários e quaisquer outras despesas necessárias ao cumprimento das " +
                "obrigações decorrentes do presente contrato.\n";

        final String clausula5 = "CLÁUSULA QUINTA:\n" +
                "As despesas decorrentes do presente contrato correrão à conta das seguintes " +
                "dotações orçamentárias: \n";

        final String paragraph2 = "DOTAÇÃO ORÇAMENTÁRIA: \n" +
                "06 - SECRETARIA MUNICIPAL DE EDUCACAO\n" +
                "3.3.90.32.00 - MATERIAL, BEM OU SERVICOS PARA DISTRIBIUCAO GRATUITA\n" +
                "12.361.0251.2036.0000 - ASSISTENCIA ALIMENTAR PARA OS ALUNOS DA REDE MUNICIPAL PROGRAMAS DE TRABALHO: ENSINO FUNDAMENTAL, EDUCACAO INFANTIL EDUCACAO DE JOVENS E ADULTOS - EJA E QUILOMBOLAS PROGRAMA NACIONAL DE AUMENTACAO ESCOLAR - PNAE\n" +
                "\n" +
                "PROGRAMA DA AGRICULTURA FAMILIARCLÁUSULA SEXTA:\n" +
                "O CONTRATANTE, após receber os documentos descritos na Cláusula Quarta, alínea " +
                "“a”, e após a tramitação do processo para instrução e liquidação, efetuará o seu pagamento no valor correspondente às entregas do mês anterior.\n";

        final String clausula7 = "CLÁUSULA SÉTIMA:\n" +
                "O CONTRATANTE que não seguir a forma de liberação de recursos para pagamento do " +
                "CONTRATADO, está sujeito a pagamento de multa de 2%, mais juros de 0,1% ao dia, sobre o valor da parcela vencida.\n";

        final String clausula8 = "CLÁUSULA OITAVA:\n" +
                "O CONTRATANTE se compromete em guardar pelo prazo estabelecido no § 11 do artigo" +
                " 45 da Resolução CD/FNDE nº 26/2013, as cópias das Notas Fiscais de Compra, os Termos de Recebimento e Aceitabilidade, apresentados nas prestações de contas, bem como o Projeto de Venda de Gêneros Alimentícios da Agricultura Familiar para Alimentação Escolar e documentos anexos, estando à disposição para comprovação.\n";

        final String clausula9 = "CLÁUSULA NONA:\n" +
                "É de exclusiva responsabilidade do CONTRATADO o ressarcimento de danos causados " +
                "ao CONTRATANTE ou a terceiros, decorrentes de sua culpa ou dolo na execução do contrato, não excluindo ou reduzindo esta responsabilidade à fiscalização.\n";

        final String clausula10 = "CLÁUSULA DÉCIMA:\n" +
                "O CONTRATANTE em razão da supremacia do interesse público sobre os interesses particulares poderá:\n" +
                "a) modificar unilateralmente o contrato para melhor adequação às finalidades de interesse público, respeitando os direitos do CONTRATADO;\n" +
                "b) rescindir unilateralmente o contrato, nos casos de infração contratual ou inaptidão do CONTRATADO;\n" +
                "c) fiscalizar a execução do contrato;\n" +
                "d) aplicar sanções motivadas pela inexecução total ou parcial do ajuste;\n" +
                "Sempre que o CONTRATANTE alterar ou rescindir o contrato sem restar " +
                "caracterizada, culpa do CONTRATADO, deverá respeitar o equilíbrio econômico-financeiro, garantindo-lhe o aumento da remuneração respectiva ou a indenização por despesas já realizadas.\n";


        final String last = "CLÁUSULA DÉCIMA PRIMEIRA:\n" +
                "A multa aplicada após regular processo administrativo poderá ser descontada dos pagamentos eventualmente devidos pelo CONTRATANTE ou, quando for o caso, cobrada judicialmente.\n" +
                "CLÁUSULA DÉCIMA SEGUNDA:\n" +
                "A fiscalização do presente contrato ficará a cargo do respectivo fiscal de contrato, da Secretaria Municipal de Educação, da Entidade Executora, do Conselho de Alimentação Escolar – CAE e outras entidades designadas pelo contratante ou pela legislação.\n" +
                "CLÁUSULA DÉCIMA TERCEIRA:\n" +
                "O presente contrato rege-se, ainda, pela CHAMADA PÚBLICA N.º 01/" + projetoDeVenda.getDataProjeto().format(yearNumberFormat) + ", " +
                "pela " +
                "Resolução/CD/ FNDE n.º 26/2013, de 17 de junho de 2013 e Resolução n° 4, de 02 de abril de 2015, pela Lei nº 8.666/1993 e pela Lei nº 11.947/2009, em todos os seus termos.\n" +
                "\n" +
                "CLÁUSULA DÉCIMA QUARTA:\n" +
                "Este Contrato poderá ser aditado a qualquer tempo, mediante acordo formal entre as partes, resguardadas as suas condições essenciais.\n" +
                "\n" +
                "CLÁUSULA DÉCIMA QUINTA:\n" +
                "As comunicações com origem neste contrato deverão ser formais e expressas, por meio de carta, que somente terá validade se enviada mediante registro de recebimento ou por fax, transmitido pelas partes.\n" +
                "\n" +
                "CLÁUSULA DÉCIMA SEXTA:\n" +
                "Este Contrato, desde que observada à formalização preliminar à sua efetivação, por carta, consoante Cláusula Décima Quinta, poderá ser rescindido, de pleno direito, independentemente de notificação ou interpelação judicial ou extrajudicial, nos seguintes casos:\n" +
                "a) por acordo entre as partes;\n" +
                "b) pela inobservância de qualquer de suas condições;\n" +
                "c) por quaisquer dos motivos previstos em lei.\n" +
                "\n" +
                "CLÁUSULA DÉCIMA SÉTIMA:\n" +
                "O presente contrato vigorará da sua assinatura até a entrega total dos produtos " +
                "mediante o cronograma apresentado (Cláusula Quarta) ou até 31 de Dezembro de " + contrato.getDataContratacao().format(yearNumberFormat) + ".\n" +
                "CLÁUSULA DÉCIMA OITAVA:\n" +
                "É competente o Foro da Comarca de Colinas/Ma para dirimir qualquer controvérsia que se originar deste contrato.\n" +
                "E, por estarem assim, justos e contratados, assinam o presente instrumento em " +
                "três vias de igual teor e forma, na presença de duas testemunhas.\n";

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            regularFont = PdfFontFactory.createFont("basic_fonts/static/Lora-Regular.ttf", "Identity-H");
            boldFont = PdfFontFactory.createFont("basic_fonts/static/Lora-Bold.ttf", "Identity-H");

            Paragraph paragraph = new Paragraph()
                    .add(new Text("A PREFEITURA MUNICIPAL DE COLINAS/SECRETARIA MUNICIPAL DE EDUCAÇÃO/SEMED, ")
                            .setFont(boldFont)).setTextAlignment(justified).setFontSize(10) // Deixa essa parte em negrito
                    .add(new Text("Órgão de Administração Pública em Geral, inscrita no C.N.P.J. (MF) sob o nº 06.113.682/001-25, com sede à " +
                            "Praça Dias Carneiro, 402, Centro, representada neste ato pela Secretaria Municipal de Educação, a Sra. ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10) // Texto normal
                    .add(new Text(contrato.getContratante()).setFont(boldFont)).setTextAlignment(justified).setFontSize(10) // Nome do contratante em negrito
                    .add(new Text(", inscrita no CPF sob n.º " + contrato.getCpfContratante() + ", doravante denominada CONTRATANTE, e, por outro lado, ")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10)
                    .add(new Text(contrato.getProdutor().getNome()).setFont(boldFont)).setTextAlignment(justified).setFontSize(10) // Nome do fornecedor em negrito
                    .add(new Text(", fornecedor individual, situado à " + contrato.getProdutor().getEndereco() + ", " +
                            contrato.getProdutor().getMunicipio() + "-" + contrato.getProdutor().getEstado() +
                            ", inscrito no CPF sob n.º " + contrato.getProdutor().getCaf() +
                            ", doravante denominado(a) CONTRATADO(A), fundamentados nas disposições da Lei nº 11.947/2009 e da Lei nº 8.666/93, " +
                            "Resolução/CD/FNDE n.º 26/2013, de 17 de junho de 2013, e Resolução n° 4, de 02 de abril de 2015, e tendo em vista o que " +
                            "consta na CHAMADA PÚBLICA Nº 01/" + projetoDeVenda.getDataProjeto().format(yearNumberFormat) + "/CPL " +
                            "por DISPENSA DE LICITAÇÃO Nº 01/" + projetoDeVenda.getDataProjeto().format(yearNumberFormat) + "/CPL, resolvem celebrar o presente contrato " +
                            "mediante as cláusulas que seguem.")
                            .setFont(regularFont)).setTextAlignment(justified).setFontSize(10);

            Paragraph newParagraph = new Paragraph()
                    .add(new Text("CONTRATO DE AQUISIÇÃO DE\n")
                            .setFont(regularFont)).setTextAlignment(right).setFontSize(10) // Deixa essa
                    // parte em negrito
                    .add(new Text("GÊNEROS ALIMENTÍCIOS DA\n")
                            .setFont(regularFont)).setTextAlignment(right).setFontSize(10) // Texto
                    // normal
                    .add(new Text("AGRICULTURA FAMILIAR PARA A\n")
                            .setFont(regularFont)).setTextAlignment(right).setFontSize(10)
                    // Texto normal
                    .add(new Text("ALIMENTAÇÃO ESCOLAR/PNAE")
                            .setFont(regularFont)).setTextAlignment(right).setFontSize(10); //

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

            addMainHeader(document,
                    "CONTRATO N° " + contrato.getId() + "/" + contrato.getDataContratacao().format(yearNumberFormat), 90, regularFont, left);
            document.add(newParagraph);
            document.add(paragraph);
            addParagraph(document, clausula1, regularFont, justified);
            addParagraph(document, clausula2, regularFont, justified);
            addParagraph(document, clausula3, regularFont, justified);
            addParagraph(document, clausula4, regularFont, justified);
            addParagraph(document, "II- RELAÇÃO DE PRODUTOS", boldFont, center);
            addTable(document, projetoDeVenda);
            addParagraph(document, clausula5, regularFont, justified);
            addParagraph(document, paragraph2, regularFont, justified);
            addParagraph(document, clausula7, regularFont, justified);
            addParagraph(document, clausula8, regularFont, justified);
            addParagraph(document, clausula9, regularFont, justified);
            addParagraph(document, clausula10, regularFont, justified);
            addParagraph(document, last, regularFont, justified);
            addParagraph(document, "", regularFont, justified);
            addParagraph(document, "", regularFont, justified);
            addFooter(document, contrato);

            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return baos.toByteArray();
    }

    private void addFooter(Document document, Contrato contrato) {
        Paragraph footer =
                new Paragraph("Colinas (MA), " + contrato.getDataContratacao().format(formatter).toUpperCase())
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(footer);

        Paragraph assign1 = new Paragraph("___________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(assign1);

        Paragraph name = new Paragraph()
                .add(new Text("CONTRATADO (S) ")
                        .setFont(boldFont)).setTextAlignment(center).setFontSize(10) // Deixa essa
                // parte em negrito
                .add(new Text(contrato.getProdutor().getNome())
                        .setFont(regularFont)).setTextAlignment(center).setFontSize(10);
        document.add(name);

        Paragraph producerCpf = new Paragraph()
                .add(new Text("CPF: ")
                        .setFont(regularFont)).setTextAlignment(center).setFontSize(10).setMarginTop(-6) // Deixa essa
                // parte em negrito
                .add(new Text(contrato.getProdutor().getCpf())
                        .setFont(boldFont)).setTextAlignment(center).setFontSize(10).setMarginTop(-6);
        document.add(producerCpf);

        Paragraph assign2 = new Paragraph("___________________________________________")
                .setTextAlignment(TextAlignment.CENTER)
                .setFontSize(10);
        document.add(assign2);

        Paragraph contratante = new Paragraph()
                .add(new Text("CONTRATANTE (S) ")
                        .setFont(boldFont)).setTextAlignment(center).setFontSize(10) // Deixa essa
                // parte em negrito
                .add(new Text(contrato.getContratante())
                        .setFont(regularFont)).setTextAlignment(center).setFontSize(10);
        document.add(contratante);

        Paragraph contratanteCpf = new Paragraph()
                .add(new Text("CPF: ")
                        .setFont(regularFont)).setTextAlignment(center).setFontSize(10).setMarginTop(-6) //
                // Deixa essa
                // parte em negrito
                .add(new Text(contrato.getCpfContratante())
                        .setFont(boldFont)).setTextAlignment(center).setFontSize(10).setMarginTop(-6);
        document.add(contratanteCpf);

    }
//
    private void addTable(Document document, ProjetoDeVenda projetoDeVenda) {
        float[] columnWidthsProdutos = {1, 1, 1, 1, 1, 1};
        float[] columnWidthsTotalGeral = {4000f, 450f};

        Table tableProdutos = new Table(columnWidthsProdutos);
        tableProdutos.addHeaderCell(createdStyledCell("PRODUTOS", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("UNIDADES", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("QUANTIDADE TOTAL", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("PERIDIOCIDADE DE ENTREGA", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("PREÇO UNIT. R$", regularFont));
        tableProdutos.addHeaderCell(createdStyledCell("VALOR TOTAL R$", regularFont));

        for(ProjetoProduto p : projetoDeVenda.getProjetoProdutos()) {
            String inicioEntrega = p.getInicioEntrega().format(monthFormatter);
            String fimEntrega = p.getFimEntrega().format(monthFormatter);
            String peridiocidadeDeEntrega = inicioEntrega.toUpperCase() + " a " + fimEntrega.toUpperCase();
            tableProdutos.addCell(createdStyledCell(p.getProduto().getDescricao(), regularFont));
            tableProdutos.addCell(createdStyledCell(p.getProduto().getUnidade(), regularFont));
            tableProdutos.addCell(createdStyledCell(p.getQuantidade().toString(), regularFont));
            tableProdutos.addCell(createdStyledCell(peridiocidadeDeEntrega, regularFont));
            tableProdutos.addCell(createdStyledCell("R" + currencyBr.format(p.getProduto().getPrecoMedio()), regularFont));
            tableProdutos.addCell(createdStyledCell("R" + currencyBr.format(p.getTotal()), regularFont));
            tableProdutos.setWidth(UnitValue.createPercentValue(100));
        }

        Table tableTotalGeral = new Table(columnWidthsTotalGeral);
        tableTotalGeral.addCell(createdStyledCell("TOTAL", regularFont));
        tableTotalGeral.addCell(createdStyledCell("R" + currencyBr.format(projetoDeVenda.getTotal()), regularFont));


        tableProdutos.setKeepTogether(true);
        tableTotalGeral.setKeepTogether(false);

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

    private void addMainHeader(Document document, String headerText, int spacing, PdfFont font,
                               TextAlignment textAlignment) {
        Paragraph header = new Paragraph(headerText)
                .setTextAlignment(textAlignment)
                .setFont(font)
                .setFontSize(10)
                .setMarginTop(spacing);
        document.add(header);
    }

    private void addParagraph(Document document, String text, PdfFont font, TextAlignment textAlignment) {
        Paragraph header = new Paragraph(text)
                .setTextAlignment(textAlignment)
                .setFont(font)
                .setFontSize(10);
        document.add(header);
    }
}
;