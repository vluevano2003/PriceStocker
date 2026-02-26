package com.vluevano.repository;

import com.vluevano.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {

    @Query("SELECT c FROM Cliente c WHERE c.activo = true AND (" +
           "LOWER(c.nombreCliente) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.rfcCliente) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "LOWER(c.municipio) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
           "c.telefonoCliente LIKE %:filtro%)")
    List<Cliente> buscarPorFiltro(@Param("filtro") String filtro);
    
    @Query("SELECT c FROM Cliente c WHERE c.activo = true")
    List<Cliente> findAllActivos();
}