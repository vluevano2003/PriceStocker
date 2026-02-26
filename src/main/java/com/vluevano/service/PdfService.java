package com.vluevano.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.vluevano.model.Compra;
import com.vluevano.model.Venta;
import com.vluevano.util.GestorIdioma;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PdfService {

    @Autowired
    private MonedaService monedaService;

    @Autowired
    private GestorIdioma idioma;

    private static final Color COLOR_PRIMARY = new Color(249, 115, 22);
    private static final Color COLOR_DARK_TEXT = new Color(17, 24, 39);
    private static final Color COLOR_BORDER = new Color(229, 231, 235);

    private static final Font FONT_BRAND_LOGO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, COLOR_DARK_TEXT);
    private static final Font FONT_BRAND_DOT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24, COLOR_PRIMARY);
    private static final Font FONT_SUBTITLE = FontFactory.getFont(FontFactory.HELVETICA, 10, new Color(107, 114, 128));
    private static final Font FONT_HEADER_TABLE = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
    private static final Font FONT_DATA_TABLE = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_DARK_TEXT);
    private static final Font FONT_TOTAL = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY);
    private static final Font FONT_DATA_SMALL = FontFactory.getFont(FontFactory.HELVETICA, 8, new Color(55, 65, 81));

    /**
     * Genera un reporte PDF de ventas con formato profesional, incluyendo detalles de cada venta, cliente, vendedor y total en moneda local. El reporte se estiliza con colores y fuentes para mejorar su legibilidad y presentación.
     * @param archivo
     * @param listaVentas
     * @param rangoFechas
     * @throws IOException
     */
    public void generarReporteVentas(File archivo, List<Venta> listaVentas, String rangoFechas) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivo));
            document.open();

            agregarEncabezadoMarca(document, idioma.get("pdf.sales.title"), rangoFechas);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1f, 1.5f, 4.5f, 2.5f, 1.5f, 1.5f });
            table.setSpacingBefore(20);

            agregarCeldaEncabezado(table, idioma.get("pdf.sales.col.folio"));
            agregarCeldaEncabezado(table, idioma.get("pdf.sales.col.date"));
            agregarCeldaEncabezado(table, idioma.get("pdf.sales.col.products"));
            agregarCeldaEncabezado(table, idioma.get("pdf.sales.col.client"));
            agregarCeldaEncabezado(table, idioma.get("pdf.sales.col.seller"));
            agregarCeldaEncabezado(table, idioma.get("pdf.sales.col.total"), Element.ALIGN_RIGHT);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            double granTotal = 0;
            boolean esPar = false;

            String monedaPref = monedaService.getMonedaPorDefecto();
            double tc = monedaService.getTipoCambioActual();
            if (tc == 0) tc = 20.0;

            for (Venta v : listaVentas) {
                Color bgColor = esPar ? new Color(243, 244, 246) : Color.WHITE;

                agregarCeldaDato(table, String.valueOf(v.getIdventa()), Element.ALIGN_CENTER, bgColor);
                agregarCeldaDato(table, v.getFechaVenta().format(fmt), Element.ALIGN_CENTER, bgColor);

                String detalleProductos = idioma.get("pdf.txt.no_details");
                if (v.getDetalles() != null && !v.getDetalles().isEmpty()) {
                    detalleProductos = v.getDetalles().stream()
                            .map(d -> "• " + d.getProducto().getNombreProducto() + " (x" + d.getCantidad() + ") - "
                                    + formatPdfPrecio(d.getPrecioUnitario(), v.getMoneda()))
                            .collect(Collectors.joining("\n"));
                }
                PdfPCell cellProd = new PdfPCell(new Phrase(detalleProductos, FONT_DATA_SMALL));
                estilizarCeldaDato(cellProd, Element.ALIGN_LEFT, bgColor);
                table.addCell(cellProd);

                agregarCeldaDato(table, v.getCliente() != null ? v.getCliente().getNombreCliente() : idioma.get("pdf.txt.general_public"), Element.ALIGN_LEFT, bgColor);
                agregarCeldaDato(table, v.getUsuario().getNombreUsuario(), Element.ALIGN_CENTER, bgColor);

                String totalStr = formatPdfPrecio(v.getTotalVenta(), v.getMoneda());
                agregarCeldaDato(table, totalStr, Element.ALIGN_RIGHT, bgColor);

                double totalDoc = v.getTotalVenta();
                String monedaDoc = v.getMoneda() != null ? v.getMoneda() : "MXN";

                if (monedaDoc.equalsIgnoreCase(monedaPref)) {
                    granTotal += totalDoc;
                } else {
                    if (monedaPref.equalsIgnoreCase("MXN") && monedaDoc.equalsIgnoreCase("USD")) {
                        granTotal += (totalDoc * tc);
                    } else if (monedaPref.equalsIgnoreCase("USD") && monedaDoc.equalsIgnoreCase("MXN")) {
                        granTotal += (totalDoc / tc); 
                    }
                }
                esPar = !esPar;
            }
            document.add(table);
            agregarSeccionTotal(document, idioma.get("pdf.sales.total_income", monedaPref), granTotal);

        } catch (DocumentException e) {
            throw new IOException(idioma.get("pdf.error.sales"), e);
        } finally {
            document.close();
        }
    }

    /**
     * Genera un reporte PDF de compras con formato profesional, incluyendo detalles de cada compra, proveedor o fabricante, usuario responsable y total en moneda local. El reporte se estiliza con colores y fuentes para mejorar su legibilidad y presentación.
     * @param archivo
     * @param listaCompras
     * @param rangoFechas
     * @throws IOException
     */
    public void generarReporteCompras(File archivo, List<Compra> listaCompras, String rangoFechas) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 30, 30);
        try {
            PdfWriter.getInstance(document, new FileOutputStream(archivo));
            document.open();

            agregarEncabezadoMarca(document, idioma.get("pdf.purchases.title"), rangoFechas);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 1f, 1.5f, 4.5f, 2.5f, 1.5f, 1.5f });
            table.setSpacingBefore(20);

            agregarCeldaEncabezado(table, idioma.get("pdf.purchases.col.id"));
            agregarCeldaEncabezado(table, idioma.get("pdf.purchases.col.date"));
            agregarCeldaEncabezado(table, idioma.get("pdf.purchases.col.products"));
            agregarCeldaEncabezado(table, idioma.get("pdf.purchases.col.origin"));
            agregarCeldaEncabezado(table, idioma.get("pdf.purchases.col.buyer"));
            agregarCeldaEncabezado(table, idioma.get("pdf.purchases.col.total"), Element.ALIGN_RIGHT);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            double granTotal = 0;
            boolean esPar = false;

            String monedaPref = monedaService.getMonedaPorDefecto();
            double tc = monedaService.getTipoCambioActual();
            if (tc == 0)
                tc = 20.0;

            for (Compra c : listaCompras) {
                Color bgColor = esPar ? new Color(243, 244, 246) : Color.WHITE;

                agregarCeldaDato(table, String.valueOf(c.getIdcompra()), Element.ALIGN_CENTER, bgColor);
                agregarCeldaDato(table, c.getFechaCompra().format(fmt), Element.ALIGN_CENTER, bgColor);

                String detalleProductos = idioma.get("pdf.txt.no_details");
                if (c.getDetalles() != null && !c.getDetalles().isEmpty()) {
                    detalleProductos = c.getDetalles().stream()
                            .map(d -> "• " + d.getProducto().getNombreProducto() + " (x" + d.getCantidad() + ") - "
                                    + formatPdfPrecio(d.getCostoUnitario(), c.getMoneda()))
                            .collect(Collectors.joining("\n"));
                }
                PdfPCell cellProd = new PdfPCell(new Phrase(detalleProductos, FONT_DATA_SMALL));
                estilizarCeldaDato(cellProd, Element.ALIGN_LEFT, bgColor);
                table.addCell(cellProd);

                String origen = idioma.get("pdf.txt.unknown");
                if (c.getProveedor() != null)
                    origen = idioma.get("pdf.prefix.supplier") + " " + c.getProveedor().getNombreProv();
                else if (c.getFabricante() != null)
                    origen = idioma.get("pdf.prefix.manufacturer") + " " + c.getFabricante().getNombreFabricante();

                agregarCeldaDato(table, origen, Element.ALIGN_LEFT, bgColor);
                agregarCeldaDato(table, c.getUsuario().getNombreUsuario(), Element.ALIGN_CENTER, bgColor);

                String totalStr = formatPdfPrecio(c.getTotalCompra(), c.getMoneda());
                agregarCeldaDato(table, totalStr, Element.ALIGN_RIGHT, bgColor);

                double totalDoc = c.getTotalCompra();
                String monedaDoc = c.getMoneda() != null ? c.getMoneda() : "MXN";

                if (monedaDoc.equalsIgnoreCase(monedaPref)) {
                    granTotal += totalDoc;
                } else {
                    if (monedaPref.equalsIgnoreCase("MXN") && monedaDoc.equalsIgnoreCase("USD")) {
                        granTotal += (totalDoc * tc);
                    } else if (monedaPref.equalsIgnoreCase("USD") && monedaDoc.equalsIgnoreCase("MXN")) {
                        granTotal += (totalDoc / tc); 
                    }
                }

                esPar = !esPar;
            }
            document.add(table);
            agregarSeccionTotal(document, idioma.get("pdf.purchases.total_expenses", monedaPref), granTotal);

        } catch (DocumentException e) {
            throw new IOException(idioma.get("pdf.error.purchases"), e);
        } finally {
            document.close();
        }
    }

    /**
     * Aplica estilos profesionales a una celda de datos en la tabla del PDF, configurando alineación, color de fondo, bordes y padding para mejorar la legibilidad y presentación de la información.
     * @param cell
     * @param alineacion
     * @param bgColor
     */
    private void estilizarCeldaDato(PdfPCell cell, int alineacion, Color bgColor) {
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(new Color(229, 231, 235));
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setPaddingLeft(4);
        cell.setPaddingRight(4);
    }

    /**
     * Agrega un encabezado profesional al documento PDF con el logo de la marca, título del reporte, fecha de generación y rango de fechas del reporte. El encabezado se estiliza con colores y fuentes para mejorar su presentación y legibilidad.
     * @param document
     * @param tituloReporte
     * @param rangoFechas
     * @throws DocumentException
     */
    private void agregarEncabezadoMarca(Document document, String tituloReporte, String rangoFechas)
            throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] { 1, 1 });

        Paragraph logoPara = new Paragraph();
        logoPara.add(new Chunk("PRICESTOCKER", FONT_BRAND_LOGO));
        logoPara.add(new Chunk(".", FONT_BRAND_DOT));
        PdfPCell cellLogo = new PdfPCell(logoPara);
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(cellLogo);

        Paragraph metaPara = new Paragraph();

        metaPara.add(
                new Chunk(tituloReporte + "\n", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_PRIMARY)));
        metaPara.add(new Chunk(
                idioma.get("pdf.header.generated") + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + "\n",
                FONT_SUBTITLE));
        metaPara.add(new Chunk(idioma.get("pdf.header.period") + " " + rangoFechas, FONT_SUBTITLE));

        PdfPCell cellMeta = new PdfPCell(metaPara);
        cellMeta.setBorder(Rectangle.NO_BORDER);
        cellMeta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellMeta.setVerticalAlignment(Element.ALIGN_MIDDLE);
        headerTable.addCell(cellMeta);

        document.add(headerTable);

        PdfPTable separator = new PdfPTable(1);
        separator.setWidthPercentage(100);
        PdfPCell line = new PdfPCell(new Phrase(" "));
        line.setBackgroundColor(COLOR_PRIMARY);
        line.setBorder(Rectangle.NO_BORDER);
        line.setFixedHeight(2f);
        separator.addCell(line);
        separator.setSpacingBefore(10);
        document.add(separator);
    }

    /**
     * Agrega una celda de encabezado a la tabla del PDF con estilos profesionales, configurando alineación, color de fondo, bordes y padding para mejorar la presentación y legibilidad de los títulos de las columnas.
     * @param table
     * @param texto
     */
    private void agregarCeldaEncabezado(PdfPTable table, String texto) {
        agregarCeldaEncabezado(table, texto, Element.ALIGN_CENTER);
    }

    /**
     * Agrega una celda de encabezado a la tabla del PDF con estilos profesionales, configurando alineación, color de fondo, bordes y padding para mejorar la presentación y legibilidad de los títulos de las columnas.
     * @param table
     * @param texto
     * @param alineacion
     */
    private void agregarCeldaEncabezado(PdfPTable table, String texto, int alineacion) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_HEADER_TABLE));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(COLOR_PRIMARY);
        cell.setBorderColor(COLOR_PRIMARY);
        cell.setPaddingTop(8);
        cell.setPaddingBottom(8);
        cell.setPaddingLeft(6);
        cell.setPaddingRight(6);
        table.addCell(cell);
    }

    /**
     * Agrega una celda de dato a la tabla del PDF con estilos profesionales, configurando alineación, color de fondo, bordes y padding para mejorar la legibilidad y presentación de la información en las filas de datos.
     * @param table
     * @param texto
     * @param alineacion
     * @param bgColor
     */
    private void agregarCeldaDato(PdfPTable table, String texto, int alineacion, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, FONT_DATA_TABLE));
        cell.setHorizontalAlignment(alineacion);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bgColor);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.5f);
        cell.setPaddingTop(6);
        cell.setPaddingBottom(6);
        cell.setPaddingLeft(4);
        cell.setPaddingRight(4);
        table.addCell(cell);
    }

    /**
     * Agrega una sección al final del documento PDF para mostrar el total general de ingresos o gastos, con estilos profesionales que incluyen una línea separadora, alineación a la derecha y formato de moneda para mejorar la presentación y legibilidad del total.
     * @param document
     * @param etiqueta
     * @param valor
     * @throws DocumentException
     */
    private void agregarSeccionTotal(Document document, String etiqueta, double valor) throws DocumentException {
        PdfPTable lineTable = new PdfPTable(1);
        lineTable.setWidthPercentage(100);
        PdfPCell line = new PdfPCell(new Phrase(" "));
        line.setBorder(Rectangle.TOP);
        line.setBorderColor(COLOR_PRIMARY);
        line.setBorderWidth(1f);
        lineTable.addCell(line);
        lineTable.setSpacingBefore(10);
        document.add(lineTable);
        Paragraph pTotal = new Paragraph();
        pTotal.setAlignment(Element.ALIGN_RIGHT);

        pTotal.add(new Chunk(etiqueta + " ", FontFactory.getFont(FontFactory.HELVETICA, 12, COLOR_DARK_TEXT)));

        pTotal.add(new Chunk(String.format("$ %,.2f", valor), FONT_TOTAL));
        pTotal.setSpacingBefore(5);
        document.add(pTotal);
    }

    /**
     * Formatea un precio para mostrarlo en el PDF, incluyendo el símbolo de moneda, formato numérico y una conversión aproximada a la moneda preferida del sistema si es diferente. El formato se estiliza para mejorar la legibilidad y presentación de los precios en el reporte.
     * @param precio
     * @param monedaItem
     * @return
     */
    private String formatPdfPrecio(double precio, String monedaItem) {
        if (monedaItem == null)
            monedaItem = "MXN";
        String monedaPref = monedaService.getMonedaPorDefecto();
        String textoOriginal = String.format("$ %,.2f %s", precio, monedaItem);

        if (monedaItem.equalsIgnoreCase(monedaPref))
            return textoOriginal;
        try {
            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0)
                tipoCambio = 20.0;
            double precioConvertido = monedaPref.equalsIgnoreCase("MXN") ? (precio * tipoCambio)
                    : (precio / tipoCambio);
            return String.format("%s (≈ $ %,.2f %s)", textoOriginal, precioConvertido, monedaPref);
        } catch (Exception e) {
            return textoOriginal;
        }
    }
}