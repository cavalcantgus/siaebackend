package com.siae.relatorios;

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
import com.siae.entities.Produto;
import com.siae.entities.Produtor;
import com.siae.entities.ProjetoDeVenda;
import org.springframework.stereotype.Service;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ProdutosPDF {
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd 'de' MMMM yyyy", new Locale("pt", "BR"));
	DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MMMM", new Locale("pt", "BR"));
	PdfFont regularFont;
    PdfFont boldFont;
	NumberFormat currencyBr = NumberFormat.getCurrencyInstance();
	public byte[] createPdf(List<Produto> produtos) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		try {
			PdfWriter writer = new PdfWriter(baos);
			PdfDocument pdfDoc = new PdfDocument(writer);
			Document document = new Document(pdfDoc);
			
			regularFont = PdfFontFactory.createFont("fonts/static/Roboto-Regular.ttf", "Identity-H");
			boldFont = PdfFontFactory.createFont("fonts/static/Roboto-Bold.ttf", "Identity-H");
			

			addMainHeader(document, "Relação de Produtos", 0, regularFont);


			addTable(document, produtos);
			
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

	private void addTable(Document document, List<Produto> produtos) {
		int count = 1;
		float[] columnWidthsProdutos = {1, 1, 1, 1};

		
		Table tableProdutos = new Table(columnWidthsProdutos);
		tableProdutos.addHeaderCell(createdStyledHeader("N°", boldFont));
		tableProdutos.addHeaderCell(createdStyledHeader("DESCRIÇÃO", boldFont));
		tableProdutos.addHeaderCell(createdStyledHeader("ESPECIFICAÇÂO", boldFont));
		tableProdutos.addHeaderCell(createdStyledHeader("UNIDADE", boldFont));
		tableProdutos.addHeaderCell(createdStyledHeader("PREÇO MÉDIO", boldFont));

		for(Produto p : produtos) {
			tableProdutos.addCell(createdStyledCell(String.valueOf(count), regularFont));
			tableProdutos.addCell(createdStyledCell(p.getDescricao(), regularFont));
			tableProdutos.addCell(createdStyledCell(p.getEspecificacao(), regularFont));
			tableProdutos.addCell(createdStyledCell(p.getUnidade(), regularFont));
			tableProdutos.addCell(createdStyledCell("R" + currencyBr.format(p.getPrecoMedio()), regularFont));
			tableProdutos.setWidth(UnitValue.createPercentValue(100));
			count++;
		}

		tableProdutos.setKeepTogether(true);

		document.add(tableProdutos);
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