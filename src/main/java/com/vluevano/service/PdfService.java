package com.vluevano.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.vluevano.model.Compra;
import com.vluevano.model.Venta;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfService {

    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12,  new java.awt.Color(255, 255, 255));
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10);

    /**
     * Genera un reporte PDF de ventas con el formato especificado
     * @param archivo
     * @param listaVentas
     * @param rangoFechas
     */
    public void generarReporteVentas(File archivo, List<Venta> listaVentas, String rangoFechas) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivo));
            document.open();

            Paragraph titulo = new Paragraph("Reporte de Ventas", FONT_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("Generado: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("Rango: " + rangoFechas));
            document.add(new Paragraph(" ")); 

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 3, 2, 2});

            crearCeldaEncabezado(table, "Folio");
            crearCeldaEncabezado(table, "Fecha");
            crearCeldaEncabezado(table, "Cliente");
            crearCeldaEncabezado(table, "Vendedor");
            crearCeldaEncabezado(table, "Total");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            double granTotal = 0;

            for (Venta v : listaVentas) {
                table.addCell(crearCelda(String.valueOf(v.getIdventa())));
                table.addCell(crearCelda(v.getFechaVenta().format(fmt)));
                table.addCell(crearCelda(v.getCliente() != null ? v.getCliente().getNombreCliente() : "Público General"));
                table.addCell(crearCelda(v.getUsuario().getNombreUsuario()));
                
                String totalStr = String.format("$%.2f", v.getTotalVenta());
                PdfPCell celdaTotal = crearCelda(totalStr);
                celdaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(celdaTotal);

                granTotal += v.getTotalVenta();
            }

            document.add(table);

            Paragraph totalFinal = new Paragraph("Total Periodo: $" + String.format("%.2f", granTotal), FONT_TITULO);
            totalFinal.setAlignment(Element.ALIGN_RIGHT);
            totalFinal.setSpacingBefore(10);
            document.add(totalFinal);

        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        } finally {
            document.close();
        }
    }

    /**
     * Genera un reporte PDF de compras con el formato especificado
     * @param archivo
     * @param listaCompras
     * @param rangoFechas
     */
    public void generarReporteCompras(File archivo, List<Compra> listaCompras, String rangoFechas) throws IOException {
        Document document = new Document(PageSize.A4);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivo));
            document.open();

            Paragraph titulo = new Paragraph("Reporte de Compras", FONT_TITULO);
            titulo.setAlignment(Element.ALIGN_CENTER);
            document.add(titulo);

            document.add(new Paragraph("Generado: " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
            document.add(new Paragraph("Rango: " + rangoFechas));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 3, 2, 2});

            crearCeldaEncabezado(table, "ID");
            crearCeldaEncabezado(table, "Fecha");
            crearCeldaEncabezado(table, "Origen (Prov/Fab)");
            crearCeldaEncabezado(table, "Comprador");
            crearCeldaEncabezado(table, "Total");

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            double granTotal = 0;

            for (Compra c : listaCompras) {
                table.addCell(crearCelda(String.valueOf(c.getIdcompra())));
                table.addCell(crearCelda(c.getFechaCompra().format(fmt)));

                String origen = "Desconocido";
                if (c.getProveedor() != null) origen = "[P] " + c.getProveedor().getNombreProv();
                else if (c.getFabricante() != null) origen = "[F] " + c.getFabricante().getNombreFabricante();
                table.addCell(crearCelda(origen));

                table.addCell(crearCelda(c.getUsuario().getNombreUsuario()));

                String totalStr = String.format("$%.2f", c.getTotalCompra());
                PdfPCell celdaTotal = crearCelda(totalStr);
                celdaTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(celdaTotal);

                granTotal += c.getTotalCompra();
            }

            document.add(table);

            Paragraph totalFinal = new Paragraph("Total Gastos: $" + String.format("%.2f", granTotal), FONT_TITULO);
            totalFinal.setAlignment(Element.ALIGN_RIGHT);
            totalFinal.setSpacingBefore(10);
            document.add(totalFinal);

        } catch (DocumentException e) {
            throw new IOException(e.getMessage());
        } finally {
            document.close();
        }
    }

    /**
     * Crea una celda de encabezado con el estilo definido
     * @param table
     * @param texto
     */
    private void crearCeldaEncabezado(PdfPTable table, String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_HEADER));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBackgroundColor(new java.awt.Color(249, 115, 22));
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Crea una celda normal con el estilo definido
     * @param texto
     */
    private PdfPCell crearCelda(String texto) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_NORMAL));
        cell.setPadding(4);
        return cell;
    }
}