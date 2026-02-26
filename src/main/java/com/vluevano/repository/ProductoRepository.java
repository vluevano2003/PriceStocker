package com.vluevano.repository;

import com.vluevano.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {

    @Query("SELECT DISTINCT p FROM Producto p " +
            "LEFT JOIN FETCH p.categorias " +
            "WHERE p.activo = true AND (" +
            "LOWER(p.nombreProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "LOWER(p.fichaProducto) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "LOWER(p.alternoProducto) LIKE LOWER(CONCAT('%', :filtro, '%')))")
    List<Producto> buscarPorFiltro(@Param("filtro") String filtro);
    
    @Query("SELECT p FROM Producto p WHERE p.activo = true")
    List<Producto> findAllActivos();
}