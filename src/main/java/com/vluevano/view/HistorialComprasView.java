package com.vluevano.view;

import com.vluevano.model.Compra;
import com.vluevano.model.DetalleCompra;
import com.vluevano.service.CompraService;
import com.vluevano.service.MonedaService;
import com.vluevano.service.PdfService;
import com.vluevano.util.AppTheme;
import com.vluevano.util.UIFactory;
import com.vluevano.view.base.BaseHistorialView;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HistorialComprasView extends BaseHistorialView<Compra> {

    @Autowired private CompraService compraService;
    @Autowired private PdfService pdfService;
    @Autowired private MonedaService monedaService;

    @Override
    protected String getTituloVentana() {
        return idioma.get("history.purchase.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("history.purchase.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("history.purchase.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("history.search.prompt");
    }

    @Override
    protected String getTxtTablaVacia() {
        return idioma.get("history.purchase.table.empty");
    }

    @Override
    protected String getTxtExportarError() {
        return idioma.get("history.msg.report_error.content");
    }

    @Override
    protected String getTxtExportarVacio() {
        return idioma.get("history.purchase.msg.empty_table.content");
    }

    @Override
    protected String getNombreArchivoPdf() {
        return "Compras";
    }

    @Override
    protected List<Compra> obtenerDatos(LocalDate inicio, LocalDate fin, String busqueda) {
        List<Compra> compras = compraService.obtenerComprasPorRango(inicio, fin);
        
        if (!busqueda.isEmpty()) {
            return compras.stream().filter(c -> {
                boolean matchId = String.valueOf(c.getIdcompra()).contains(busqueda);
                boolean matchUsuario = c.getUsuario() != null && c.getUsuario().getNombreUsuario().toLowerCase().contains(busqueda);
                boolean matchProveedor = c.getProveedor() != null && c.getProveedor().getNombreProv().toLowerCase().contains(busqueda);
                boolean matchFabricante = c.getFabricante() != null && c.getFabricante().getNombreFabricante().toLowerCase().contains(busqueda);
                
                return matchId || matchUsuario || matchProveedor || matchFabricante;
            }).collect(Collectors.toList());
        }
        return compras;
    }

    /**
     * Genera un reporte PDF con los datos de compras filtrados. Este método es llamado desde la lógica común de exportación en la clase base, y utiliza el servicio de PDF para crear el archivo con el formato específico para compras
     */
    @Override
    protected void generarReportePdf(File file, List<Compra> datos, String rangoFechas) throws Exception {
        pdfService.generarReporteCompras(file, datos, rangoFechas);
    }

    /**
     * Configura las columnas específicas para la tabla de compras, incluyendo un botón para ver los detalles de cada compra. Este método es llamado desde la clase base después de inicializar la tabla, y se encarga de agregar las columnas necesarias para mostrar la información relevante de cada compra, así como el botón que abre un pop-up con los detalles de los productos comprados
     */
    @Override
    protected void configurarColumnasTabla() {
        TableColumn<Compra, String> colId = UIFactory.crearColumna(idioma.get("history.col.id"), c -> String.valueOf(c.getIdcompra()), 60);
        colId.setMaxWidth(60);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        TableColumn<Compra, String> colFecha = UIFactory.crearColumna(idioma.get("history.col.date"), c -> c.getFechaCompra().format(fmt), 140);
        colFecha.setMaxWidth(140);

        TableColumn<Compra, Void> colDetalles = new TableColumn<>(idioma.get("history.col.content"));
        colDetalles.setMinWidth(140);
        colDetalles.setMaxWidth(140);
        colDetalles.setCellFactory(param -> new TableCell<>() {
            private final Button btnVer = new Button(idioma.get("history.btn.view_products"));
            {
                btnVer.setStyle("-fx-background-color: " + AppTheme.COLOR_PRIMARY + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-radius: 4;");
                btnVer.setOnAction(event -> mostrarPopUpDetalles(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Compra c = getTableView().getItems().get(getIndex());
                    int items = (c.getDetalles() != null) ? c.getDetalles().size() : 0;
                    btnVer.setText(idioma.get("history.btn.view_items", items));
                    setGraphic(btnVer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Compra, String> colOrigen = UIFactory.crearColumna(idioma.get("history.purchase.col.origin"), c -> {
            String prefixP = idioma.get("history.purchase.prefix.supplier");
            String prefixF = idioma.get("history.purchase.prefix.manufacturer");
            if (c.getProveedor() != null) return prefixP + " " + c.getProveedor().getNombreProv();
            if (c.getFabricante() != null) return prefixF + " " + c.getFabricante().getNombreFabricante();
            return idioma.get("history.txt.unknown");
        }, 140);

        TableColumn<Compra, String> colUsuario = UIFactory.crearColumna(idioma.get("history.col.user"), c -> c.getUsuario().getNombreUsuario(), 120);
        colUsuario.setMaxWidth(120);

        TableColumn<Compra, String> colTotal = UIFactory.crearColumna(idioma.get("history.col.total"), c -> formatearPrecioInteligente(c.getTotalCompra(), c.getMoneda()), 240);
        colTotal.setMaxWidth(280);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tablaDatos.getColumns().addAll(List.of(colId, colFecha, colDetalles, colOrigen, colUsuario, colTotal));
    }

    /**
     * Muestra un pop-up con los detalles de una compra específica, incluyendo una tabla con los productos comprados, sus cantidades, costos unitarios y subtotales. Este método es llamado desde el botón de cada fila en la tabla principal, y utiliza un nuevo Stage para mostrar la información detallada de manera clara y organizada, permitiendo al usuario revisar el contenido de cada compra sin salir de la vista principal del historial
     */
    private void mostrarPopUpDetalles(Compra compra) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        root.setMinWidth(700);
        root.setMinHeight(400);

        Label lblTitulo = new Label(idioma.get("history.purchase.popup.title", compra.getIdcompra()));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");

        TableView<DetalleCompra> tableDetalles = new TableView<>();
        tableDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        TableColumn<DetalleCompra, String> colProd = UIFactory.crearColumna(idioma.get("history.popup.col.product"), d -> d.getProducto().getNombreProducto(), 0);
        TableColumn<DetalleCompra, String> colCant = UIFactory.crearColumna(idioma.get("history.popup.col.qty"), d -> String.valueOf(d.getCantidad()), 0);
        colCant.setStyle("-fx-alignment: CENTER;");

        TableColumn<DetalleCompra, String> colCosto = UIFactory.crearColumna(idioma.get("history.popup.col.cost"), d -> formatearPrecioInteligente(d.getCostoUnitario(), compra.getMoneda()), 180);
        colCosto.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<DetalleCompra, String> colSub = UIFactory.crearColumna(idioma.get("history.popup.col.subtotal"), d -> formatearPrecioInteligente(d.getSubtotal(), compra.getMoneda()), 180);
        colSub.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tableDetalles.getColumns().addAll(List.of(colProd, colCant, colCosto, colSub));

        if (compra.getDetalles() != null) {
            tableDetalles.setItems(FXCollections.observableArrayList(compra.getDetalles()));
        }
        VBox.setVgrow(tableDetalles, Priority.ALWAYS);

        Button btnCerrar = UIFactory.crearBotonSecundario(idioma.get("history.btn.close"));
        btnCerrar.setOnAction(e -> dialog.close());
        HBox footer = new HBox(btnCerrar);
        footer.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(lblTitulo, new Separator(), tableDetalles, footer);
        dialogService.mostrarDialogoModal(dialog, root, stage);
    }

    /**
     * Formatea el precio de manera inteligente mostrando el valor original con su moneda, y si la moneda es diferente a la preferida del usuario, también muestra una conversión aproximada al formato preferido. Esto permite que el usuario tenga una referencia clara del precio en su moneda habitual, incluso si los datos originales están en otra moneda
     * @param precio
     * @param monedaItem
     * @return
     */
    private String formatearPrecioInteligente(double precio, String monedaItem) {
        if (monedaItem == null) monedaItem = "MXN";
        String monedaPref = monedaService.getMonedaPorDefecto();
        String textoOriginal = String.format("$%.2f %s", precio, monedaItem);

        if (monedaItem.equalsIgnoreCase(monedaPref)) return textoOriginal;
        
        try {
            double tipoCambio = monedaService.convertirAMxn(1.0, "USD");
            if (tipoCambio == 0) tipoCambio = 20.0;
            double precioConvertido = monedaPref.equalsIgnoreCase("MXN") ? (precio * tipoCambio) : (precio / tipoCambio);
            return String.format("%s (≈ $%.2f %s)", textoOriginal, precioConvertido, monedaPref);
        } catch (Exception e) {
            return textoOriginal;
        }
    }
}