package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    /**
     * Registra una venta, guarda los detalles y actualiza el stock de productos.
     * 
     * @param venta
     * @param detalles
     * @param nombreUsuario
     * @return
     */
    @Transactional
    public String registrarVenta(Venta venta, List<DetalleVenta> detalles, String nombreUsuario) {
        try {
            Usuario u = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
            if (u == null)
                return "Error: Usuario no encontrado.";

            venta.setUsuario(u);
            venta.setFechaVenta(LocalDateTime.now());

            Venta ventaGuardada = ventaRepository.save(venta);

            for (DetalleVenta det : detalles) {
                det.setVenta(ventaGuardada);
                detalleVentaRepository.save(det);

                Producto p = det.getProducto();
                if (p.getExistenciaProducto() < det.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + p.getNombreProducto());
                }
                p.setExistenciaProducto(p.getExistenciaProducto() - det.getCantidad());
                productoRepository.save(p);
            }

            return "Venta registrada exitosamente. Folio: " + ventaGuardada.getIdventa();
        } catch (Exception e) {
            throw new RuntimeException("Error en venta: " + e.getMessage());
        }
    }
}