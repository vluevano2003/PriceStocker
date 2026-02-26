package com.vluevano.service;

import com.vluevano.model.Fabricante;
import com.vluevano.repository.FabricanteRepository;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FabricanteService {

    @Autowired
    private FabricanteRepository fabricanteRepository;

    @Autowired
    private GestorIdioma idioma;

    /**
     * Consulta todos los fabricantes
     */
    public List<Fabricante> consultarFabricantes() {
        return fabricanteRepository.findAllActivos();
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
        // Validaciones básicas con textos dinámicos
        if (fabricante.getNombreFabricante() == null || fabricante.getNombreFabricante().trim().isEmpty()) {
            return idioma.get("srv.manufacturer.val.name");
        }

        if (fabricante.getRfcFabricante() != null && !fabricante.getRfcFabricante().isEmpty()) {
            if (!fabricante.getRfcFabricante().matches("[A-Za-z0-9]{12,13}")) {
                return idioma.get("srv.manufacturer.val.rfc");
            }
        }

        if (fabricante.getTelefonoFabricante() != null && !fabricante.getTelefonoFabricante().isEmpty()) {
            if (!fabricante.getTelefonoFabricante().matches("\\d{10}")) {
                return idioma.get("srv.manufacturer.val.phone");
            }
        }

        if (fabricante.getCorreoFabricante() != null && !fabricante.getCorreoFabricante().isEmpty()) {
            if (!fabricante.getCorreoFabricante().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                return idioma.get("srv.manufacturer.val.email");
            }
        }

        if (String.valueOf(fabricante.getCpFabricante()).length() != 5) {
            return idioma.get("srv.manufacturer.val.cp");
        }

        try {
            fabricanteRepository.save(fabricante);
            return idioma.get("srv.manufacturer.msg.success");
        } catch (Exception e) {
            e.printStackTrace();
            return idioma.get("srv.manufacturer.msg.error") + " " + e.getMessage();
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
            fabricante.setActivo(false);
            fabricanteRepository.save(fabricante); 
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}