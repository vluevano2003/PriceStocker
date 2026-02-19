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
     * Obtiene el costo de compra de un producto considerando su relación con un proveedor o fabricante específico. Si se encuentra una relación con el proveedor, se devuelve ese costo; si no, se busca la relación con el fabricante. Si no se encuentra ninguna relación o el producto es nulo, se devuelve 0.00.
     * @param p
     * @param prov
     * @param fab
     * @return
     */
    public Double obtenerCostoCompra(Producto p, Proveedor prov, Fabricante fab) {
        if (p == null) return 0.0;
        if (prov != null) {
            ProductoProveedor pp = productoProveedorRepository.findCostoEspecifico(p.getIdProducto(), prov.getIdProveedor());
            if (pp != null) return pp.getCosto();
        }
        if (fab != null) {
            ProductoFabricante pf = productoFabricanteRepository.findCostoEspecifico(p.getIdProducto(), fab.getIdFabricante());
            if (pf != null) return pf.getCosto();
        }
        return 0.00;
    }

    /**
     * Obtiene la moneda de compra de un producto considerando su relación con un proveedor o fabricante específico. Si se encuentra una relación con el proveedor, se devuelve esa moneda; si no, se busca la relación con el fabricante. Si no se encuentra ninguna relación o el producto es nulo, se devuelve la moneda del producto o "MXN" por defecto.
     * @param p
     * @param prov
     * @param fab
     * @return
     */
    public String obtenerMonedaCompra(Producto p, Proveedor prov, Fabricante fab) {
        if (p == null) return "MXN";
        if (prov != null) {
            ProductoProveedor pp = productoProveedorRepository.findCostoEspecifico(p.getIdProducto(), prov.getIdProveedor());
            if (pp != null && pp.getMoneda() != null) return pp.getMoneda();
        }
        if (fab != null) {
            ProductoFabricante pf = productoFabricanteRepository.findCostoEspecifico(p.getIdProducto(), fab.getIdFabricante());
            if (pf != null && pf.getMoneda() != null) return pf.getMoneda();
        }
        return p.getMonedaProducto() != null ? p.getMonedaProducto() : "MXN";
    }

    /**
     * Registra una compra en el sistema, asociándola a un usuario específico y actualizando el inventario de productos. Para cada detalle de compra, se actualiza la existencia del producto y se guarda la relación de costo con el proveedor o fabricante correspondiente. Si la compra no tiene un proveedor o fabricante asociado, se devuelve un mensaje de error. En caso de éxito, se devuelve un mensaje con el ID de la compra registrada.
     * @param compra
     * @param detalles
     * @param nombreUsuario
     * @return
     */
    @Transactional
    public String registrarCompra(Compra compra, List<DetalleCompra> detalles, String nombreUsuario) {
        try {
            Usuario u = usuarioRepository.findByNombreUsuario(nombreUsuario).orElse(null);
            if (u == null) return "Error: Usuario no encontrado.";

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

                String monedaDestino = compra.getMoneda() != null ? compra.getMoneda() : "MXN";

                if (compra.getProveedor() != null) {
                    Proveedor prov = compra.getProveedor();
                    ProductoProveedor pp = productoProveedorRepository.findCostoEspecifico(p.getIdProducto(), prov.getIdProveedor());
                    if (pp == null) {
                        pp = new ProductoProveedor();
                        pp.setId(new ProductoProveedorId(p.getIdProducto(), prov.getIdProveedor()));
                        pp.setProducto(p);
                        pp.setProveedor(prov);
                    }
                    pp.setMoneda(monedaDestino);
                    pp.setCosto(det.getCostoUnitario());
                    productoProveedorRepository.save(pp);
                }

                if (compra.getFabricante() != null) {
                    Fabricante fab = compra.getFabricante();
                    ProductoFabricante pf = productoFabricanteRepository.findCostoEspecifico(p.getIdProducto(), fab.getIdFabricante());
                    if (pf == null) {
                        pf = new ProductoFabricante();
                        pf.setId(new ProductoFabricanteId(p.getIdProducto(), fab.getIdFabricante()));
                        pf.setProducto(p);
                        pf.setFabricante(fab);
                    }
                    pf.setMoneda(monedaDestino);
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

    /**
     * Obtiene una lista de compras realizadas dentro de un rango de fechas específico. El método convierte las fechas de inicio y fin a objetos LocalDateTime para incluir todo el día en la consulta, y luego utiliza el repositorio de compras para recuperar las compras que se encuentran dentro de ese rango.
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    public List<Compra> obtenerComprasPorRango(LocalDate fechaInicio, LocalDate fechaFin) {
        LocalDateTime inicio = fechaInicio.atStartOfDay();
        LocalDateTime fin = fechaFin.atTime(23, 59, 59);
        return compraRepository.findByFechaCompraBetween(inicio, fin);
    }
}