package com.vluevano.service;

import com.vluevano.model.PrestadorServicio;
import com.vluevano.repository.PrestadorServicioRepository;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PrestadorServicioService {

    @Autowired
    private PrestadorServicioRepository repository;

    @Autowired
    private GestorIdioma idioma;

    /**
     * Consulta todos los prestadores de servicio
     * * @return
     */
    public List<PrestadorServicio> consultarPrestadores() {
        return repository.findAllActivos();
    }

    /**
     * Busca prestadores de servicio por filtro
     * * @param filtro
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
     * * @param prestador
     * @return
     */
    @Transactional
    public String guardarPrestador(PrestadorServicio prestador) {
        if (prestador.getNombrePrestador() == null || prestador.getNombrePrestador().trim().isEmpty()) {
            return idioma.get("srv.provider.val.name");
        }

        try {
            repository.save(prestador);
            return idioma.get("srv.provider.msg.success");
        } catch (Exception e) {
            e.printStackTrace();
            return idioma.get("srv.provider.msg.error") + " " + e.getMessage();
        }
    }

    /**
     * Elimina un prestador de servicio
     * * @param prestador
     * @return
     */
    @Transactional
    public boolean eliminarPrestador(PrestadorServicio prestador) {
        try {
            prestador.setActivo(false);
            repository.save(prestador);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}