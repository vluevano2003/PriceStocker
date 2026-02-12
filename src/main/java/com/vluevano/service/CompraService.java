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
    @Autowired
    private ProductoProveedorRepository productoProveedorRepository;
    @Autowired
    private ProductoFabricanteRepository productoFabricanteRepository;

    /**
     * Obtiene el costo de compra para un producto dado, considerando el proveedor o
     * fabricante.
     * 
     * @param p
     * @param prov
     * @param fab
     * @return
     */
    public Double obtenerCostoCompra(Producto p, Proveedor prov, Fabricante fab) {
        if (p == null)
            return 0.0;

        if (prov != null) {
            ProductoProveedor pp = productoProveedorRepository.findCostoEspecifico(p.getIdProducto(),
                    prov.getIdProveedor());
            if (pp != null) {
                return pp.getCosto();
            }
        }

        if (fab != null) {
            ProductoFabricante pf = productoFabricanteRepository.findCostoEspecifico(p.getIdProducto(),
                    fab.getIdFabricante());
            if (pf != null) {
                return pf.getCosto();
            }
        }

        return 0.00;
    }

    /**
     * Registra una compra, actualiza stock y aprende costos históricos para
     * proveedor/fabricante.
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

            if (compra.getProveedor() == null && compra.getFabricante() == null) {
                return "Error: Debes seleccionar un Proveedor o un Fabricante.";
            }

            compra.setUsuario(u);
            compra.setFechaCompra(LocalDateTime.now());

            Compra compraGuardada = compraRepository.save(compra);

            for (DetalleCompra det : detalles) {
                det.setCompra(compraGuardada);
                detalleCompraRepository.save(det);

                Producto p = det.getProducto();
                p.setExistenciaProducto(p.getExistenciaProducto() + det.getCantidad());
                productoRepository.save(p);

                if (compra.getProveedor() != null) {
                    Proveedor prov = compra.getProveedor();
                    ProductoProveedor pp = productoProveedorRepository.findCostoEspecifico(p.getIdProducto(),
                            prov.getIdProveedor());

                    if (pp == null) {
                        pp = new ProductoProveedor();
                        pp.setId(new ProductoProveedorId(p.getIdProducto(), prov.getIdProveedor()));
                        pp.setProducto(p);
                        pp.setProveedor(prov);
                        pp.setMoneda("MXN");
                    }
                    pp.setCosto(det.getCostoUnitario());
                    productoProveedorRepository.save(pp);
                }

                if (compra.getFabricante() != null) {
                    Fabricante fab = compra.getFabricante();
                    ProductoFabricante pf = productoFabricanteRepository.findCostoEspecifico(p.getIdProducto(),
                            fab.getIdFabricante());

                    if (pf == null) {
                        pf = new ProductoFabricante();
                        pf.setId(new ProductoFabricanteId(p.getIdProducto(), fab.getIdFabricante()));
                        pf.setProducto(p);
                        pf.setFabricante(fab);
                        pf.setMoneda("MXN");
                    }
                    pf.setCosto(det.getCostoUnitario());
                    productoFabricanteRepository.save(pf);
                }
            }
            return "Compra registrada exitosamente. ID: " + compraGuardada.getIdcompra();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error en compra: " + e.getMessage());
        }
    }
}