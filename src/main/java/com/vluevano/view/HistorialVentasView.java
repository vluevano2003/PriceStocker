package com.vluevano.view;

import com.vluevano.model.DetalleVenta;
import com.vluevano.model.Venta;
import com.vluevano.service.VentaService;
import com.vluevano.service.MonedaService;
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

import com.vluevano.service.PdfService;

@Component
public class HistorialVentasView extends BaseHistorialView<Venta> {

    @Autowired private VentaService ventaService;
    @Autowired private PdfService pdfService;
    @Autowired private MonedaService monedaService;

    @Override
    protected String getTituloVentana() {
        return idioma.get("history.sales.window.title");
    }

    @Override
    protected String getTituloHeader() {
        return idioma.get("history.sales.header.title");
    }

    @Override
    protected String getSubtituloHeader() {
        return idioma.get("history.sales.header.subtitle");
    }

    @Override
    protected String getPromptBusqueda() {
        return idioma.get("history.sales.search.prompt");
    }

    @Override
    protected String getTxtTablaVacia() {
        return idioma.get("history.sales.table.empty");
    }

    @Override
    protected String getTxtExportarError() {
        return idioma.get("history.msg.report_error.content");
    }

    @Override
    protected String getTxtExportarVacio() {
        return idioma.get("history.sales.msg.empty_table.content");
    }

    @Override
    protected String getNombreArchivoPdf() {
        return "Ventas";
    }

    /**
     * Obtiene las ventas dentro del rango de fechas y filtra por búsqueda en ID, cliente o usuario
     */
    @Override
    protected List<Venta> obtenerDatos(LocalDate inicio, LocalDate fin, String busqueda) {
        List<Venta> ventas = ventaService.obtenerVentasPorRango(inicio, fin);
        
        if (!busqueda.isEmpty()) {
            return ventas.stream().filter(v -> {
                boolean matchId = String.valueOf(v.getIdventa()).contains(busqueda);
                boolean matchCliente = v.getCliente() != null && v.getCliente().getNombreCliente().toLowerCase().contains(busqueda);
                boolean matchUsuario = v.getUsuario() != null && v.getUsuario().getNombreUsuario().toLowerCase().contains(busqueda);
                return matchId || matchCliente || matchUsuario;
            }).collect(Collectors.toList());
        }
        return ventas;
    }

    /**
     * Genera un reporte PDF de las ventas utilizando el servicio PdfService
     */
    @Override
    protected void generarReportePdf(File file, List<Venta> datos, String rangoFechas) throws Exception {
        pdfService.generarReporteVentas(file, datos, rangoFechas);
    }

    /**
     * Configura las columnas de la tabla para mostrar ID, fecha, detalles, cliente, usuario y total de cada venta
     */
    @Override
    protected void configurarColumnasTabla() {
        TableColumn<Venta, String> colId = UIFactory.crearColumna(idioma.get("history.sales.col.folio"), v -> String.valueOf(v.getIdventa()), 60);
        colId.setMaxWidth(60);

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        TableColumn<Venta, String> colFecha = UIFactory.crearColumna(idioma.get("history.col.date"), v -> v.getFechaVenta().format(fmt), 140);
        colFecha.setMaxWidth(140);

        TableColumn<Venta, Void> colDetalles = new TableColumn<>(idioma.get("history.col.content"));
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
                    Venta v = getTableView().getItems().get(getIndex());
                    int items = (v.getDetalles() != null) ? v.getDetalles().size() : 0;
                    btnVer.setText(idioma.get("history.sales.btn.view_products", items));
                    setGraphic(btnVer);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        TableColumn<Venta, String> colCliente = UIFactory.crearColumna(idioma.get("history.sales.col.client"), 
            v -> v.getCliente() != null ? v.getCliente().getNombreCliente() : idioma.get("history.sales.txt.public"), 200);

        TableColumn<Venta, String> colUsuario = UIFactory.crearColumna(idioma.get("history.col.user"), v -> v.getUsuario().getNombreUsuario(), 120);
        colUsuario.setMaxWidth(120);

        TableColumn<Venta, String> colTotal = UIFactory.crearColumna(idioma.get("history.col.total"), v -> formatearPrecioInteligente(v.getTotalVenta(), v.getMoneda()), 240);
        colTotal.setMaxWidth(280);
        colTotal.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tablaDatos.getColumns().addAll(List.of(colId, colFecha, colDetalles, colCliente, colUsuario, colTotal));
    }

    /**
     * Muestra un pop-up con los detalles de los productos vendidos en la venta seleccionada, incluyendo nombre, cantidad, precio unitario y subtotal
     * @param venta
     */
    private void mostrarPopUpDetalles(Venta venta) {
        Stage dialog = new Stage();
        UIFactory.configurarStageModal(dialog, stage);

        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: white; -fx-background-radius: 8;");
        root.setMinWidth(700);
        root.setMinHeight(400);

        Label lblTitulo = new Label(idioma.get("history.sales.popup.title", venta.getIdventa()));
        lblTitulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: " + AppTheme.COLOR_PRIMARY + ";");

        TableView<DetalleVenta> tableDetalles = new TableView<>();
        tableDetalles.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        
        TableColumn<DetalleVenta, String> colProd = UIFactory.crearColumna(idioma.get("history.popup.col.product"), d -> d.getProducto().getNombreProducto(), 0);
        
        TableColumn<DetalleVenta, String> colCant = UIFactory.crearColumna(idioma.get("history.popup.col.qty"), d -> String.valueOf(d.getCantidad()), 0);
        colCant.setStyle("-fx-alignment: CENTER;");

        TableColumn<DetalleVenta, String> colPrecio = UIFactory.crearColumna(idioma.get("history.sales.popup.col.price"), d -> formatearPrecioInteligente(d.getPrecioUnitario(), venta.getMoneda()), 180);
        colPrecio.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<DetalleVenta, String> colSub = UIFactory.crearColumna(idioma.get("history.popup.col.subtotal"), d -> formatearPrecioInteligente(d.getSubtotal(), venta.getMoneda()), 180);
        colSub.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold;");

        tableDetalles.getColumns().addAll(List.of(colProd, colCant, colPrecio, colSub));

        if (venta.getDetalles() != null) {
            tableDetalles.setItems(FXCollections.observableArrayList(venta.getDetalles()));
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
     * Formatea el precio mostrando la moneda original y, si es diferente a la moneda preferida del usuario, también muestra el equivalente en la moneda preferida utilizando el servicio MonedaService para obtener el tipo de cambio
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