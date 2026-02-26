package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;
    @Autowired
    private DetalleVentaRepository detalleVentaRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private ProductoClienteRepository productoClienteRepository;
    @Autowired
    private GestorIdioma idioma;

    /**
     * Obtiene el precio de venta considerando si el cliente tiene un precio específico para el producto
     * @param p
     * @param c
     * @return
     */
    public Double obtenerPrecioVenta(Producto p, Cliente c) {
        if (c != null && p != null) {
            ProductoCliente pc = productoClienteRepository.findPrecioEspecifico(p.getIdProducto(), c.getIdCliente());
            if (pc != null && pc.getCosto() != null) {
                return pc.getCosto();
            }
        }
        if (p != null && p.getPrecioProducto() != null) {
            return p.getPrecioProducto();
        }
        return 0.0;
    }

    /**
     * Obtiene la moneda de venta considerando si el cliente tiene una moneda específica para el producto
     * @param p
     * @param c
     * @return
     */
    public String obtenerMonedaVenta(Producto p, Cliente c) {
        if (p == null) return "MXN";
        if (c != null) {
            ProductoCliente pc = productoClienteRepository.findPrecioEspecifico(p.getIdProducto(), c.getIdCliente());
            if (pc != null && pc.getMoneda() != null) return pc.getMoneda();
        }
        return p.getMonedaProducto() != null ? p.getMonedaProducto() : "MXN";
    }

    /**
     * Registra una venta con sus detalles, actualiza el stock del producto y guarda precios específicos para el cliente si es necesario
     * @param venta
     * @param detalles
     * @param nombreUsuario
     * @return
     */
    @Transactional
    public String registrarVenta(Venta venta, List<DetalleVenta> detalles, String nombreUsuario) {
        try {
            Usuario u = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
            if (u == null) return idioma.get("srv.sale.error.user_not_found");

            venta.setUsuario(u);
            venta.setFechaVenta(LocalDateTime.now());

            Venta ventaGuardada = ventaRepository.save(venta);

            for (DetalleVenta det : detalles) {
                det.setVenta(ventaGuardada);

                if (detalleVentaRepository != null) {
                    detalleVentaRepository.save(det);
                }

                Producto p = det.getProducto();

                if (p.getExistenciaProducto() < det.getCantidad()) {
                    throw new RuntimeException(idioma.get("srv.sale.error.insufficient_stock", p.getNombreProducto()));
                }
                p.setExistenciaProducto(p.getExistenciaProducto() - det.getCantidad());
                productoRepository.save(p);

                String monedaDestino = venta.getMoneda() != null ? venta.getMoneda() : "MXN";

                if (venta.getCliente() != null) {
                    Cliente cliente = venta.getCliente();

                    ProductoCliente pc = productoClienteRepository.findPrecioEspecifico(p.getIdProducto(), cliente.getIdCliente());

                    if (pc == null) {
                        pc = new ProductoCliente();
                        pc.setProducto(p);
                        pc.setCliente(cliente);
                    }
                    
                    pc.setMoneda(monedaDestino);
                    pc.setCosto(det.getPrecioUnitario());
                    productoClienteRepository.save(pc);
                }
            }

            return idioma.get("srv.sale.msg.success", ventaGuardada.getIdventa());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(idioma.get("srv.sale.error.general") + " " + e.getMessage());
        }
    }

    /**
     * Obtiene las ventas realizadas dentro de un rango de fechas específico
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    public List<Venta> obtenerVentasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return ventaRepository.findByFechaVentaBetween(inicio, fin);
    }
}