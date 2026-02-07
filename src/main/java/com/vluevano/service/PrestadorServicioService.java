package com.vluevano.service;

import com.vluevano.model.PrestadorServicio;
import com.vluevano.repository.PrestadorServicioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrestadorServicioService {

    @Autowired
    private PrestadorServicioRepository repository;

    /**
     * Consulta todos los prestadores de servicio
     * 
     * @return
     */
    public List<PrestadorServicio> consultarPrestadores() {
        return repository.findAll();
    }

    /**
     * Busca prestadores de servicio por filtro
     * 
     * @param filtro
     * @return
     */
    public List<PrestadorServicio> buscarPrestadores(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return consultarPrestadores();
        }
        return repository.buscarPorFiltro(filtro);
    }

    /**
     * Guarda un prestador de servicio
     * 
     * @param prestador
     * @return
     */
    @Transactional
    public String guardarPrestador(PrestadorServicio prestador) {
        if (prestador.getNombrePrestador() == null || prestador.getNombrePrestador().trim().isEmpty()) {
            return "El nombre del prestador es obligatorio.";
        }

        try {
            repository.save(prestador);
            return "Prestador guardado exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al guardar: " + e.getMessage();
        }
    }

    /**
     * Elimina un prestador de servicio
     * 
     * @param prestador
     * @return
     */
    @Transactional
    public boolean eliminarPrestador(PrestadorServicio prestador) {
        try {
            repository.delete(prestador);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}