package com.vluevano.repository;

import com.vluevano.model.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ServicioRepository extends JpaRepository<Servicio, Integer> {
    @Query("SELECT s FROM Servicio s WHERE s.activo = true")
    List<Servicio> findAllActivos();
}