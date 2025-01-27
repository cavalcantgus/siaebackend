package com.siae.relatorios;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
public class RelatorioProjetoDeVendaPDF {
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
	DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
	PdfFont regularFont;
    PdfFont boldFont;
	NumberFormat currencyBr = NumberFormat.getCurrencyInstance();
	public byte[] createPdf(ProjetoDeVenda projetoDeVenda) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);
			
			regularFont = PdfFontFactory.createFont("fonts/static/Roboto-Regular.ttf", "Identity-H");
			boldFont = PdfFontFactory.createFont("fonts/static/Roboto-Bold.ttf", "Identity-H");
			
			addMainHeader(document, projetoDeVenda.getProdutor().getNome(), 0, boldFont);
			addMainHeader(document, "CAF - N° " + projetoDeVenda.getProdutor().getCaf(), 0, regularFont);
			addMainHeader(document, "Validade da CAF - " + projetoDeVenda.getProdutor().getValidadeCaf().format(formatter), 0, regularFont);
			addMainHeader(document, "CPF - N° " + projetoDeVenda.getProdutor().getCpf(), 0, regularFont);
			addMainHeader(document, "PROJETO DE VENDA", 20, boldFont);
			addMainHeader(document, "REFERÊNCIA: CHAMADA PÚBLICA Nº 01/" + projetoDeVenda.getDataProjeto().getYear() + "/" + "CPL POR DISPENSA DE LICITAÇÃO Nº 01/" + projetoDeVenda.getDataProjeto().getYear() + "/" + "CPL", 0, boldFont);
			
			addParagraph(document, "OBJETO: Aquisição de gêneros alimentícios através de Grupos Informais de Agricultores Familiares, para o cumprimento do Programa Nacional de Alimentação Escolar/PNAE, durante o ano letivo de " + projetoDeVenda.getDataProjeto().getYear() + ", que visa atender os alunos da rede municipal de ensino de pré-escolar/educação infantil, ensino fundamental e Educação de Jovens e Adultos–EJA e das Comunidades Quilombolas, em cumprimento da Lei Federal 11.947 de 16 de junho de 2009 e da Resolução FNDE/ CD n.º 38/2009 utilizando pelo menos 30% do recurso do Programa Nacional de Alimentação Escolar (PNAE), da Agricultura Familiar."
					+ "", regularFont);
			
			addParagraph(document, "1.	PROPOSTA DE FORNECIMENTO DE ALIMENTOS PARA AGRICULTORES INDIVIDUAIS", boldFont);
			addTable(document, projetoDeVenda);
			
			addFooter(document, projetoDeVenda);
			
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	private void addFooter(Document document, ProjetoDeVenda projeto) {
		Paragraph footer = new Paragraph("COLINAS (MA), " + projeto.getDataProjeto().format(formatter).toUpperCase())
				 .setTextAlignment(TextAlignment.CENTER)
	                .setFontSize(10)
	                .setFixedPosition(40, 120, UnitValue.createPercentValue(100));
		document.add(footer);
		
		Paragraph assign = new Paragraph("___________________________________________")
				 .setTextAlignment(TextAlignment.CENTER)
	                .setFontSize(10)
	                .setFixedPosition(40, 90, UnitValue.createPercentValue(100));
		document.add(assign);
		
		Paragraph name = new Paragraph(projeto.getProdutor().getNome())
				 .setTextAlignment(TextAlignment.CENTER)
	                .setFontSize(10)
	                .setFixedPosition(40, 70, UnitValue.createPercentValue(100));
		document.add(name);
		
		Paragraph cpf = new Paragraph("CPF Nº " + projeto.getProdutor().getCpf())
				 .setTextAlignment(TextAlignment.CENTER)
	                .setFontSize(10)
	                .setFixedPosition(40, 50, UnitValue.createPercentValue(100));
		document.add(cpf);
		
	}

	private void addTable(Document document, ProjetoDeVenda projetoDeVenda) {
		float[] columnWidthsProjeto = {1};
		float[] columnWidthsProdutorNome = {1};
		float[] columnWidthsProdutorEndereco = {1, 1, 1};
		float[] columnWidthsProdutorDocs = {1, 1, 1};
		float[] columnWidthsProdutorBanco = {1, 1, 1};
		float[] columnWidthsProdutos = {1, 1, 1, 1, 1, 1};
		float[] columnWidthsRelacao = {1};
		float[] columnWidthsTotalGeral = {4000f, 450f};
		
		Table tableProjeto = new Table(columnWidthsProjeto);
		tableProjeto.addHeaderCell(createdStyledHeader("PROJETO DE VENDA DE GÊNEROS ALIMENTÍCIOS DA AGRICULTURA FAMILIAR PARA O PNAE", boldFont));
		tableProjeto.addCell(createdStyledCell("Proposta de Preços Nº " + projetoDeVenda.getId() + "/" + projetoDeVenda.getDataProjeto().getYear(), regularFont));
		tableProjeto.setWidth(UnitValue.createPercentValue(100));
		
		// Nome do Produtor
		Table tableProdutorNome = new Table(columnWidthsProdutorNome);
		tableProdutorNome.addHeaderCell(createdStyledHeader("I – IDENTIFICAÇÃO DO AGRICULTOR/ FORNECEDOR", boldFont));
		tableProdutorNome.addCell(createdStyledCell("1 - NOME DO PROPONENTE: " + projetoDeVenda.getProdutor().getNome(), regularFont));
		tableProdutorNome.setWidth(UnitValue.createPercentValue(100));

		// Endereco do Produtor
		Table tableProdutorEndereco = new Table(columnWidthsProdutorEndereco);
		tableProdutorEndereco.addCell(createdStyledCell("2 - Endereço: " + projetoDeVenda.getProdutor().getEndereco(), regularFont));
		tableProdutorEndereco.addCell(createdStyledCell("3 - Município: " + projetoDeVenda.getProdutor().getMunicipio(), regularFont));
		tableProdutorEndereco.addCell(createdStyledCell("4 - CEP: " + projetoDeVenda.getProdutor().getCep(), regularFont));
		tableProdutorEndereco.setWidth(UnitValue.createPercentValue(100));
		
		// Documentos Produtor
		Table tableProdutorDocs = new Table(columnWidthsProdutorDocs);
		tableProdutorDocs.addCell(createdStyledCell("5 - N° da CAF - N° " + projetoDeVenda.getProdutor().getCaf(), regularFont));
		tableProdutorDocs.addCell(createdStyledCell("6 - CPF N° " + projetoDeVenda.getProdutor().getCpf(), regularFont));
		tableProdutorDocs.addCell(createdStyledCell("7 - CONTATO: " + projetoDeVenda.getProdutor().getContato(), regularFont));
		tableProdutorDocs.setWidth(UnitValue.createPercentValue(100));
		
		Table tableProdutorBanco = new Table(columnWidthsProdutorBanco);
		tableProdutorBanco.addCell(createdStyledCell("8 - Banco indicado para depósito de pagamentos: " + projetoDeVenda.getProdutor().getBanco(), regularFont));
		tableProdutorBanco.addCell(createdStyledCell("9 - N° DA AGÊNCIA: " + projetoDeVenda.getProdutor().getAgencia(), regularFont));
		tableProdutorBanco.addCell(createdStyledCell("10 - N° DA CONTA CORRENTE: " + projetoDeVenda.getProdutor().getConta(), regularFont));
		tableProdutorBanco.setWidth(UnitValue.createPercentValue(100));
		
		Table tableRelacao = new Table(columnWidthsRelacao);
		tableRelacao.addHeaderCell(createdStyledHeader("II - RELAÇÃO DE PRODUTOS", boldFont));
		tableRelacao.setWidth(UnitValue.createPercentValue(100));

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
//		float[]  columnWidths = {1, 5};
//		Table table = new Table(columnWidths);
//		
//		table.addHeaderCell(createdStyledCell("ID"));
//		table.addHeaderCell(createdStyledCell("Descrição"));
//		
//		for(String[] rowData : tableData) {
//			table.addCell(createdStyledCell(rowData[0]));
//			table.addCell(createdStyledCell(rowData[1]));
//		}
		
		tableProdutorNome.setKeepTogether(true);
		tableProdutorEndereco.setKeepTogether(true);
		tableProdutorDocs.setKeepTogether(true);
		tableProdutorBanco.setKeepTogether(true);
		tableProjeto.setKeepTogether(true);
		tableProdutos.setKeepTogether(true);
		tableRelacao.setKeepTogether(true);
		tableTotalGeral.setKeepTogether(false);
//		
		document.add(tableProjeto);
		document.add(tableProdutorNome);
		document.add(tableProdutorEndereco);
		document.add(tableProdutorDocs);
		document.add(tableProdutorBanco);
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
;