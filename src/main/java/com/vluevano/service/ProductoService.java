package com.vluevano.service;

import com.vluevano.model.*;
import com.vluevano.repository.*;
import com.vluevano.util.GestorIdioma;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    @Autowired
    private GestorIdioma idioma;

    /**
     * Consulta todos los productos disponibles en la base de datos. Retorna una
     * lista de productos, o una lista vacía si no hay productos registrados
     * 
     * @return
     */
    public List<Producto> consultarProductos() {
        return productoRepository.findAllActivos();
    }

    /**
     * Busca productos que coincidan con el filtro proporcionado. El filtro se
     * aplica a campos como nombre, descripción, categoría, proveedor, cliente,
     * fabricante o empresa. Retorna una lista de productos que coincidan con el
     * filtro, o una lista vacía si no se encuentran coincidencias
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
     * Consulta todas las categorías disponibles en la base de datos. Retorna una
     * lista de categorías, o una lista vacía si no hay categorías registradas
     * 
     * @return
     */
    public List<Categoria> obtenerCategorias() {
        return categoriaRepository.findAllActivos();
    }

    /**
     * Consulta todos los proveedores disponibles en la base de datos. Retorna una
     * lista de proveedores, o una lista vacía si no hay proveedores registrados
     * 
     * @return
     */
    public List<Proveedor> obtenerProveedores() {
        return proveedorRepository.findAllActivos();
    }

    /**
     * Consulta todos los clientes disponibles en la base de datos. Retorna una
     * lista de clientes, o una lista vacía si no hay clientes registrados
     * 
     * @return
     */
    public List<Cliente> obtenerClientes() {
        return clienteRepository.findAllActivos();
    }

    /**
     * Consulta todos los fabricantes disponibles en la base de datos. Retorna una
     * lista de fabricantes, o una lista vacía si no hay fabricantes registrados
     * 
     * @return
     */
    public List<Fabricante> obtenerFabricantes() {
        return fabricanteRepository.findAllActivos();
    }

    /**
     * Consulta todas las empresas disponibles en la base de datos. Retorna una
     * lista de empresas, o una lista vacía si no hay empresas registradas
     * 
     * @return
     */
    public List<Empresa> obtenerEmpresas() {
        return empresaRepository.findAllActivos();
    }

    /**
     * Consulta todos los servicios disponibles en la base de datos. Retorna una
     * lista de servicios, o una lista vacía si no hay servicios registrados
     * 
     * @return
     */
    public List<Servicio> obtenerServicios() {
        return servicioRepository.findAllActivos();
    }

    /**
     * Consulta un producto por su ID y carga todas sus asociaciones para evitar
     * problemas de LazyInitializationException. Retorna el producto con sus
     * asociaciones cargadas, o null si no se encuentra el producto
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
     * Guarda un producto en la base de datos. Si el producto tiene categorías,
     * proveedores, clientes, fabricantes o empresas asociadas, se aseguran de que
     * estén correctamente referenciados antes de guardar. Retorna un mensaje
     * indicando el resultado de la operación, ya sea éxito o error con detalles del
     * mismo
     * 
     * @param producto
     * @return
     */
    @Transactional
    public String guardarProducto(Producto producto) {
        if (producto.getNombreProducto() == null || producto.getNombreProducto().trim().isEmpty()) {
            return idioma.get("srv.product.val.name");
        }

        try {
            Producto productoAGuardar;

            if (producto.getIdProducto() != null) {
                productoAGuardar = productoRepository.findById(producto.getIdProducto())
                        .orElseThrow(() -> new RuntimeException("Producto no encontrado"));

                productoAGuardar.setNombreProducto(producto.getNombreProducto());
                productoAGuardar.setFichaProducto(producto.getFichaProducto());
                productoAGuardar.setAlternoProducto(producto.getAlternoProducto());
                productoAGuardar.setExistenciaProducto(producto.getExistenciaProducto());
                productoAGuardar.setPrecioProducto(producto.getPrecioProducto());
                productoAGuardar.setMonedaProducto(producto.getMonedaProducto());

                if (producto.getCategorias() != null) {
                    List<Categoria> catsVivas = new ArrayList<>();
                    for (Categoria c : producto.getCategorias()) {
                        catsVivas.add(c.getIdCategoria() != null
                                ? categoriaRepository.getReferenceById(c.getIdCategoria())
                                : categoriaRepository.save(c));
                    }
                    productoAGuardar.getCategorias().clear();
                    productoAGuardar.getCategorias().addAll(catsVivas);
                }

                actualizarRelaciones(producto, productoAGuardar);

            } else {
                productoAGuardar = producto;

                if (producto.getProductoProveedores() != null) {
                    producto.getProductoProveedores().forEach(pp -> {
                        pp.setProducto(producto);
                        pp.setProveedor(proveedorRepository.getReferenceById(pp.getProveedor().getIdProveedor()));
                        pp.getId().setIdProveedor(pp.getProveedor().getIdProveedor());
                    });
                }
                if (producto.getProductoClientes() != null) {
                    producto.getProductoClientes().forEach(pc -> {
                        pc.setProducto(producto);
                        pc.setCliente(clienteRepository.getReferenceById(pc.getCliente().getIdCliente()));
                        pc.getId().setIdCliente(pc.getCliente().getIdCliente());
                    });
                }
                if (producto.getProductoFabricantes() != null) {
                    producto.getProductoFabricantes().forEach(pf -> {
                        pf.setProducto(producto);
                        pf.setFabricante(fabricanteRepository.getReferenceById(pf.getFabricante().getIdFabricante()));
                        pf.getId().setIdFabricante(pf.getFabricante().getIdFabricante());
                    });
                }
                if (producto.getProductoEmpresas() != null) {
                    producto.getProductoEmpresas().forEach(pe -> {
                        pe.setProducto(producto);
                        pe.setEmpresa(empresaRepository.getReferenceById(pe.getEmpresa().getIdEmpresa()));
                        pe.getId().setIdEmpresa(pe.getEmpresa().getIdEmpresa());
                    });
                }
            }

            productoRepository.save(productoAGuardar);
            return idioma.get("srv.product.msg.success");

        } catch (Exception e) {
            e.printStackTrace();
            return idioma.get("srv.product.msg.error") + " " + e.getMessage();
        }
    }

    /**
     * Actualiza las relaciones de un producto con proveedores, clientes,
     * fabricantes y empresas. Este método se encarga de limpiar las relaciones
     * existentes y establecer las nuevas
     * basándose en el producto proporcionado. Se asegura de que las referencias a
     * entidades relacionadas sean válidas y estén actualizadas antes de guardar el
     * producto
     * 
     * @param producto
     * @param productoAGuardar
     */
    private void actualizarRelaciones(Producto producto, Producto productoAGuardar) {
        productoAGuardar.getProductoProveedores().clear();
        if (producto.getProductoProveedores() != null) {
            producto.getProductoProveedores().forEach(pp -> {
                pp.setProducto(productoAGuardar);
                pp.setProveedor(proveedorRepository.getReferenceById(pp.getProveedor().getIdProveedor()));
                pp.getId().setIdProducto(productoAGuardar.getIdProducto());
                pp.getId().setIdProveedor(pp.getProveedor().getIdProveedor());
                productoAGuardar.getProductoProveedores().add(pp);
            });
        }

        productoAGuardar.getProductoClientes().clear();
        if (producto.getProductoClientes() != null) {
            producto.getProductoClientes().forEach(pc -> {
                pc.setProducto(productoAGuardar);
                pc.setCliente(clienteRepository.getReferenceById(pc.getCliente().getIdCliente()));
                pc.getId().setIdProducto(productoAGuardar.getIdProducto());
                pc.getId().setIdCliente(pc.getCliente().getIdCliente());
                productoAGuardar.getProductoClientes().add(pc);
            });
        }

        productoAGuardar.getProductoFabricantes().clear();
        if (producto.getProductoFabricantes() != null) {
            producto.getProductoFabricantes().forEach(pf -> {
                pf.setProducto(productoAGuardar);
                pf.setFabricante(fabricanteRepository.getReferenceById(pf.getFabricante().getIdFabricante()));
                pf.getId().setIdProducto(productoAGuardar.getIdProducto());
                pf.getId().setIdFabricante(pf.getFabricante().getIdFabricante());
                productoAGuardar.getProductoFabricantes().add(pf);
            });
        }

        productoAGuardar.getProductoEmpresas().clear();
        if (producto.getProductoEmpresas() != null) {
            producto.getProductoEmpresas().forEach(pe -> {
                pe.setProducto(productoAGuardar);
                pe.setEmpresa(empresaRepository.getReferenceById(pe.getEmpresa().getIdEmpresa()));
                pe.getId().setIdProducto(productoAGuardar.getIdProducto());
                pe.getId().setIdEmpresa(pe.getEmpresa().getIdEmpresa());
                productoAGuardar.getProductoEmpresas().add(pe);
            });
        }
    }

    /**
     * Elimina un producto de la base de datos. Retorna true si la eliminación fue
     * exitosa, o false si ocurrió un error
     * 
     * @param producto
     * @return
     */
    @Transactional
    public boolean eliminarProducto(Producto producto) {
        try {
            producto.setActivo(false);
            productoRepository.save(producto);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}