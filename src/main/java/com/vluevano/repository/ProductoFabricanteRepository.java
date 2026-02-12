package com.vluevano.repository;

import com.vluevano.model.ProductoFabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoFabricanteRepository extends JpaRepository<ProductoFabricante, Object> {
    @Query("SELECT pf FROM ProductoFabricante pf WHERE pf.id.idProducto = :idProducto AND pf.id.idFabricante = :idFabricante")
    ProductoFabricante findCostoEspecifico(Integer idProducto, Integer idFabricante);
}