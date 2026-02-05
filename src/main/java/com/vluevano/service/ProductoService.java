package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import org.hibernate.Hibernate; // Importante
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    @Autowired
    private ProductoRepository productoRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;
    @Autowired
    private ProveedorRepository proveedorRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private FabricanteRepository fabricanteRepository;
    @Autowired
    private EmpresaRepository empresaRepository;
    @Autowired
    private ServicioRepository servicioRepository;

    /**
     * Consulta todos los productos sin filtros
     * 
     * @return
     */
    public List<Producto> consultarProductos() {
        return productoRepository.findAll();
    }

    /**
     * Busca productos por un filtro de texto (nombre o descripción)
     * 
     * @param filtro
     * @return
     */
    public List<Producto> buscarProductos(String filtro) {
        if (filtro == null || filtro.trim().isEmpty())
            return consultarProductos();
        return productoRepository.buscarPorFiltro(filtro);
    }

    /**
     * Consulta todas las categorías disponibles (para llenar el ComboBox)
     * 
     * @return
     */
    public List<Categoria> obtenerCategorias() {
        return categoriaRepository.findAll();
    }

    /**
     * Consulta todos los proveedores disponibles (para llenar el ComboBox)
     * 
     * @return
     */
    public List<Proveedor> obtenerProveedores() {
        return proveedorRepository.findAll();
    }

    /**
     * Consulta todos los clientes disponibles (para llenar el ComboBox)
     * 
     * @return
     */
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAll();
    }

    /**
     * Consulta todos los fabricantes disponibles (para llenar el ComboBox)
     * 
     * @return
     */
    public List<Fabricante> obtenerFabricantes() {
        return fabricanteRepository.findAll();
    }

    /**
     * Consulta todas las empresas disponibles (para llenar el ComboBox)
     * 
     * @return
     */
    public List<Empresa> obtenerEmpresas() {
        return empresaRepository.findAll();
    }

    /**
     * Consulta todos los servicios disponibles (para llenar el ComboBox)
     * 
     * @return
     */
    public List<Servicio> obtenerServicios() {
        return servicioRepository.findAll();
    }

    /**
     * Obtiene un producto por su ID, cargando todas sus relaciones Lazy
     * 
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public Producto obtenerProductoCompleto(Integer id) {
        Optional<Producto> opt = productoRepository.findById(id);
        if (opt.isPresent()) {
            Producto p = opt.get();
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

    /**
     * Guarda o actualiza un producto, manejando las relaciones y validaciones
     * necesarias
     * 
     * @param producto
     * @return
     */
    @Transactional
    public String guardarProducto(Producto producto) {
        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
            return "El nombre del producto es obligatorio.";
        }

        try {
            if (producto.getCategorias() != null && !producto.getCategorias().isEmpty()) {
                List<Categoria> categoriasProcesadas = new ArrayList<>();

                for (Categoria cat : producto.getCategorias()) {
                    if (cat.getIdCategoria() != null) {
                        categoriasProcesadas.add(categoriaRepository.getReferenceById(cat.getIdCategoria()));
                    } else {
                        Categoria nuevaGuardada = categoriaRepository.save(cat);
                        categoriasProcesadas.add(nuevaGuardada);
                    }
                }
                producto.setCategorias(categoriasProcesadas);
            }

            if (producto.getServicios() != null && !producto.getServicios().isEmpty()) {
                List<Servicio> serviciosVivos = new ArrayList<>();
                for (Servicio serv : producto.getServicios()) {
                    serviciosVivos.add(servicioRepository.getReferenceById(serv.getIdServicio()));
                }
                producto.setServicios(serviciosVivos);
            }

            if (producto.getProductoProveedores() != null) {
                producto.getProductoProveedores().forEach(pp -> {
                    pp.setProducto(producto);
                    if (pp.getProveedor() != null) {
                        pp.setProveedor(proveedorRepository.getReferenceById(pp.getProveedor().getIdProveedor()));
                    }
                });
            }

            if (producto.getProductoClientes() != null) {
                producto.getProductoClientes().forEach(pc -> {
                    pc.setProducto(producto);
                    if (pc.getCliente() != null) {
                        pc.setCliente(clienteRepository.getReferenceById(pc.getCliente().getIdCliente()));
                    }
                });
            }

            if (producto.getProductoFabricantes() != null) {
                producto.getProductoFabricantes().forEach(pf -> {
                    pf.setProducto(producto);
                    if (pf.getFabricante() != null) {
                        pf.setFabricante(fabricanteRepository.getReferenceById(pf.getFabricante().getIdFabricante()));
                    }
                });
            }

            if (producto.getProductoEmpresas() != null) {
                producto.getProductoEmpresas().forEach(pe -> {
                    pe.setProducto(producto);
                    if (pe.getEmpresa() != null) {
                        pe.setEmpresa(empresaRepository.getReferenceById(pe.getEmpresa().getIdEmpresa()));
                    }
                });
            }
            productoRepository.save(producto);

            return "Producto guardado exitosamente.";

        } catch (Exception e) {
            e.printStackTrace();
            Throwable rootCause = e;
            while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                rootCause = rootCause.getCause();
            }
            return "Error al guardar: " + rootCause.getMessage();
        }
    }

    /**
     * Elimina un producto
     * 
     * @param producto
     * @return
     */
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