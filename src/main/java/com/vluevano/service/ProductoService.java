package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import org.hibernate.Hibernate; // Importante
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired private ProductoRepository productoRepository;
    @Autowired private CategoriaRepository categoriaRepository;
    @Autowired private ProveedorRepository proveedorRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private FabricanteRepository fabricanteRepository;
    @Autowired private EmpresaRepository empresaRepository;
    @Autowired private ServicioRepository servicioRepository;

    public List<Producto> consultarProductos() {
        return productoRepository.findAll();
    }

    public List<Producto> buscarProductos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) return consultarProductos();
        return productoRepository.buscarPorFiltro(filtro);
    }

    // Listas para ComboBoxes
    public List<Categoria> obtenerCategorias() { return categoriaRepository.findAll(); }
    public List<Proveedor> obtenerProveedores() { return proveedorRepository.findAll(); }
    public List<Cliente> obtenerClientes() { return clienteRepository.findAll(); }
    public List<Fabricante> obtenerFabricantes() { return fabricanteRepository.findAll(); }
    public List<Empresa> obtenerEmpresas() { return empresaRepository.findAll(); }
    public List<Servicio> obtenerServicios() { return servicioRepository.findAll(); }

    /**
     * Este método soluciona el LazyInitializationException.
     * Carga el producto y fuerza la inicialización de todas sus listas.
     */
    @Transactional(readOnly = true)
    public Producto obtenerProductoCompleto(Integer id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
            // Forzar la carga de las relaciones Lazy
            Hibernate.initialize(p.getCategorias());
            Hibernate.initialize(p.getProductoProveedores());
            Hibernate.initialize(p.getProductoClientes());
            Hibernate.initialize(p.getProductoFabricantes());
            Hibernate.initialize(p.getProductoEmpresas());
            Hibernate.initialize(p.getServicios());
            return p;
        }
        return null;
    }

    @Transactional
    public String guardarProducto(Producto producto) {
        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
            return "El nombre del producto es obligatorio.";
        }

        try {
            // Reconectar entidades detached (Categorias y Servicios)
            // Nota: Las relaciones complejas (ProductoProveedor, etc) se manejan por ID en sus clases, 
            // pero es buena práctica asegurarse de que las referencias base existan.
            
            // Guardar (CascadeType.ALL en Producto se encarga de las tablas intermedias)
            productoRepository.save(producto);
            return "Producto guardado exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al guardar: " + e.getMessage();
        }
    }

    @Transactional
    public boolean eliminarProducto(Producto producto) {
        try {
            productoRepository.delete(producto);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}