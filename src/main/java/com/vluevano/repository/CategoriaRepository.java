package com.vluevano.repository;

import com.vluevano.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    Optional<Categoria> findByNombreCategoriaAndActivoTrue(String nombre);
    
    @Query("SELECT c FROM Categoria c WHERE c.activo = true")
    List<Categoria> findAllActivos();
}