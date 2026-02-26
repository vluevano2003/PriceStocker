package com.vluevano.service;

import com.vluevano.model.Empresa;
import com.vluevano.repository.EmpresaRepository;
import com.vluevano.util.GestorIdioma;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private GestorIdioma idioma;

    /**
     * Consulta todas las empresas
     * 
     * @return
     */
    public List<Empresa> consultarEmpresas() {
        return empresaRepository.findAllActivos();
    }

    /**
     * Busca empresas por filtro
     * 
     * @param filtro
     * @return
     */
    public List<Empresa> buscarEmpresas(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return consultarEmpresas();
        }
        return empresaRepository.buscarPorFiltro(filtro);
    }

    /**
     * Guarda o actualiza una empresa
     * 
     * @param empresa
     * @return
     */
    @Transactional
    public String guardarEmpresa(Empresa empresa) {
        if (empresa.getNombreEmpresa() == null || empresa.getNombreEmpresa().trim().isEmpty()) {
            return idioma.get("srv.company.val.name");
        }

        if (empresa.getRfcEmpresa() != null && !empresa.getRfcEmpresa().isEmpty()) {
            if (empresa.getRfcEmpresa().length() < 12 || empresa.getRfcEmpresa().length() > 13) {
                return idioma.get("srv.company.val.rfc");
            }
        }

        try {
            empresaRepository.save(empresa);
            return idioma.get("srv.company.msg.success");
        } catch (Exception e) {
            e.printStackTrace();
            return idioma.get("srv.company.msg.error") + " " + e.getMessage();
        }
    }

    /**
     * Elimina una empresa
     * 
     * @param empresa
     * @return
     */
    @Transactional
    public boolean eliminarEmpresa(Empresa empresa) {
        try {
            empresa.setActivo(false);
            empresaRepository.save(empresa);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}