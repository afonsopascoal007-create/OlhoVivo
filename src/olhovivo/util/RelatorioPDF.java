package olhovivo.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import olhovivo.model.vo.Denuncia;

/**
 * Relatorio PDF das denuncias de uma esquadra (iText 5: Document, PdfWriter, Paragraph,
 * PdfPTable com cabecalho). Se o seu jar for o iText 2.1.7, troque "com.itextpdf.text"
 * por "com.lowagie.text" nos imports.
 */
public final class RelatorioPDF {
    private RelatorioPDF() { }

    public static void gerar(File destino, String esquadra, String agente, List<Denuncia> lista)
            throws IOException, DocumentException {
        Document doc = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
        PdfWriter.getInstance(doc, new FileOutputStream(destino));
        doc.open();

        Font titulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(0x1F, 0x3A, 0x5F));
        Font normal = FontFactory.getFont(FontFactory.HELVETICA, 10);
        Font cab = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.WHITE);
        Font emerg = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, new BaseColor(0xC6, 0x28, 0x28));
        Font cel = FontFactory.getFont(FontFactory.HELVETICA, 9);

        Paragraph p = new Paragraph("OLHO VIVO - Relatorio de denuncias", titulo);
        p.setAlignment(Element.ALIGN_CENTER);
        doc.add(p);
        doc.add(new Paragraph("Esquadra: " + esquadra + "    Agente: " + agente + "    Emitido em: "
                + new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()), normal));
        doc.add(new Paragraph("Total de denuncias: " + lista.size(), normal));
        doc.add(new Paragraph(" "));

        PdfPTable t = new PdfPTable(new float[]{1f, 2.4f, 2.4f, 2f, 1.6f, 9f});
        t.setWidthPercentage(100);
        t.setHeaderRows(1);
        for (String h : new String[]{"ID", "Data/hora", "Categoria", "Estado", "Grav.", "Descricao"}) {
            PdfPCell c = new PdfPCell(new Phrase(h, cab));
            c.setBackgroundColor(new BaseColor(0x1F, 0x3A, 0x5F));
            c.setPadding(5);
            t.addCell(c);
        }
        SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (Denuncia d : lista) {
            Font ft = d.isEmergencia() ? emerg : cel;
            t.addCell(new PdfPCell(new Phrase(String.valueOf(d.getId()), ft)));
            t.addCell(new PdfPCell(new Phrase(f.format(d.getDataHora()), cel)));
            t.addCell(new PdfPCell(new Phrase(d.getCategoria() + (d.isEmergencia() ? " (EMERGENCIA)" : ""), ft)));
            t.addCell(new PdfPCell(new Phrase(d.getEstado(), cel)));
            t.addCell(new PdfPCell(new Phrase(String.valueOf(d.getGravidade()), cel)));
            t.addCell(new PdfPCell(new Phrase(d.getDescricao(), cel)));
        }
        doc.add(t);
        doc.close();
    }
}
