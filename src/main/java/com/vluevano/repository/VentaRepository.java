package com.vluevano.repository;

import com.vluevano.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {
    List<Venta> findByUsuario_NombreUsuarioAndFechaVentaBetween(String nombreUsuario, LocalDateTime inicio, LocalDateTime fin);
    List<Venta> findByFechaVentaBetween(LocalDateTime inicio, LocalDateTime fin);
}