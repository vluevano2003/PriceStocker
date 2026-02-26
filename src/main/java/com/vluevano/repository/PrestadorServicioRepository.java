package com.vluevano.repository;

import com.vluevano.model.PrestadorServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrestadorServicioRepository extends JpaRepository<PrestadorServicio, Integer> {

    @Query("SELECT DISTINCT p FROM PrestadorServicio p " +
            "LEFT JOIN FETCH p.servicios " +
            "WHERE p.activo = true AND (" +
            "LOWER(p.nombrePrestador) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "LOWER(p.rfcPrestador) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "LOWER(p.municipio) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "p.telefonoPrestador LIKE %:filtro%)")
    List<PrestadorServicio> buscarPorFiltro(@Param("filtro") String filtro);
    
    @Query("SELECT p FROM PrestadorServicio p WHERE p.activo = true")
    List<PrestadorServicio> findAllActivos();
}