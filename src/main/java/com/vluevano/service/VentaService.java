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
    @Autowired
    private ProductoClienteRepository productoClienteRepository;

    /**
     * Obtiene el precio de venta para un producto dado un cliente, siguiendo la
     * lógica de prioridad:
     * 
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
     * Registra una venta, actualiza stock y guarda el precio específico para el
     * cliente.
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

                if (detalleVentaRepository != null) {
                    detalleVentaRepository.save(det);
                }

                Producto p = det.getProducto();

                if (p.getExistenciaProducto() < det.getCantidad()) {
                    throw new RuntimeException("Stock insuficiente para: " + p.getNombreProducto());
                }
                p.setExistenciaProducto(p.getExistenciaProducto() - det.getCantidad());
                productoRepository.save(p);

                if (venta.getCliente() != null) {
                    Cliente cliente = venta.getCliente();

                    ProductoCliente pc = productoClienteRepository.findPrecioEspecifico(p.getIdProducto(),
                            cliente.getIdCliente());

                    if (pc == null) {
                        pc = new ProductoCliente();
                        pc.setProducto(p);
                        pc.setCliente(cliente);
                        pc.setMoneda("MXN");
                    }

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
}