package com.vluevano.service;

import com.vluevano.model.Fabricante;
import com.vluevano.repository.FabricanteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FabricanteService {

    @Autowired
    private FabricanteRepository fabricanteRepository;

    /**
     * Consulta todos los fabricantes
     */
    public List<Fabricante> consultarFabricantes() {
        return fabricanteRepository.findAll();
    }

    /**
     * Busca fabricantes por filtro
     * @param filtro
     * @return
     */
    public List<Fabricante> buscarFabricantes(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return consultarFabricantes();
        }
        return fabricanteRepository.buscarPorFiltro(filtro);
    }

    /**
     * Guarda o actualiza un fabricante
     * @param fabricante
     * @return
     */
    @Transactional
    public String guardarFabricante(Fabricante fabricante) {
        // Validaciones básicas
        if (fabricante.getNombreFabricante() == null || fabricante.getNombreFabricante().trim().isEmpty()) {
            return "El nombre del fabricante es obligatorio.";
        }

        if (fabricante.getRfcFabricante() != null && !fabricante.getRfcFabricante().isEmpty()) {
            if (!fabricante.getRfcFabricante().matches("[A-Za-z0-9]{12,13}")) {
                return "El RFC debe contener 12 o 13 caracteres alfanuméricos.";
            }
        }

        if (fabricante.getTelefonoFabricante() != null && !fabricante.getTelefonoFabricante().isEmpty()) {
            if (!fabricante.getTelefonoFabricante().matches("\\d{10}")) {
                return "El teléfono debe tener exactamente 10 dígitos.";
            }
        }

        if (fabricante.getCorreoFabricante() != null && !fabricante.getCorreoFabricante().isEmpty()) {
            if (!fabricante.getCorreoFabricante().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return "El correo electrónico no es válido.";
            }
        }

        if (String.valueOf(fabricante.getCpFabricante()).length() != 5) {
            return "El código postal debe tener 5 dígitos.";
        }

        try {
            fabricanteRepository.save(fabricante);
            return "Fabricante guardado exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al guardar: " + e.getMessage();
        }
    }

    /**
     * Elimina un fabricante
     * @param fabricante
     * @return
     */
    @Transactional
    public boolean eliminarFabricante(Fabricante fabricante) {
        try {
            fabricanteRepository.delete(fabricante);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}