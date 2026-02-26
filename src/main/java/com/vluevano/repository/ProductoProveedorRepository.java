package com.vluevano.repository;

import com.vluevano.model.ProductoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoProveedorRepository extends JpaRepository<ProductoProveedor, Object> {
    @Query("SELECT pp FROM ProductoProveedor pp WHERE pp.id.idProducto = :idProducto AND pp.id.idProveedor = :idProveedor")
    ProductoProveedor findCostoEspecifico(Integer idProducto, Integer idProveedor);
}