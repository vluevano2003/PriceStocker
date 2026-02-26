package com.vluevano.repository;

import com.vluevano.model.Fabricante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FabricanteRepository extends JpaRepository<Fabricante, Integer> {

    @Query("SELECT f FROM Fabricante f WHERE f.activo = true AND (" +
           "LOWER(f.nombreFabricante) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(f.rfcFabricante) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(f.municipio) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "f.telefonoFabricante LIKE %:filtro%)")
    List<Fabricante> buscarPorFiltro(@Param("filtro") String filtro);
    
    @Query("SELECT f FROM Fabricante f WHERE f.activo = true")
    List<Fabricante> findAllActivos();
}