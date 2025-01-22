package com.siae.relatorios;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.colors.ColorConstants;
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
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' LLLL yyyy", new Locale("pt", "BR"));
	
	public byte[] createPdf(ProjetoDeVenda projetoDeVenda) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		
		try {
			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);
			
			addMainHeader(document, projetoDeVenda.getProdutor().getNome(), 0);
			addMainHeader(document, "CAF - N° " + projetoDeVenda.getProdutor().getCaf(), 0);
			addMainHeader(document, "CPF - N° " + projetoDeVenda.getProdutor().getCpf(), 0);
			addMainHeader(document, "PROJETO DE VENDA", 10);
			addMainHeader(document, "REFERÊNCIA: CHAMADA PÚBLICA Nº 01/" + projetoDeVenda.getDataProjeto().getYear() + "/" + "CPL POR DISPENSA DE LICITAÇÃO Nº 01/" + projetoDeVenda.getDataProjeto().getYear() + "/" + "CPL", 0);
			
			addParagraph(document, "OBJETO: Aquisição de gêneros alimentícios através de Grupos Informais de Agricultores Familiares, para o cumprimento do Programa Nacional de Alimentação Escolar/PNAE, durante o ano letivo de " + projetoDeVenda.getDataProjeto().getYear() + ", que visa atender os alunos da rede municipal de ensino de pré-escolar/educação infantil, ensino fundamental e Educação de Jovens e Adultos–EJA e das Comunidades Quilombolas, em cumprimento da Lei Federal 11.947 de 16 de junho de 2009 e da Resolução FNDE/ CD n.º 38/2009 utilizando pelo menos 30% do recurso do Programa Nacional de Alimentação Escolar (PNAE), da Agricultura Familiar."
					+ "");
			
			addParagraph(document, "1.	PROPOSTA DE FORNECIMENTO DE ALIMENTOS PARA AGRICULTORES INDIVIDUAIS");
			addTable(document, projetoDeVenda);
			
			addFooter(document, projetoDeVenda);
			
			document.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}

	private void addFooter(Document document, ProjetoDeVenda projeto) {
		Paragraph footer = new Paragraph("COLINAS (MA), " + projeto.getDataProjeto().format(formatter))
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
		tableProjeto.addHeaderCell(createdStyledHeader("PROJETO DE VENDA DE GÊNEROS ALIMENTÍCIOS DA AGRICULTURA FAMILIAR PARA O PNAE"));
		tableProjeto.addCell(createdStyledCell("Proposta de Preços Nº " + projetoDeVenda.getId() + "/" + projetoDeVenda.getDataProjeto().getYear()));
		tableProjeto.setWidth(UnitValue.createPercentValue(100));
		
		// Nome do Produtor
		Table tableProdutorNome = new Table(columnWidthsProdutorNome);
		tableProdutorNome.addHeaderCell(createdStyledHeader("I – IDENTIFICAÇÃO DO AGRICULTOR/ FORNECEDOR"));
		tableProdutorNome.addCell(createdStyledCell("1 - NOME DO PROPONENTE: " + projetoDeVenda.getProdutor().getNome()));
		tableProdutorNome.setWidth(UnitValue.createPercentValue(100));

		// Endereco do Produtor
		Table tableProdutorEndereco = new Table(columnWidthsProdutorEndereco);
		tableProdutorEndereco.addCell(createdStyledCell("2 - Endereço: " + projetoDeVenda.getProdutor().getEndereco()));
		tableProdutorEndereco.addCell(createdStyledCell("3 - Município: " + projetoDeVenda.getProdutor().getMunicipio()));
		tableProdutorEndereco.addCell(createdStyledCell("4 - CEP: " + projetoDeVenda.getProdutor().getCep()));
		tableProdutorEndereco.setWidth(UnitValue.createPercentValue(100));
		
		// Documentos Produtor
		Table tableProdutorDocs = new Table(columnWidthsProdutorDocs);
		tableProdutorDocs.addCell(createdStyledCell("5 - N° da CAF - N° " + projetoDeVenda.getProdutor().getCaf()));
		tableProdutorDocs.addCell(createdStyledCell("6 - CPF N° " + projetoDeVenda.getProdutor().getCpf()));
		tableProdutorDocs.addCell(createdStyledCell("7 - CONTATO: " + projetoDeVenda.getProdutor().getContato()));
		tableProdutorDocs.setWidth(UnitValue.createPercentValue(100));
		
		Table tableProdutorBanco = new Table(columnWidthsProdutorBanco);
		tableProdutorBanco.addCell(createdStyledCell("8 - Banco indicado para depósito de pagamentos: " + projetoDeVenda.getProdutor().getBanco()));
		tableProdutorBanco.addCell(createdStyledCell("9 - N° DA AGÊNCIA: " + projetoDeVenda.getProdutor().getAgencia()));
		tableProdutorBanco.addCell(createdStyledCell("10 - N° DA CONTA CORRENTE: " + projetoDeVenda.getProdutor().getConta()));
		tableProdutorBanco.setWidth(UnitValue.createPercentValue(100));
		
		Table tableRelacao = new Table(columnWidthsRelacao);
		tableRelacao.addHeaderCell(createdStyledHeader("II - RELAÇÃO DE PRODUTOS"));
		tableRelacao.setWidth(UnitValue.createPercentValue(100));

		Table tableProdutos = new Table(columnWidthsProdutos);
		tableProdutos.addHeaderCell(createdStyledCell("PRODUTOS"));
		tableProdutos.addHeaderCell(createdStyledCell("UNIDADES"));
		tableProdutos.addHeaderCell(createdStyledCell("QUANTIDADE TOTAL"));
		tableProdutos.addHeaderCell(createdStyledCell("PERIDIOCIDADE DE ENTREGA"));
		tableProdutos.addHeaderCell(createdStyledCell("PREÇO UNIT. R$"));
		tableProdutos.addHeaderCell(createdStyledCell("VALOR TOTAL R$"));
		
		for(ProjetoProduto p : projetoDeVenda.getProjetoProdutos()) {	
			tableProdutos.addCell(createdStyledCell(p.getProduto().getDescricao()));
			tableProdutos.addCell(createdStyledCell(p.getProduto().getUnidade()));
			tableProdutos.addCell(createdStyledCell(p.getQuantidade().toString()));
			tableProdutos.addCell(createdStyledCell("-"));
			tableProdutos.addCell(createdStyledCell("R$ " + p.getProduto().getPrecoMedio().toString()));
			tableProdutos.addCell(createdStyledCell("R$ " + p.getTotal().toString()));
		}
		
		Table tableTotalGeral = new Table(columnWidthsTotalGeral);
		tableTotalGeral.addCell(createdStyledCell("TOTAL"));
		tableTotalGeral.addCell(createdStyledCell("R$ " + projetoDeVenda.getTotal().toString()));
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

	private Cell createdStyledCell(String content) {
		return new Cell().add(new Paragraph(content))
				.setBackgroundColor(ColorConstants.WHITE)
				.setFontSize(10)
				.setTextAlignment(TextAlignment.LEFT);
	}
	
	private Cell createdStyledHeader(String content) {
		return new Cell().add(new Paragraph(content))
				.setBackgroundColor(ColorConstants.CYAN)
				.setFontSize(10)
				.setTextAlignment(TextAlignment.LEFT);
	}
	
	private void addMainHeader(Document document, String headerText, int spacing) {
		Paragraph header = new Paragraph(headerText)
				.setTextAlignment(TextAlignment.CENTER)
				.setFontSize(10)
				.setMarginTop(spacing);
		document.add(header);	
	}
	
	private void addParagraph(Document document, String text) {
		Paragraph header = new Paragraph(text)
				.setTextAlignment(TextAlignment.JUSTIFIED)
				.setFontSize(10);
		document.add(header);	
	}
}
;