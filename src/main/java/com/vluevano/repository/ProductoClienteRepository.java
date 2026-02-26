package com.vluevano.repository;

import com.vluevano.model.ProductoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoClienteRepository extends JpaRepository<ProductoCliente, Object> {
    @Query("SELECT pc FROM ProductoCliente pc WHERE pc.id.idProducto = :idProducto AND pc.id.idCliente = :idCliente")
    ProductoCliente findPrecioEspecifico(Integer idProducto, Integer idCliente);
}