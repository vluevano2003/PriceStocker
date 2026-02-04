package com.vluevano.service;

import com.vluevano.model.Categoria;
import com.vluevano.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> consultarCategorias() {
        return categoriaRepository.findAll();
    }
    
    // Aquí puedes agregar métodos para guardar/editar categorías si lo requieres
}