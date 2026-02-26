package com.vluevano.repository;

import com.vluevano.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {
    
    @Query("SELECT e FROM Empresa e WHERE e.activo = true AND (" +
           "LOWER(e.nombreEmpresa) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.rfcEmpresa) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(e.municipio) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "e.telefonoEmpresa LIKE %:filtro%)")
    List<Empresa> buscarPorFiltro(@Param("filtro") String filtro);
    
    @Query("SELECT e FROM Empresa e WHERE e.activo = true")
    List<Empresa> findAllActivos();
}