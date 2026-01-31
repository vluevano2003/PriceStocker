package com.vluevano.service;

import com.vluevano.model.Empresa;
import com.vluevano.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    /**
     * Consulta todas las empresas
     * @return
     */
    public List<Empresa> consultarEmpresas() {
        return empresaRepository.findAll();
    }

    /**
     * Busca empresas por filtro
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
     * @param empresa
     * @return
     */
    @Transactional
    public String guardarEmpresa(Empresa empresa) {
        if (empresa.getNombreEmpresa() == null || empresa.getNombreEmpresa().trim().isEmpty()) {
            return "El nombre de la empresa es obligatorio.";
        }
        
        if (empresa.getRfcEmpresa() != null && !empresa.getRfcEmpresa().isEmpty()) {
            if (empresa.getRfcEmpresa().length() < 12 || empresa.getRfcEmpresa().length() > 13) {
                 return "El RFC debe tener 12 o 13 caracteres.";
            }
        }
        
        try {
            empresaRepository.save(empresa);
            return "Empresa guardada exitosamente.";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al guardar: " + e.getMessage();
        }
    }

    /**
     * Elimina una empresa
     * @param empresa
     * @return
     */
    @Transactional
    public boolean eliminarEmpresa(Empresa empresa) {
        try {
            empresaRepository.delete(empresa);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}