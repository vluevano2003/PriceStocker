package com.vluevano.repository;

import com.vluevano.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Integer> {
    
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true AND (" +
           "LOWER(p.nombreProv) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(p.rfcProveedor) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(p.municipio) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "p.telefonoProv LIKE %:filtro%)")
    List<Proveedor> buscarPorFiltro(@Param("filtro") String filtro);
    
    @Query("SELECT p FROM Proveedor p WHERE p.activo = true")
    List<Proveedor> findAllActivos();
}