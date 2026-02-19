package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
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

    /**
     * Obtiene el precio de venta registrado históricamente para un producto y un cliente
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
     * Obtiene la moneda con la que se hizo la venta, dando prioridad a la moneda específica del cliente-producto, luego a la moneda del producto y finalmente a "MXN" si no hay información
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
     * Registra una venta con sus detalles, actualizando el stock de los productos y guardando la moneda utilizada. Si el cliente tiene un precio específico, se guarda esa información para futuras consultas
     * @param venta
     * @param detalles
     * @param nombreUsuario
     * @return
     */
    @Transactional
    public String registrarVenta(Venta venta, List<DetalleVenta> detalles, String nombreUsuario) {
        try {
            Usuario u = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
            if (u == null) return "Error: Usuario no encontrado.";

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
                    throw new RuntimeException("Stock insuficiente para: " + p.getNombreProducto());
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

            return "Venta registrada exitosamente. Folio: " + ventaGuardada.getIdventa();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en venta: " + e.getMessage());
        }
    }

    /**
     * Obtiene todas las ventas realizadas dentro de un rango de fechas específico, incluyendo la información del cliente, usuario y detalles de cada venta
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