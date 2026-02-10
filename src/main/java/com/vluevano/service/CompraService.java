package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CompraService {

    @Autowired
    private CompraRepository compraRepository;
    @Autowired
    private DetalleCompraRepository detalleCompraRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private ProductoRepository productoRepository;

    /**
     * Registra una compra, guarda los detalles y actualiza el stock de productos.
     * 
     * @param compra
     * @param detalles
     * @param nombreUsuario
     * @return
     */
    @Transactional
    public String registrarCompra(Compra compra, List<DetalleCompra> detalles, String nombreUsuario) {
        try {
            Usuario u = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
            if (u == null)
                return "Error: Usuario no encontrado.";

            compra.setUsuario(u);
            compra.setFechaCompra(LocalDateTime.now());

            if (compra.getProveedor() == null && compra.getFabricante() == null) {
                return "Error: Debes seleccionar un Proveedor o un Fabricante.";
            }

            Compra compraGuardada = compraRepository.save(compra);

            for (DetalleCompra det : detalles) {
                det.setCompra(compraGuardada);
                detalleCompraRepository.save(det);

                Producto p = det.getProducto();
                p.setExistenciaProducto(p.getExistenciaProducto() + det.getCantidad());
                productoRepository.save(p);
            }

            return "Compra registrada exitosamente. ID: " + compraGuardada.getIdcompra();

        } catch (Exception e) {
            throw new RuntimeException("Error en compra: " + e.getMessage());
        }
    }
}